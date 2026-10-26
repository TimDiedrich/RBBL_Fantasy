package com.timdiedrich.rbblfantasy.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.sql.Date

@JsonClass(generateAdapter = true)
data class FantasyPlayer(
    @Json(name = "id") val id: Int,
    @Json(name = "league_id") val leagueId: Int? = null,
    @Json(name = "original_player_id") val originalPlayerId: Int,
    @Json(name = "shirt_number") val shirtNumber: Int,
    @Json(name = "player_name") val name: String,
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "dob") val dob: String? = null,
    @Json(name = "nationality") val nationality: String? = null,
    @Json(name = "position") val position: String? = null,
    @Json(name = "sport_class") val sportClass: Float,
    @Json(name = "gender") val gender: String,
    @Json(name = "bonus") val bonus: Float,
    @Json(name = "team_id") val teamId: String,
    @Json(name = "is_active") val isActive: Boolean,
    @Json(name = "is_drafted") val isDrafted: Boolean,
    @Json(name = "drafted_by_team_id") val draftedByTeamId: Int? = null,
    )