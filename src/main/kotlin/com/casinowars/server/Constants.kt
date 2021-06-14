package com.casinowars.server

const val SWAGGER_PATH = "swagger.yaml"
const val DB_CONFIGURATION_PATH = "db-config.yaml"
const val SERVER_CONFIGURATION_PATH = "server-config.yaml"


object OperationIds {
  const val RequestBet: String = "requestBet"
  const val CreateGame: String = "createGame"
  const val GetPlayerById: String = "getPlayerById"
  const val UpdatePlayerTokens: String = "updatePlayerToken"
  const val CreatePlayer: String = "createPlayer"
}
