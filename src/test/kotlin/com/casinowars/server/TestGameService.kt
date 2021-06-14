package com.casinowars.server

import com.casinowars.server.Utils.futureSuccess
import com.casinowars.server.api.models.BetRequest
import com.casinowars.server.api.models.BetResult
import com.casinowars.server.api.models.PlayerCreationRequest
import com.casinowars.server.configuration.Configuration
import com.casinowars.server.configuration.DbConfiguration
import com.casinowars.server.configuration.ServerConfiguration
import com.casinowars.server.deck.DeckManager
import com.casinowars.server.repository.Repository
import com.casinowars.server.repository.model.GameData
import com.casinowars.server.repository.model.PlayerData
import com.casinowars.server.service.GameService
import com.casinowars.server.service.GameServiceException
import com.casinowars.server.service.GameServiceImpl
import com.google.inject.AbstractModule
import com.google.inject.Guice
import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.capture
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Test game service
 */
@ExtendWith(VertxExtension::class)
class TestGameService {
  private var gameService: GameService? = null
  private val deckManager: DeckManager = DeckManager()
  @Mock
  private val repository: Repository? = null
  @Captor
  private lateinit var idCaptor: ArgumentCaptor<UUID>
  @Captor
  private lateinit var idCaptor2: ArgumentCaptor<UUID>
  @Captor
  private lateinit var intCaptor1: ArgumentCaptor<Int>
  @Captor
  private lateinit var stringCaptor: ArgumentCaptor<String>
  @Captor
  private lateinit var intCaptor2: ArgumentCaptor<Int>
  @Captor
  private lateinit var intListCaptor: ArgumentCaptor<List<Int>>

  private val config = Configuration(ServerConfiguration(0, 50), DbConfiguration("", 0, "", "", "", 0), "")

  @BeforeEach
  fun setup() {
    MockitoAnnotations.openMocks(this)
    val injector = Guice.createInjector(object : AbstractModule() {
      override fun configure() {
        bind(Configuration::class.java).toInstance(config)
        bind(DeckManager::class.java).toInstance(deckManager)
        bind(Repository::class.java).toInstance(repository)
        bind(GameService::class.java).to(GameServiceImpl::class.java)
      }
    })
    gameService =  injector.getInstance(GameService::class.java)
  }

  @Test
  fun testGetPlayerById(vertx: Vertx, testContext: VertxTestContext) {
    val testPlayerData = PlayerData(UUID.randomUUID(), "testUser", 880)
    Mockito.`when`(repository!!.getPlayer(any())).thenReturn(futureSuccess(testPlayerData))
    gameService!!.getPlayerById(testPlayerData.id).onComplete {
      assertTrue(it.succeeded(), "Request should succeed")
      Mockito.verify(repository).getPlayer(
        capture(idCaptor)
      )
      assertEquals(testPlayerData.id,idCaptor.value)
      assertEquals(it.result().name,testPlayerData.name)
      assertEquals(it.result().tokenCount,testPlayerData.tokenCount)
      testContext.completeNow()
    }
  }

  @Test
  fun testGetPlayerByIdNotFound(vertx: Vertx, testContext: VertxTestContext) {
    val testPlayerData = PlayerData(UUID.randomUUID(), "testUser", 880)
    Mockito.`when`(repository!!.getPlayer(any())).thenReturn(futureSuccess(null))
    gameService!!.getPlayerById(testPlayerData.id).onComplete {
      Mockito.verify(repository).getPlayer(
        capture(idCaptor)
      )
      assertEquals(testPlayerData.id,idCaptor.value)
      assert(it.failed())
      assertNotNull(it.cause())
      assert(it.cause() is GameServiceException)
      assertEquals((it.cause() as GameServiceException).statusCode, 404)
      testContext.completeNow()
    }
  }

  @Test
  fun testGetGameById(vertx: Vertx, testContext: VertxTestContext) {
    val testGameData = GameData(deckManager.generateShuffledDeck(), 8, 880)
    Mockito.`when`(repository!!.getGame(any())).thenReturn(futureSuccess(testGameData))
    val gameId = UUID.randomUUID()
    gameService!!.getGameById(gameId).onComplete {
      assertTrue(it.succeeded(), "Request should succeed")
      Mockito.verify(repository).getGame(
        capture(idCaptor)
      )
      assertEquals(gameId,idCaptor.value)
      assertEquals(it.result().deck,testGameData.deck)
      assertEquals(it.result().topCardIndex,testGameData.topCardIndex)
      assertEquals(it.result().tableTokens,testGameData.tableTokens)
      testContext.completeNow()
    }
  }

  @Test
  fun testUpdatePlayer(vertx: Vertx, testContext: VertxTestContext) {
    val testPlayerData = PlayerData(UUID.randomUUID(), "testUser", 880)
    Mockito.`when`(repository!!.getPlayer(any())).thenReturn(futureSuccess(testPlayerData))
    Mockito.`when`(repository.updatePlayer(any(), any(), any())).thenReturn(futureSuccess(null))
    val newCount = 90
    gameService!!.updatePlayerToken(testPlayerData.id, newCount).onComplete {
      assertTrue(it.succeeded(), "Request should succeed")
      Mockito.verify(repository).getPlayer(any())
      Mockito.verify(repository).updatePlayer(
        capture(idCaptor),
        capture(intCaptor1),
        capture(intCaptor2)
      )
      assertEquals(testPlayerData.id,idCaptor.value)
      assertEquals(testPlayerData.tokenCount,intCaptor1.value)
      assertEquals(newCount,intCaptor2.value)
      testContext.completeNow()
    }
  }

  @Test
  fun testGetGameByIdNotFound(vertx: Vertx, testContext: VertxTestContext) {
    Mockito.`when`(repository!!.getGame(any())).thenReturn(futureSuccess(null))
    val gameId = UUID.randomUUID()
    gameService!!.getGameById(gameId).onComplete {
      Mockito.verify(repository).getGame(
        capture(idCaptor)
      )
      assertEquals(gameId,idCaptor.value)
      assert(it.failed())
      assertNotNull(it.cause())
      assert(it.cause() is GameServiceException)
      assertEquals((it.cause() as GameServiceException).statusCode, 404)
      testContext.completeNow()
    }
  }

  @Test
  fun testCreatePlayer(vertx: Vertx, testContext: VertxTestContext) {
    val testPlayerData = PlayerData(UUID.randomUUID(), "testUser", 880)
    Mockito.`when`(repository!!.createPlayer(any(), any(), any())).thenReturn(futureSuccess(testPlayerData))
    gameService!!.createPlayer(PlayerCreationRequest(testPlayerData.name)).onComplete {
      assertTrue(it.succeeded(), "Request should succeed")
      Mockito.verify(repository).createPlayer(
        capture(idCaptor),
        capture(stringCaptor),
        capture(intCaptor1)
      )
      assertEquals(it.result().id,idCaptor.value)
      assertEquals(testPlayerData.name,stringCaptor.value)
      assertEquals(it.result().tokenCount,intCaptor1.value)
      testContext.completeNow()
    }
  }

  @Test
  fun testCreateGame(vertx: Vertx, testContext: VertxTestContext) {
    val testGameData = GameData(deckManager.generateShuffledDeck(), 8, 880)
    val playerId = UUID.randomUUID()
    Mockito.`when`(repository!!.createGame(any(), any(), any())).thenReturn(futureSuccess(testGameData))
    gameService!!.createGame(playerId).onComplete {
      assertTrue(it.succeeded(), "Request should succeed")
      Mockito.verify(repository).createGame(
        capture(idCaptor),
        capture(idCaptor2),
        capture(intListCaptor)
      )
      assertEquals(it.result(),idCaptor.value)
      assertEquals(playerId,idCaptor2.value)
      testContext.completeNow()
    }
  }

  @Test
  fun testInvalidBet(vertx: Vertx, testContext: VertxTestContext) {
    val testGameData = GameData(listOf(10, 2, 10, 2, 10, 2), 0, 0)
    val testPlayerData = PlayerData(UUID.randomUUID(), "testUser", 0)
    Mockito.`when`(repository!!.getGame(any())).thenReturn(futureSuccess(testGameData))
    Mockito.`when`(repository.getPlayer(any())).thenReturn(futureSuccess(testPlayerData))
    Mockito.`when`(repository.updatePlayer(any(), any(), any())).thenReturn(futureSuccess(null))
    Mockito.`when`(repository.updateGame(any(), any(), any(), any(), any())).thenReturn(futureSuccess(null))
    val sessionId = UUID.randomUUID()
    val betValue = 1
    gameService!!.requestBet(sessionId, BetRequest(betValue, 0, testPlayerData.id)).onComplete {
      assertTrue(it.failed(), "Request should fail, player has no tokens")
      assertTrue(it.cause() is GameServiceException)
      assertEquals((it.cause() as GameServiceException).statusCode, 409)
      testContext.completeNow()
    }
  }

  @Test
  fun testWinningBet(vertx: Vertx, testContext: VertxTestContext) {
    val testGameData = GameData(listOf(10, 2, 10, 2, 10, 2), 0, 0)
    val testPlayerData = PlayerData(UUID.randomUUID(), "testUser", 880)
    Mockito.`when`(repository!!.getGame(any())).thenReturn(futureSuccess(testGameData))
    Mockito.`when`(repository.getPlayer(any())).thenReturn(futureSuccess(testPlayerData))
    Mockito.`when`(repository.updatePlayer(any(), any(), any())).thenReturn(futureSuccess(null))
    Mockito.`when`(repository.updateGame(any(), any(), any(), any(), any())).thenReturn(futureSuccess(null))
    val sessionId = UUID.randomUUID()
    val betValue = 1
    gameService!!.requestBet(sessionId, BetRequest(betValue, 0, testPlayerData.id)).onComplete {
      assertTrue(it.succeeded(), "Request should succeed")
      val response = it.result()
      assertEquals(response.result!!, BetResult.win)
      assertEquals(response.tokenValue!!, betValue)
      assertEquals(response.playerCard!!, deckManager.indexToCard(testGameData.deck[0]))
      assertEquals(response.casinoCard!!, deckManager.indexToCard(testGameData.deck[1]))
      testContext.completeNow()
    }
  }

  @Test
  fun testLoosingBet(vertx: Vertx, testContext: VertxTestContext) {
    val testGameData = GameData(listOf(1, 2, 10, 2, 10, 2), 0, 0)
    val testPlayerData = PlayerData(UUID.randomUUID(), "testUser", 880)
    Mockito.`when`(repository!!.getGame(any())).thenReturn(futureSuccess(testGameData))
    Mockito.`when`(repository.getPlayer(any())).thenReturn(futureSuccess(testPlayerData))
    Mockito.`when`(repository.updatePlayer(any(), any(), any())).thenReturn(futureSuccess(null))
    Mockito.`when`(repository.updateGame(any(), any(), any(), any(), any())).thenReturn(futureSuccess(null))
    val sessionId = UUID.randomUUID()
    val betValue = 1
    gameService!!.requestBet(sessionId, BetRequest(betValue, 0, testPlayerData.id)).onComplete {
      assertTrue(it.succeeded(), "Request should succeed")
      val response = it.result()
      assertEquals(response.result!!, BetResult.loss)
      assertEquals(response.tokenValue!!, betValue * -1)
      assertEquals(response.playerCard!!, deckManager.indexToCard(testGameData.deck[0]))
      assertEquals(response.casinoCard!!, deckManager.indexToCard(testGameData.deck[1]))
      testContext.completeNow()
    }
  }

  @Test
  fun testTieBet(vertx: Vertx, testContext: VertxTestContext) {
    val testGameData = GameData(listOf(1, 1, 10, 2, 10, 2), 0, 0)
    val testPlayerData = PlayerData(UUID.randomUUID(), "testUser", 880)
    Mockito.`when`(repository!!.getGame(any())).thenReturn(futureSuccess(testGameData))
    Mockito.`when`(repository.getPlayer(any())).thenReturn(futureSuccess(testPlayerData))
    Mockito.`when`(repository.updatePlayer(any(), any(), any())).thenReturn(futureSuccess(null))
    Mockito.`when`(repository.updateGame(any(), any(), any(), any(), any())).thenReturn(futureSuccess(null))
    val sessionId = UUID.randomUUID()
    val betValue = 1
    gameService!!.requestBet(sessionId, BetRequest(0, betValue, testPlayerData.id)).onComplete {
      assertTrue(it.succeeded(), "Request should succeed")
      val response = it.result()
      assertEquals(response.result!!, BetResult.tiewin)
      assertEquals(response.tokenValue!!, betValue * 10)
      assertEquals(response.playerCard!!, deckManager.indexToCard(testGameData.deck[0]))
      assertEquals(response.casinoCard!!, deckManager.indexToCard(testGameData.deck[1]))
      testContext.completeNow()
    }
  }


}
