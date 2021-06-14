package com.casinowars.server.repository.model

import java.util.*

/**
 * Represents exposed stored player data
 */
data class PlayerData(val id: UUID, val name: String, val tokenCount: Int)
