package com.casinowars.server.api

import io.vertx.ext.web.RoutingContext

/**
 * Main API interface handling requests from vertx routing context
 */
interface GameApi {
  fun getPlayerById(routingContext: RoutingContext)
  fun updatePlayerToken(routingContext: RoutingContext)
  fun createPlayer(routingContext: RoutingContext)
  fun requestBet(routingContext: RoutingContext)
  fun createGame(routingContext: RoutingContext)
}
