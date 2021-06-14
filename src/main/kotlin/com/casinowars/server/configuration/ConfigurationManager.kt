package com.casinowars.server.configuration

import com.casinowars.server.DB_CONFIGURATION_PATH
import com.casinowars.server.SERVER_CONFIGURATION_PATH
import com.casinowars.server.SWAGGER_PATH
import com.casinowars.server.yaml.YamlMapper

/**
 * ConfigurationManager
 *
 * Responsible for loading the configuration files and mapping using the provided Yaml mapper
 */
class ConfigurationManager(private val mapper: YamlMapper) {
  private val dbConfig: DbConfiguration = initDbConfig()
  private val serverConfiguration: ServerConfiguration = initServerConfig()
  private val openAPIData: String = initOpenAPIData()

  val configuration: Configuration = Configuration(serverConfiguration, dbConfig, openAPIData)

  private fun initOpenAPIData(): String {
   return this::class.java.classLoader.getResource(SWAGGER_PATH).readText()
  }

  private fun initDbConfig(): DbConfiguration {
    return mapper.read(this::class.java.classLoader.getResource(DB_CONFIGURATION_PATH).readText())
  }

  private fun initServerConfig(): ServerConfiguration {
    return mapper.read(this::class.java.classLoader.getResource(SERVER_CONFIGURATION_PATH).readText())
  }
}
