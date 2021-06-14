package com.casinowars.server.repository.model

/**
 * Represents the data needed to evaluate current gameplay progress
 * Each game contain 52 rounds at max, a game at round 52 is considered complete
 * When the table has tokens left on it, war is in progress, they should be doubled up to go to war or half of them lost with a zero bet
 */
data class GameData(
  // represents the current randomization of the deck
  val deck: List<Int>,
  val topCardIndex: Int,
  // represents the amount of token left on the table from the previous turn
  val tableTokens: Int
)

