package com.timdiedrich.rbblfantasy.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GetRosterResponse(
    @Json(name = "roster") val roster: List<Roster>
)

@JsonClass(generateAdapter = true)
data class Roster(
    @Json(name = "id") val playerId: Int,
    @Json(name = "shirt_number") val shirtNumber: Int,
    @Json(name = "player_name") val playerName: String,
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "dob") val dob: String? = null,
    @Json(name = "nationality") val nationality: String? = null,
    @Json(name = "position") val position: String? = null,
    @Json(name = "sport_class") val sportClass: Float,
    @Json(name = "gender") val gender: String,
    @Json(name = "bonus") val bonus: Float,
    @Json(name = "real_team_id") val teamId: String,
    @Json(name= "is_starter") val isStarter: Boolean,
    @Json(name = "roster_id") val rosterId: Int,
)

@JsonClass(generateAdapter = true)
data class GetUndraftedPlayersResponse(
    @Json(name = "players") val fantasyPlayers: List<FantasyPlayer>
)

@JsonClass(generateAdapter = true)
data class AddToRosterRequest(
    @Json(name = "id") val rosterId: Int,
    @Json(name = "fantasy_team_id") val fantasyTeamId: Int,
    @Json(name = "player_id") val playerId: Int,
    @Json(name = "is_starter") val isStarter: Boolean
)

@JsonClass(generateAdapter = true)
data class AddToRosterResponse(
    @Json(name = "message") val message: String
)