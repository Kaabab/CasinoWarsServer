package com.casinowars.server.service

import com.casinowars.server.api.models.*
import com.casinowars.server.configuration.Configuration
import com.casinowars.server.deck.DeckManager
import com.casinowars.server.repository.Repository
import com.casinowars.server.repository.model.GameData
import com.casinowars.server.repository.model.PlayerData
import com.google.inject.Inject
import io.vertx.core.CompositeFuture
import io.vertx.core.Future
import io.vertx.core.Promise
import java.util.*
import kotlin.math.roundToInt

class GameServiceImpl : GameService {

  @Inject
  val repository: Repository? = null

  @Inject
  val deckManager: DeckManager? = null

  @Inject
  val configuration: Configuration? = null


  override fun createGame(playerId: UUID): Future<UUID> {
    val promise = Promise.promise<UUID>()
    val deck = deckManager!!.generateShuffledDeck()
    val gameId = UUID.randomUUID()
    val game = repository!!.createGame(gameId, playerId, deck)
    game.onSuccess {
      promise.complete(gameId)
    }
    game.onFailure(promise::fail)
    return promise.future()
  }

  override fun getGameById(gameId: UUID): Future<GameData> {
    val promise = Promise.promise<GameData>()
    val game = repository!!.getGame(gameId)
    game.onSuccess {
      if (it != null) {
        promise.complete(it)
      }else {
        promise.fail(GameServiceException(404, "Game not found", null))
      }
    }
    game.onFailure(promise::fail)
    return promise.future()
  }

  override fun getPlayerById(playerId: UUID): Future<Player> {
    val promise = Promise.promise<Player>()
    val playerIdFuture = repository!!.getPlayer(playerId)
    playerIdFuture.onSuccess {
      if (it != null) {
        promise.complete(Player(it.id, it.name, it.tokenCount))
      }else {
        promise.fail(GameServiceException(404, "Player Not found", null))
      }
    }
    playerIdFuture.onFailure(promise::fail)
    return promise.future()
  }

  override fun updatePlayerToken(playerId: UUID, tokenCount: Int): Future<Void> {
    val promise = Promise.promise<Void>()
    val player = getPlayerById(playerId)
    player.onSuccess {
      val update = repository!!.updatePlayer(playerId, it.tokenCount!!, tokenCount)
      update.onSuccess {
        promise.complete()
      }
      update.onFailure(promise::fail)
    }
    player.onFailure(promise::fail)
    return promise.future()
  }

  override fun createPlayer(playerCreationRequest: PlayerCreationRequest): Future<Player> {
    val promise = Promise.promise<Player>()
    val playerId = UUID.randomUUID()
    val playerName = playerCreationRequest.name!!
    val tokenCount = configuration!!.serverConfiguration.defaultTokenCount
    val player = repository!!.createPlayer(playerId, playerName, tokenCount)
    player.onSuccess {
      promise.complete(
        Player(playerId, playerName, tokenCount)
      )
    }
    player.onFailure(promise::fail)
    return promise.future()
  }

  override fun requestBet(gameSessionId: UUID, bet: BetRequest): Future<BetResponse> {
    val promise = Promise.promise<BetResponse>()
    val gameFuture = repository!!.getGame(gameSessionId)
    val playerFuture = repository.getPlayer(bet.playerId!!)
    // query data from db with future composition
    val composite = CompositeFuture.all(gameFuture, playerFuture)
    composite.onSuccess {
      val game = it.resultAt<GameData>(0)
      val player = it.resultAt<PlayerData>(1)
      if (validateBet(player, bet, promise, game)) {
        if (game.tableTokens > 0) {
          handleWarBet(bet, player, game, gameSessionId, promise)
        } else {
          handleBet(bet, player, game, gameSessionId, promise)
        }
      }

    }

    composite.onFailure(promise::fail)
    return promise.future()
  }

  private fun handleBet(
    bet: BetRequest,
    player: PlayerData,
    game: GameData,
    gameSessionId: UUID,
    promise: Promise<BetResponse>
  ) {
    val nextCardIndex = game.topCardIndex + 1
    if (nextCardIndex + 1 >= game.deck.size) {
      val extendDeck = extendGameDeck(gameSessionId, game)
      extendDeck.onSuccess {
        handleBet(bet, player, it, gameSessionId, promise)
      }
      extendDeck.onFailure(promise::fail)
    }
    var topCardIndex = game.topCardIndex
    val playerCardIndex = game.deck[topCardIndex]
    topCardIndex++
    val casinoCardIndex = game.deck[topCardIndex]
    topCardIndex++
    val playerCard = deckManager!!.indexToCard(playerCardIndex)
    val casinoCard = deckManager.indexToCard(casinoCardIndex)
    var newTokens = -bet.bet!!
    newTokens -= bet.tieBet!!
    var tableTokens = 0
    val betResult: BetResult
    if (playerCard.value!! == casinoCard.value!!) {
      if (bet.tieBet > 0) {
        newTokens = (bet.tieBet * 10) - bet.bet
        betResult = BetResult.tiewin
      } else {
        tableTokens = bet.bet
        betResult = BetResult.war
      }
    } else if (playerCard.value > casinoCard.value) {
      newTokens = bet.bet - bet.tieBet
      betResult = BetResult.win
    } else {
      betResult = BetResult.loss
    }
    val newTotal = player.tokenCount + newTokens
    val playerUpdate = repository!!.updatePlayer(player.id, player.tokenCount, newTotal)
    val gameUpdate = repository.updateGame(gameSessionId, game.deck, game.topCardIndex, topCardIndex, tableTokens)
    val updateComposite = CompositeFuture.all(playerUpdate, gameUpdate)
    updateComposite.onSuccess {
      promise.complete(BetResponse(playerCard, casinoCard, betResult, newTokens))
    }
    updateComposite.onFailure(promise::fail)
  }

  private fun handleWarBet(
    bet: BetRequest,
    player: PlayerData,
    game: GameData,
    gameSessionId: UUID,
    promise: Promise<BetResponse>
  ) {
    if (bet.bet == 0) {
      handleForfeit(player, game, gameSessionId, promise)
    } else {
      // validate bet is equal to table token, otherwise bet is invalid, if valid process war
      if (bet.bet == game.tableTokens) {
        handleWar(player, game, bet, gameSessionId, promise)
      } else {
        promise.fail(
          GameServiceException(
            409,
            "Invalid bet for war, previous stakes must doubled, please bet 0 or ${game.tableTokens}",
            null
          )
        )
      }
    }
  }

  private fun validateBet(
    player: PlayerData,
    bet: BetRequest,
    promise: Promise<BetResponse>,
    game: GameData
  ): Boolean {
    if (player.tokenCount < bet.bet!! || player.tokenCount < bet.bet) {
      promise.fail(
        GameServiceException(
          409,
          "Not enough token for bet, Player has ${game.tableTokens}, bet value ${bet.bet}, side bet value ${bet.bet}",
          null
        )
      )
      return false
    }
    return true
  }

  private fun handleWar(
    player: PlayerData,
    game: GameData,
    bet: BetRequest,
    gameSessionId: UUID,
    promise: Promise<BetResponse>
  ) {
    val nextCardIndex = game.topCardIndex + 3
    if (nextCardIndex + 1 >= game.deck.size) {
      val extendDeck = extendGameDeck(gameSessionId, game)
      extendDeck.onSuccess {
        handleWar(player, it, bet, gameSessionId, promise)
      }
      extendDeck.onFailure(promise::fail)
    } else {
      var topCardIndex = nextCardIndex
      val playerCardIndex = game.deck[topCardIndex]
      topCardIndex++
      val casinoCardIndex = game.deck[topCardIndex]
      val playerCard = deckManager!!.indexToCard(playerCardIndex)
      val casinoCard = deckManager.indexToCard(casinoCardIndex)
      topCardIndex++
      if (playerCard.value!! > casinoCard.value!!) {
        // player is awarded back his bet, no deduction for this turn or change in data
        // the bet is discarded
        promise.complete(BetResponse(playerCard, casinoCard, BetResult.win, bet.bet))
      } else {

        val newTokens: Int
        var newTotal = player.tokenCount
        val betResult: BetResult
        if (playerCard.value == casinoCard.value)
        {
          betResult = BetResult.tiewin
          // Player is rewarded the double stack to be added to his token count, he gets the table tokens and his current wager
          newTokens = bet.bet!! * 2
          newTotal += newTokens
        } else {
          betResult = BetResult.loss
          // Player lost the war and his double wager, the previous wager is on the table and is clearer
          newTokens = bet.bet!! * -1
          newTotal += newTokens
        }
        val playerUpdate = repository!!.updatePlayer(player.id, player.tokenCount, newTotal)
        val gameUpdate = repository.updateGame(gameSessionId, game.deck, game.topCardIndex, topCardIndex, 0)
        val updateComposite = CompositeFuture.all(playerUpdate, gameUpdate)
        updateComposite.onSuccess {
          promise.complete(BetResponse(playerCard, casinoCard, betResult, newTokens))
        }
        updateComposite.onFailure(promise::fail)
      }
    }
  }

  private fun handleForfeit(
    player: PlayerData,
    game: GameData,
    gameSessionId: UUID,
    promise: Promise<BetResponse>
  ) {
    /**
     * Forfeited war
     */
    val returnedToken:Int =  (game.tableTokens * 0.5f).roundToInt()
    val newPlayerTokens = player.tokenCount + returnedToken
    val playerUpdate = repository!!.updatePlayer(player.id, player.tokenCount, newPlayerTokens)
    val gameUpdate = repository.updateGame(gameSessionId, game.deck, game.topCardIndex, game.topCardIndex, 0)
    val updateComposite = CompositeFuture.all(playerUpdate, gameUpdate)
    updateComposite.onSuccess {
      promise.complete(BetResponse(null, null, BetResult.surrender, returnedToken))
    }
    updateComposite.onFailure(promise::fail)
  }

  private fun extendGameDeck(id: UUID, gameData: GameData): Future<GameData> {
    val promise = Promise.promise<GameData>()
    val newDeck = deckManager!!.generateShuffledDeck()
    val expandedDeck = gameData.deck + newDeck
    val updateData = repository!!.updateGame(
      id,
      expandedDeck,
      gameData.topCardIndex,
      gameData.topCardIndex,
      gameData.tableTokens
    )
    updateData.onSuccess {
      promise.complete(
        GameData(
          expandedDeck, gameData.topCardIndex,
          gameData.tableTokens
        )
      )
    }
    updateData.onFailure(promise::fail)
    return promise.future()
  }
}
