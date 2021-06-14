package com.casinowars.server.service

import com.casinowars.server.api.models.BetRequest
import com.casinowars.server.api.models.BetResponse
import com.casinowars.server.api.models.Player
import com.casinowars.server.api.models.PlayerCreationRequest
import com.casinowars.server.repository.model.GameData
import io.vertx.core.Future
import java.util.*

/**
 * Main service interface
 */
interface GameService {
  fun getGameById(gameId: UUID): Future<GameData>
  fun getPlayerById(playerId: UUID): Future<Player>
  fun updatePlayerToken(playerId: UUID, tokenCount: Int): Future<Void>
  fun createPlayer(playerCreationRequest: PlayerCreationRequest): Future<Player>
  fun requestBet(gameSessionId: UUID, bet: BetRequest): Future<BetResponse>
  fun createGame(playerId: UUID): Future<UUID>
}
