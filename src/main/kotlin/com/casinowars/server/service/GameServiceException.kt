package com.casinowars.server.service

class GameServiceException(
  val statusCode: Int,
  override val message: String?,
  override val cause: Throwable?,
) : Exception(message, cause)
