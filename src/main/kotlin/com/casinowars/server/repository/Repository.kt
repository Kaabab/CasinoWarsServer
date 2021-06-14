package com.casinowars.server.repository

import com.casinowars.server.repository.model.GameData
import com.casinowars.server.repository.model.PlayerData
import io.vertx.core.Future
import java.util.*

/**
 * Interface for repository/storage related operations
 *
 */
interface Repository {
  /**
   * Insert new game session in db
   */
  fun createGame(gameId: UUID, playerId: UUID, deck: List<Int>): Future<GameData>

  /**
   * Update game session
   */
  fun updateGame(gameId: UUID, deck: List<Int>, currentIndex: Int, newIndex: Int, tableTokenCount: Int): Future<Void>

  /**
   * Return game data by id
   */
  fun getGame(gameId: UUID): Future<GameData>

  /**
   * Return player by id
   */
  fun getPlayer(playerId: UUID): Future<PlayerData>

  /**
   * Return player by id
   */
  fun getPlayerSessionById(playerId: UUID): Future<UUID>

  /**
   * Create new Player
   */
  fun createPlayer(playerId: UUID, playerName: String, tokenCount: Int): Future<PlayerData>

  /**
   * Update player tokens by id
   */
  fun updatePlayer(playerId: UUID, currentTokenCount: Int, newTokenCount: Int): Future<Void>
}
