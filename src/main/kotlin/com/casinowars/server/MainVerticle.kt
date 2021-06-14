package com.casinowars.server

import com.casinowars.server.api.*
import com.casinowars.server.configuration.Configuration
import com.google.inject.Inject
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.ext.web.openapi.RouterBuilder


/**
 * Main verticle responsible for creating and configuring the open api router using [RouterBuilder]
 */
class MainVerticle : AbstractVerticle() {

  @Inject
  val configuration: Configuration? = null

  @Inject
  val gameAPI: GameApi? = null


  override fun start(startPromise: Promise<Void>?) {
    RouterBuilder.create(vertx, SWAGGER_PATH)
      .onSuccess { routerBuilder: RouterBuilder? ->

        routerBuilder!!.operation(OperationIds.RequestBet).handler(gameAPI!!::requestBet)
        routerBuilder.operation(OperationIds.CreateGame).handler(gameAPI::createGame)
        routerBuilder.operation(OperationIds.GetPlayerById).handler(gameAPI::getPlayerById)
        routerBuilder.operation(OperationIds.CreatePlayer).handler(gameAPI::createPlayer)
        routerBuilder.operation(OperationIds.UpdatePlayerTokens).handler(gameAPI::updatePlayerToken)

        val router = routerBuilder.createRouter()

        router.errorHandler(HTTP_404) { routingContext: RoutingContext ->
          val errorObject: JsonObject = JsonObject()
            .put(STATUS_CODE, HTTP_404)
            .put(
              MESSAGE,
              if (routingContext.failure() != null) routingContext.failure().message else "Not Found"
            )
          routingContext
            .response()
            .setStatusCode(HTTP_404)
            .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
            .end(errorObject.encode())
        }
        router.errorHandler(HTTP_400) { routingContext: RoutingContext ->
          val errorObject: JsonObject = JsonObject()
            .put(STATUS_CODE, HTTP_400)
            .put(
              MESSAGE,
              if (routingContext.failure() != null) routingContext.failure().message else "Validation Exception"
            )
          routingContext
            .response()
            .setStatusCode(HTTP_400)
            .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
            .end(errorObject.encode())
        }
        val server = vertx.createHttpServer(
          HttpServerOptions().setPort(configuration!!.serverConfiguration.serverPort).setHost("localhost")
        )
        server.requestHandler(router).listen(configuration.serverConfiguration.serverPort)
        router.route("/*").handler(StaticHandler.create().setDefaultContentEncoding("UTF-8"))
        router.route("/swagger.yaml").handler{
          it.response()
            .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_YAML)
            .end(configuration.openAPI)
        }
        startPromise!!.complete()
      }
      .onFailure { err: Throwable? ->
        startPromise!!.fail(err)
      }

  }

}
