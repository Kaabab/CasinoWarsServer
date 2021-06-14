package com.casinowars.server.guice

import com.google.inject.Injector
import io.vertx.core.Promise
import io.vertx.core.Verticle
import io.vertx.core.spi.VerticleFactory
import java.util.concurrent.Callable

/**
 * GuiceVerticleFactory
 *
 * Holds the configured injector and registers against vertx to create injected Verticles
 */
class GuiceVerticleFactory(private val injector: Injector) : VerticleFactory {
  private val FACTORY_PREFIX = "guice"

  override fun prefix(): String {
    return FACTORY_PREFIX
  }

  override fun createVerticle(verticleName: String?, classLoader: ClassLoader?, promise: Promise<Callable<Verticle>>?) {
    val vertName = removePrefix(verticleName!!)
    val clazz: Class<Verticle>
    val verticle: Verticle
    try {
      clazz = classLoader!!.loadClass(vertName) as Class<Verticle>
      verticle = injector.getInstance(clazz)
    } catch (e: Exception) {
      promise!!.fail(e)
      return
    }
    promise!!.complete(Callable { verticle })
  }

  private fun removePrefix(identifiers: String): String {
    return identifiers.replace("$FACTORY_PREFIX:", "")
  }
}
