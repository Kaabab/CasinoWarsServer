package com.casinowars.server

import com.casinowars.server.Utils.futureSuccess
import com.casinowars.server.api.models.BetRequest
import com.casinowars.server.api.models.BetResult
import com.casinowars.server.configuration.Configuration
import com.casinowars.server.configuration.DbConfiguration
import com.casinowars.server.configuration.ServerConfiguration
import com.casinowars.server.deck.DeckManager
import com.casinowars.server.repository.Repository
import com.casinowars.server.repository.model.GameData
import com.casinowars.server.repository.model.PlayerData
import com.casinowars.server.service.GameService
import com.casinowars.server.service.GameServiceException
import com.casinowars.server.service.GameServiceImpl
import com.google.inject.AbstractModule
import com.google.inject.Guice
import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Test class specific to war related bets
 */
@ExtendWith(VertxExtension::class)
class TestGameServiceWar
{
  private var gameService: GameService? = null
  private val deckManager: DeckManager = DeckManager()
  @Mock
  private val repository: Repository? = null

  @BeforeEach
  fun setup() {
    MockitoAnnotations.openMocks(this)
    val injector = Guice.createInjector(object : AbstractModule() {
      override fun configure() {
        bind(Configuration::class.java).toInstance(Configuration(ServerConfiguration(0, 50), DbConfiguration("", 0, "", "", "", 0), ""))
        bind(DeckManager::class.java).toInstance(deckManager)
        bind(Repository::class.java).toInstance(repository)
        bind(GameService::class.java).to(GameServiceImpl::class.java)
      }
    })
    gameService =  injector.getInstance(GameService::class.java)
  }

  @Test
  fun testStartWarBet(vertx: Vertx, testContext: VertxTestContext) {
    val testGameData = GameData(listOf(1, 1, 10, 2, 10, 2), 0, 0)
    val testPlayerData = PlayerData(UUID.randomUUID(), "testUser", 880)
    Mockito.`when`(repository!!.getGame(any())).thenReturn(futureSuccess(testGameData))
    Mockito.`when`(repository.getPlayer(any())).thenReturn(futureSuccess(testPlayerData))
    Mockito.`when`(repository.updatePlayer(any(), any(), any())).thenReturn(futureSuccess(null))
    Mockito.`when`(repository.updateGame(any(), any(), any(), any(), any())).thenReturn(futureSuccess(null))
    val sessionId = UUID.randomUUID()
    val betValue = 1
    gameService!!.requestBet(sessionId, BetRequest(betValue, 0, testPlayerData.id)).onComplete {
      assertTrue(it.succeeded(), "Request should succeed")
      val response = it.result()
      assertEquals(response.result!!, BetResult.war)
      assertEquals(response.tokenValue!!, betValue * -1)
      assertEquals(response.playerCard!!, deckManager.indexToCard(testGameData.deck[0]))
      assertEquals(response.casinoCard!!, deckManager.indexToCard(testGameData.deck[1]))
      testContext.completeNow()
    }
  }

  @Test
  fun testInvalidWarBet(vertx: Vertx, testContext: VertxTestContext) {
    // war is initiated in this turn, player can bet 10 or 0
    val previousWarBet = 10
    val testGameData = GameData(listOf(1, 1, 10, 2, 10, 2), 0, previousWarBet)
    val testPlayerData = PlayerData(UUID.randomUUID(), "testUser", 880)
    Mockito.`when`(repository!!.getGame(any())).thenReturn(futureSuccess(testGameData))
    Mockito.`when`(repository.getPlayer(any())).thenReturn(futureSuccess(testPlayerData))
    Mockito.`when`(repository.updatePlayer(any(), any(), any())).thenReturn(futureSuccess(null))
    Mockito.`when`(repository.updateGame(any(), any(), any(), any(), any())).thenReturn(futureSuccess(null))
    val sessionId = UUID.randomUUID()
    val betValue = 1
    gameService!!.requestBet(sessionId, BetRequest(betValue, 0, testPlayerData.id)).onComplete {
      assertTrue(it.failed(), "Request should fail")
      assertTrue(it.cause() is GameServiceException)
      assertEquals((it.cause() as GameServiceException).statusCode, 409)
      assertTrue((it.cause() as GameServiceException).message!!.contains("$previousWarBet"))
      testContext.completeNow()
    }
  }

  @Test
  fun testForfeitWarBet(vertx: Vertx, testContext: VertxTestContext) {
    // war is initiated in this turn, player can bet 10 or 0
    val previousWarBet = 10
    val testGameData = GameData(listOf(1, 1, 10, 2, 10, 2), 0, previousWarBet)
    val testPlayerData = PlayerData(UUID.randomUUID(), "testUser", 880)
    Mockito.`when`(repository!!.getGame(any())).thenReturn(futureSuccess(testGameData))
    Mockito.`when`(repository.getPlayer(any())).thenReturn(futureSuccess(testPlayerData))
    Mockito.`when`(repository.updatePlayer(any(), any(), any())).thenReturn(futureSuccess(null))
    Mockito.`when`(repository.updateGame(any(), any(), any(), any(), any())).thenReturn(futureSuccess(null))
    val sessionId = UUID.randomUUID()
    val betValue = 0
    gameService!!.requestBet(sessionId, BetRequest(betValue, 0, testPlayerData.id)).onComplete {
      assertTrue(it.succeeded(), "Request should succeed")
      val response = it.result()
      assertEquals(response.result!!, BetResult.surrender)
      assertEquals(response.tokenValue!!, previousWarBet / 2)
      testContext.completeNow()
    }
  }

  @Test
  fun testGoToWarLoosingBet(vertx: Vertx, testContext: VertxTestContext) {
    // war is initiated in this turn, player can bet 10 or 0
    val previousWarBet = 10
    val playerCard = 1
    val houseCard = 2
    val testGameData = GameData(listOf(-1, -1, -1, playerCard, houseCard, 4, 10), 0, previousWarBet)
    val testPlayerData = PlayerData(UUID.randomUUID(), "testUser", 880)
    Mockito.`when`(repository!!.getGame(any())).thenReturn(futureSuccess(testGameData))
    Mockito.`when`(repository.getPlayer(any())).thenReturn(futureSuccess(testPlayerData))
    Mockito.`when`(repository.updatePlayer(any(), any(), any())).thenReturn(futureSuccess(null))
    Mockito.`when`(repository.updateGame(any(), any(), any(), any(), any())).thenReturn(futureSuccess(null))
    val sessionId = UUID.randomUUID()
    gameService!!.requestBet(sessionId, BetRequest(previousWarBet, 0, testPlayerData.id)).onComplete {
      assertTrue(it.succeeded(), "Request should succeed")
      val response = it.result()
      assertEquals(response.result!!, BetResult.loss)
      assertEquals(response.playerCard!!.id, playerCard)
      assertEquals(response.casinoCard!!.id, houseCard)
      assertEquals(response.tokenValue!!, previousWarBet * -1)
      testContext.completeNow()
    }
  }

  @Test
  fun testGoToWarWinningBet(vertx: Vertx, testContext: VertxTestContext) {
    val previousWarBet = 10
    val playerCard = 2
    val houseCard = 1
    val testGameData = GameData(listOf(-1, -1, -1, playerCard, houseCard, 4, 10), 0, previousWarBet)
    val testPlayerData = PlayerData(UUID.randomUUID(), "testUser", 880)
    Mockito.`when`(repository!!.getGame(any())).thenReturn(futureSuccess(testGameData))
    Mockito.`when`(repository.getPlayer(any())).thenReturn(futureSuccess(testPlayerData))
    Mockito.`when`(repository.updatePlayer(any(), any(), any())).thenReturn(futureSuccess(null))
    Mockito.`when`(repository.updateGame(any(), any(), any(), any(), any())).thenReturn(futureSuccess(null))
    val sessionId = UUID.randomUUID()
    gameService!!.requestBet(sessionId, BetRequest(previousWarBet, 0, testPlayerData.id)).onComplete {
      assertTrue(it.succeeded(), "Request should succeed")
      val response = it.result()
      assertEquals(response.result!!, BetResult.win)
      assertEquals(response.playerCard!!.id, playerCard)
      assertEquals(response.casinoCard!!.id, houseCard)
      assertEquals(response.tokenValue!!, previousWarBet)
      testContext.completeNow()
    }
  }

  @Test
  fun testGoToWarTieWin(vertx: Vertx, testContext: VertxTestContext) {
    val previousWarBet = 10
    val playerCard = 1
    val houseCard = 1
    val testGameData = GameData(listOf(-1, -1, -1, playerCard, houseCard, 4, 10), 0, previousWarBet)
    val testPlayerData = PlayerData(UUID.randomUUID(), "testUser", 880)
    Mockito.`when`(repository!!.getGame(any())).thenReturn(futureSuccess(testGameData))
    Mockito.`when`(repository.getPlayer(any())).thenReturn(futureSuccess(testPlayerData))
    Mockito.`when`(repository.updatePlayer(any(), any(), any())).thenReturn(futureSuccess(null))
    Mockito.`when`(repository.updateGame(any(), any(), any(), any(), any())).thenReturn(futureSuccess(null))
    val sessionId = UUID.randomUUID()
    gameService!!.requestBet(sessionId, BetRequest(previousWarBet, 0, testPlayerData.id)).onComplete {
      assertTrue(it.succeeded(), "Request should succeed")
      val response = it.result()
      assertEquals(response.result!!, BetResult.tiewin)
      assertEquals(response.playerCard!!.id, playerCard)
      assertEquals(response.casinoCard!!.id, houseCard)
      assertEquals(response.tokenValue!!, previousWarBet * 2)
      testContext.completeNow()
    }
  }
}
