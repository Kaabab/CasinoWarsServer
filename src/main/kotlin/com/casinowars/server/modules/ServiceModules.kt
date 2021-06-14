package com.casinowars.server.modules

import io.vertx.core.Vertx

/**
 * Defines the service modules
 */
data class ServiceModules(
  private val vertx: Vertx,
  private val configuration: com.casinowars.server.configuration.Configuration
) {
  val modules: List<Module> = listOf(
    VertxModule(vertx),
    DbModule(configuration),
    GameModule(configuration)
  )

}
