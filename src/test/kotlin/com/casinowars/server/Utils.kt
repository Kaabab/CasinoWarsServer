package com.casinowars.server

import io.vertx.core.Future

object Utils {

    fun <T> futureSuccess(result: T?): Future<T>? {
      return if (result == null) {
        io.vertx.core.impl.future.SucceededFuture.EMPTY as Future<T>
      } else {
        io.vertx.core.impl.future.SucceededFuture(result)
      }
    }


}
