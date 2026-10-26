package com.timdiedrich.rbblfantasy.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FantasyTeam (
    @Json(name = "id") val id: Int,
    @Json(name = "league_id") val leagueId: Int,
    @Json(name = "user_id") val userId: Int,
    @Json(name = "team_name") val teamName: String,
    @Json(name = "created_at") val createdAt: String,
)

@JsonClass(generateAdapter = true)
data class GetTeamsResponse(
    @Json(name = "teams") val team: FantasyTeam?
)

@JsonClass(generateAdapter = true)
data class GetAllTeamsResponse(
    @Json(name = "teams") val teams: List<FantasyTeam>
)