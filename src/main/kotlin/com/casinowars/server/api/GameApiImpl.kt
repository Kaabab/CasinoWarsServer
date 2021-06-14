package com.casinowars.server.api


import com.casinowars.server.api.models.*
import com.casinowars.server.service.GameService
import com.casinowars.server.service.GameServiceException
import com.google.inject.Inject
import io.vertx.core.http.HttpHeaders
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.RequestParameters
import io.vertx.ext.web.validation.ValidationHandler
import org.apache.commons.lang3.exception.ExceptionUtils
import java.util.*

/**
 * Game api default implementation
 */
class GameApiImpl : GameApi {

  @Inject
  val gameService: GameService? = null

  override fun getPlayerById(routingContext: RoutingContext) {
    val params: RequestParameters = routingContext[PARSED_PARAMS]
    val id = params.pathParameter(ParameterIds.PlayerId).string
    gameService!!.getPlayerById(UUID.fromString(id)).onComplete {
      if (it.succeeded()) {
        routingContext
          .response()
          .setStatusCode(HTTP_200)
          .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
          .end(JsonObject.mapFrom(it.result()).encode())
      } else {
        handleServiceException(routingContext, it.cause())
      }
    }
  }

  override fun updatePlayerToken(routingContext: RoutingContext) {
    val params: RequestParameters = routingContext[PARSED_PARAMS]
    val id = params.pathParameter(ParameterIds.PlayerId).string
    val contextParams: RequestParameters = routingContext[ValidationHandler.REQUEST_CONTEXT_KEY]
    val request = contextParams.body().jsonObject
    val requestMapped: PlayerUpdateRequest = request.mapTo(PlayerUpdateRequest::class.java) as PlayerUpdateRequest
    gameService!!.updatePlayerToken(UUID.fromString(id), requestMapped.tokenCount!!).onComplete {
      if (it.succeeded()) {
        routingContext
          .response()
          .setStatusCode(HTTP_200)
          .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
          .end()
      } else {
        handleServiceException(routingContext, it.cause())
      }
    }
  }

  override fun createPlayer(routingContext: RoutingContext) {
    val contextParams: RequestParameters = routingContext[ValidationHandler.REQUEST_CONTEXT_KEY]
    val request = contextParams.body().jsonObject
    val playerCreationRequest = request.mapTo(PlayerCreationRequest::class.java) as PlayerCreationRequest
    gameService!!.createPlayer(playerCreationRequest).onComplete {
      if (it.succeeded()) {
        routingContext
          .response()
          .setStatusCode(HTTP_200)
          .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
          .end(JsonObject.mapFrom(it.result()).encode())
      } else {
        handleServiceException(routingContext, it.cause())
      }
    }
  }

  override fun requestBet(routingContext: RoutingContext) {
    val contextParams: RequestParameters = routingContext[ValidationHandler.REQUEST_CONTEXT_KEY]
    val sessionId = routingContext.pathParam(ParameterIds.SessionId)
    val request = contextParams.body().jsonObject
    val betRequest: BetRequest = request.mapTo(BetRequest::class.java) as BetRequest
    gameService!!.requestBet(UUID.fromString(sessionId), betRequest).onComplete {
      if (it.succeeded()) {
        routingContext
          .response()
          .setStatusCode(HTTP_200)
          .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
          .end(JsonObject.mapFrom(it.result()).encode())
      } else {
        handleServiceException(routingContext, it.cause())
      }
    }
  }

  override fun createGame(routingContext: RoutingContext) {
    val contextParams: RequestParameters = routingContext[ValidationHandler.REQUEST_CONTEXT_KEY]
    val request = contextParams.body().jsonObject
    val gameCreationRequest: GameCreationRequest =
      request.mapTo(GameCreationRequest::class.java) as GameCreationRequest
    gameService!!.createGame(gameCreationRequest.playerid!!).onComplete {
      if (it.succeeded()) {
        routingContext
          .response()
          .setStatusCode(HTTP_200)
          .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
          .end(JsonObject.mapFrom(GameSession(it.result())).encode())
      } else {
        handleServiceException(routingContext, it.cause())
      }
    }
  }

  private fun handleServiceException(routingContext: RoutingContext, exception: Throwable) {
    var serviceException: GameServiceException? = exception as? GameServiceException?
    if (serviceException == null) {
      serviceException = GameServiceException(HTTP_500, SERVER_ERROR_MESSAGE, exception)
    }
    val stack:String = if (serviceException.cause != null) {
      ExceptionUtils.getStackTrace(serviceException.cause)
    }else {
      ""
    }
    val errorObject: JsonObject = JsonObject()
      .put(STATUS_CODE, serviceException.statusCode)
      .put(
        MESSAGE, serviceException.message
      )
      .put(STACK, stack)
    routingContext
      .response()
      .setStatusCode(serviceException.statusCode)
      .putHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
      .end(errorObject.encode())
  }
}
