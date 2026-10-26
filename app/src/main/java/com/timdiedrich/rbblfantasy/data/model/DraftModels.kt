package com.timdiedrich.rbblfantasy.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


data class DraftPlayer(
    val id: String,
    val name: String,
    val sportClass: Double,
    val bonus: Double = 0.0,
    val position: String? = null,
    val isDrafted: Boolean = false
)

enum class DraftSortOrder {
    NAME, CLASS_ASC, CLASS_DESC
}

data class DraftTurnState(
    val currentRound: Int = 1,
    val currentPickNumber: Int = 1,
    val currentFantasyTeamId: String = "",
    val currentTeamName: String? = null,
    val pickDeadlineIso: String? = null,
    val timePerPickSeconds: Int = 90,
    val eligibleSportClasses: List<Double> = emptyList(),
    val status: String = "NOT_STARTED" // SCHEDULED, IN_PROGRESS, COMPLETED
)

@JsonClass(generateAdapter = true)
data class DraftTeamOrder(
    @Json(name = "league_id") val leagueId: Int,
    @Json(name = "fantasy_team_id") val fantasyTeamId: String,
    @Json(name = "team_name") val fantasyTeamName: String,
    @Json(name = "draft_position") val draftPosition: Int
)

data class DraftUiState(
    val isConnected: Boolean = false,
    val userTeamId: String = "",
    val turnState: DraftTurnState = DraftTurnState(),
    val draftOrder: List<DraftTeamOrder> = emptyList(),
    val availablePlayers: List<DraftPlayer> = emptyList(),
    val scheduledFor: String? = null,
    val errorMessage: String? = null
)