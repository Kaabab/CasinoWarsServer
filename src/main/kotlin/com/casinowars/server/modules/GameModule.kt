package com.casinowars.server.modules

import com.casinowars.server.api.GameApi
import com.casinowars.server.api.GameApiImpl
import com.casinowars.server.configuration.Configuration
import com.casinowars.server.deck.DeckManager
import com.casinowars.server.repository.Repository
import com.casinowars.server.repository.RepositoryPgImpl
import com.casinowars.server.service.GameService
import com.casinowars.server.service.GameServiceImpl

/**
 * Main game module
 */
class GameModule(val configuration: Configuration) : Module() {
  private val deckManager: DeckManager = DeckManager()

  override fun configure() {
    bind(GameApi::class.java).to(GameApiImpl::class.java)
    bind(GameService::class.java).to(GameServiceImpl::class.java)
    bind(Configuration::class.java).toInstance(configuration)
    bind(DeckManager::class.java).toInstance(deckManager)
    bind(Repository::class.java).to(RepositoryPgImpl::class.java)
  }

  override fun dispose() {
  }
}
