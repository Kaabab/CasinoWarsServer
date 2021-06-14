package com.casinowars.server

import com.casinowars.server.configuration.Configuration
import com.casinowars.server.configuration.ConfigurationManager
import com.casinowars.server.guice.GuiceVerticleFactory
import com.casinowars.server.modules.ServiceModules
import com.casinowars.server.yaml.YamlMapper
import com.google.inject.Guice
import com.google.inject.Injector
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.impl.launcher.VertxCommandLauncher
import io.vertx.core.impl.launcher.VertxLifecycleHooks
import io.vertx.core.json.JsonObject

/**
 * Main application entry point
 *
 * Starts the application and registers the guice vertx factory at start time
 */
class Launcher : VertxCommandLauncher(), VertxLifecycleHooks {

  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      Launcher().dispatch(args)
    }
  }

  private val mapper = YamlMapper()
  private val configuration: Configuration = ConfigurationManager(mapper).configuration
  private var serviceModules: ServiceModules? = null

  private fun createInjector(): Injector {
    return Guice.createInjector(serviceModules!!.modules)
  }

  override fun afterStartingVertx(vertx: Vertx) {
    serviceModules = ServiceModules(vertx, configuration)
    val guiceVerticleFactory = GuiceVerticleFactory(createInjector())
    vertx.registerVerticleFactory(guiceVerticleFactory)
  }

  override fun beforeDeployingVerticle(deploymentOptions: DeploymentOptions?) {
  }

  override fun beforeStoppingVertx(vertx: Vertx?) {
  }

  override fun afterStoppingVertx() {
    serviceModules?.modules?.forEach { module -> module.dispose() }
  }

  override fun afterConfigParsed(config: JsonObject?) {
  }

  override fun beforeStartingVertx(options: VertxOptions?) {
  }

  override fun handleDeployFailed(
    vertx: Vertx?,
    mainVerticle: String?,
    deploymentOptions: DeploymentOptions?,
    cause: Throwable?
  ) {
    vertx?.close()
  }


}
