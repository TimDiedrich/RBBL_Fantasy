package com.timdiedrich.rbblfantasy.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateTeamRequest(
    @Json(name = "league_id") val leagueId: Int,
    @Json(name = "team_name") val teamName: String
)
