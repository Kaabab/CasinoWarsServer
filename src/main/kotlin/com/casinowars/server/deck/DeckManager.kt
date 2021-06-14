package com.casinowars.server.deck

import com.casinowars.server.api.models.Card
import kotlin.random.Random

/**
 * Helper class providing game specific Deck functionalities
 */
class DeckManager {
  private val cards: List<Card>
  private val ids: List<Int>
  private val random: Random = Random(System.currentTimeMillis())

  init {
    cards = generateStandardIndexedDeck()
    ids = cards.mapNotNull(Card::id).toList()
  }

  private fun generateStandardIndexedDeck(): List<Card> {
    val cards = mutableListOf<Card>()
    Suites.values().forEach { suite ->
      Ranks.values().forEach { rank ->
        val id = suite.ordinal * Ranks.values().count() + rank.ordinal
        val name = "${rank.name.capitalize()} of ${suite.name.capitalize()}"
        val value = rank.ordinal
        val card = Card(id, value, name)
        cards.add(card)
      }
    }
    return cards.toList()
  }

  fun generateShuffledDeck(): List<Int> {
    return ids.shuffled(random)
  }

  fun indexToCard(cardIndex: Int): Card {
    return cards[cardIndex]
  }


}
