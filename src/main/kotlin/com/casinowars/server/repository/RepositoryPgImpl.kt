package com.casinowars.server.repository

import com.casinowars.server.repository.model.GameData
import com.casinowars.server.repository.model.PlayerData
import com.google.inject.Inject
import io.vertx.core.Future
import io.vertx.core.Promise
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Tuple
import java.util.*

const val INSERT_GAME = "INSERT INTO game VALUES ($1, $2, $3, $4, $5)"
const val UPDATE_GAME =
  "UPDATE game SET top_card_index = $3, table_tokens = $4, deck = $5 WHERE id = $1 AND top_card_index = $2"
const val GET_GAME = "SELECT deck, top_card_index, table_tokens from game WHERE id = $1"
const  val UPDATE_PLAYER = "UPDATE player SET token_count = $3 WHERE token_count = $2 AND id = $1"
const  val GET_PLAYER = "SELECT player_name, token_count from player WHERE id = $1"
const  val INSERT_PLAYER = "INSERT INTO player VALUES ($1, $2, $3)"
const  val GET_GAME_BY_PLAYER_ID = "SELECT id from game WHERE player_id = $1"

class RepositoryPgImpl : Repository {

  @Inject
  private val sqlClient: SqlClient? = null

  override fun createGame(gameId: UUID, playerId: UUID, deck: List<Int>): Future<GameData> {
    val promise = Promise.promise<GameData>()
    val query = sqlClient!!.preparedQuery(INSERT_GAME)
    val tuple = Tuple.of(gameId.toString(), playerId.toString())
    tuple.addArrayOfInteger(deck.toTypedArray())
    tuple.addInteger(0)
    tuple.addInteger(0)
    query.execute(tuple).onComplete {
      if (it.succeeded()) {
        promise.complete(GameData(deck, 0, 0))
      } else {
        promise.fail(it.cause())
      }
    }
    return promise.future()
  }

  override fun updateGame(
    gameId: UUID,
    deck: List<Int>,
    currentIndex: Int,
    newIndex: Int,
    tableTokenCount: Int
  ): Future<Void> {
    val promise = Promise.promise<Void>()
    val query = sqlClient!!.preparedQuery(UPDATE_GAME)
    val tuple = Tuple.of(gameId.toString(), currentIndex, newIndex, tableTokenCount)
    tuple.addArrayOfInteger(deck.toTypedArray())
    query.execute(tuple).onComplete {
      if (it.succeeded()) {
        promise.complete()
      } else {
        promise.fail(it.cause())
      }
    }
    return promise.future()
  }

  override fun getGame(gameId: UUID): Future<GameData> {
    val promise = Promise.promise<GameData>()
    val query = sqlClient!!.preparedQuery(GET_GAME)
    val tuple = Tuple.of(gameId.toString())
    query.execute(tuple).onComplete {
      if (it.succeeded()) {
        val rows = it.result()
        if (rows.size() > 0) {
          val row = rows.first()
          val deck = row.getArrayOfIntegers(0)
          val topCardIndex = row.getInteger(1)
          val tableTokenCount = row.getInteger(2)
          val gameData = GameData(deck.toList(), topCardIndex, tableTokenCount)
          promise.complete(gameData)
        }else{
          promise.complete(null)
        }
      } else {
        promise.fail(it.cause())
      }
    }
    return promise.future()
  }

  override fun getPlayerSessionById(playerId: UUID): Future<UUID> {
    val promise = Promise.promise<UUID>()
    val query = sqlClient!!.preparedQuery(GET_GAME_BY_PLAYER_ID)
    val tuple = Tuple.of(playerId.toString())
    query.execute(tuple).onComplete {
      if (it.succeeded()) {
        val rows = it.result()
        if (rows.size() > 0) {
          val row = rows.first()
          val id = row.getString(0)
          promise.complete(UUID.fromString(id))
        }else {
          promise.complete(null)
        }
      } else {
        promise.fail(it.cause())
      }
    }
    return promise.future()
  }

  override fun getPlayer(playerId: UUID): Future<PlayerData> {
    val promise = Promise.promise<PlayerData>()
    val query = sqlClient!!.preparedQuery(GET_PLAYER)
    val tuple = Tuple.of(playerId.toString())
    query.execute(tuple).onComplete {
      if (it.succeeded()) {
        val rows = it.result()
        if (rows.size() > 0) {
          val row = rows.first()
          val name = row.getString(0)
          val tokenCount = row.getInteger(1)
          val playerData = PlayerData(playerId, name, tokenCount)
          promise.complete(playerData)
        }
        else {
          promise.complete(null)
        }
      } else {
        promise.fail(it.cause())
      }
    }
    return promise.future()
  }

  override fun createPlayer(playerId: UUID, playerName: String, tokenCount: Int): Future<PlayerData> {
    val promise = Promise.promise<PlayerData>()
    val query = sqlClient!!.preparedQuery(INSERT_PLAYER)
    val tuple = Tuple.of(playerId.toString(), playerName, tokenCount)
    query.execute(tuple).onComplete {
      if (it.succeeded()) {
        promise.complete(PlayerData(playerId, playerName, tokenCount))
      } else {
        promise.fail(it.cause())
      }
    }
    return promise.future()
  }

  override fun updatePlayer(playerId: UUID, currentTokenCount: Int, newTokenCount: Int): Future<Void> {
    val promise = Promise.promise<Void>()
    val query = sqlClient!!.preparedQuery(UPDATE_PLAYER)
    val tuple = Tuple.of(playerId.toString(), currentTokenCount, newTokenCount)
    query.execute(tuple).onComplete {
      if (it.succeeded()) {
        promise.complete()
      } else {
        promise.fail(it.cause())
      }
    }
    return promise.future()
  }

}
