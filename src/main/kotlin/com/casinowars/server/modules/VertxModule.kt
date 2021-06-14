package com.casinowars.server.modules

import com.google.inject.AbstractModule
import io.vertx.core.Vertx

/**
 * Guice [AbstractModule] for vertx and container injections.
 */
class VertxModule(private val vertx: Vertx) : Module() {

  override fun configure() {
    bind(Vertx::class.java).toInstance(vertx)
  }

  override fun dispose() {
  }
}
