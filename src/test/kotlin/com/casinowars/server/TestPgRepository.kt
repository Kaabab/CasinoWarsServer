package com.casinowars.server

import com.casinowars.server.deck.DeckManager
import com.casinowars.server.repository.Repository
import com.casinowars.server.repository.RepositoryPgImpl
import com.casinowars.server.repository.model.GameData
import com.google.inject.AbstractModule
import com.google.inject.Guice
import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.sqlclient.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import java.util.*
import kotlin.test.assertTrue

/**
 * Example test class for PgRepository
 */
@ExtendWith(VertxExtension::class)
class TestPgRepository {
  private val INSERT_GAME = "INSERT INTO game VALUES ($1, $2, $3, $4, $5)"

  private val deckManager: DeckManager = DeckManager()

  @Mock
  private val sqlClient: SqlClient? = null
  @Mock
  private val query: PreparedQuery<RowSet<Row>>? = null

  @Mock
  private val result:RowSet<Row>? = null

  private var repository:Repository? = null

  @BeforeEach
  fun setup() {
    MockitoAnnotations.openMocks(this)
    val injector = Guice.createInjector(object : AbstractModule() {
      override fun configure() {
        bind(SqlClient::class.java).toInstance(sqlClient)
        bind(Repository::class.java).to(RepositoryPgImpl::class.java)

      }
    })
    repository =  injector.getInstance(Repository::class.java)
  }

  @Test
  @kotlin.Throws(Throwable::class)
  fun testCreateGame(vertx: Vertx?, testContext: VertxTestContext) {
    val testGameData = GameData(deckManager.generateShuffledDeck(), 8, 880)
    Mockito.`when`(sqlClient!!.preparedQuery(any())).thenReturn(query)
    Mockito.`when`(query!!.execute(any<Tuple>())).thenReturn(io.vertx.core.impl.future.SucceededFuture(result))
    val gameId = UUID.randomUUID()
    val playerId = UUID.randomUUID()
    repository!!.createGame(gameId, playerId, testGameData.deck).onComplete {
      assertTrue(it.succeeded(), "Request should succeed")
      Mockito.verify(sqlClient).preparedQuery(INSERT_GAME)
      Mockito.verify(query).execute(any<Tuple>())
      testContext.completeNow()
    }
  }
}
