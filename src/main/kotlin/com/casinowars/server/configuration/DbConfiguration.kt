package com.casinowars.server.configuration

/**
 * Data class for configurable db values
 */
data class DbConfiguration(
  val host: String,
  val port: Int,
  val database: String,
  val username: String,
  val password: String,
  val poolMaxsize: Int
)
