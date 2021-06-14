package com.casinowars.server

import com.casinowars.server.api.GameApi
import com.casinowars.server.configuration.Configuration
import com.casinowars.server.configuration.DbConfiguration
import com.casinowars.server.configuration.ServerConfiguration
import com.google.inject.AbstractModule
import com.google.inject.Guice
import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations

/**
 * Test loading the open api and deploying the main verticle
 */
@ExtendWith(VertxExtension::class)
class TestMainVerticle {
  @Mock
  private val gameApi: GameApi? = null

  @BeforeEach
  fun deploy_verticle(vertx: Vertx, testContext: VertxTestContext) {
    val config = Configuration(ServerConfiguration(9000, 5000), DbConfiguration("", 0, "", "", "", 0), "")
    MockitoAnnotations.openMocks(this)
    val injector = Guice.createInjector(object : AbstractModule() {
      override fun configure() {
        bind(GameApi::class.java).toInstance(gameApi)
        bind(Configuration::class.java).toInstance(config)
      }
    })
    val mainVerticle = injector.getInstance(MainVerticle::class.java)
    vertx.deployVerticle(mainVerticle,
      testContext.succeeding { id: String? -> testContext.completeNow() }
    )
  }

  @Test
  @kotlin.Throws(Throwable::class)
  fun verticle_deployed(vertx: Vertx?, testContext: VertxTestContext) {
    testContext.completeNow()
  }
}
