package com.casinowars.server.configuration

/**
 * Main configuration data class for the application
 */
data class Configuration(val serverConfiguration: ServerConfiguration, val dbConfig: DbConfiguration, val openAPI: String)
