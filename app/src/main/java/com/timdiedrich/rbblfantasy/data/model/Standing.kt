package com.timdiedrich.rbblfantasy.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GetStandingsResponse(
    @Json(name = "standings") val standings: List<Standing>
)

@JsonClass(generateAdapter = true)
data class Standing(
    @Json(name = "team_id") val teamId: Int,
    @Json(name = "team_name") val teamName: String,
    @Json(name = "wins") val wins: Int,
    @Json(name = "losses") val losses: Int,
    @Json(name = "total_points") val points: Int,
    @Json(name = "rank") val rank: Int,
)
