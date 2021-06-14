package com.casinowars.server.modules

import com.casinowars.server.configuration.Configuration
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions
import io.vertx.sqlclient.SqlClient

/**
 * Init and expose the pooled db client
 */
class DbModule(configuration: Configuration) : Module() {
  private val client: SqlClient

  init {
    val connectOptions = PgConnectOptions()
      .setPort(configuration.dbConfig.port)
      .setHost(configuration.dbConfig.host)
      .setDatabase(configuration.dbConfig.database)
      .setUser(configuration.dbConfig.username)
      .setPassword(configuration.dbConfig.password)
    val poolOptions = PoolOptions()
      .setMaxSize(configuration.dbConfig.poolMaxsize)
    client = PgPool.client(connectOptions, poolOptions)
  }

  override fun dispose() {
    client.close()
  }

  override fun configure() {
    bind(SqlClient::class.java).toInstance(client)
  }

}
