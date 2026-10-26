package com.timdiedrich.rbblfantasy.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LeagueAdminSettings(
    @Json(name = "league_id") val leagueId: Int,
    @Json(name = "max_lineup_points") val maxLineupPoints: Float,
    @Json(name = "max_roster_size") val maxRosterSize: Int,
    @Json(name = "points_per_real_point") val pointsPerRealPoint: Float,
    @Json(name = "points_per_rebound") val pointsPerRebound: Float,
    @Json(name = "points_per_assist") val pointsPerAssist: Float,
    @Json(name = "points_per_steal") val pointsPerSteal: Float,
    @Json(name = "points_per_block") val pointsPerBlock: Float,
    @Json(name = "points_per_turnover") val pointsPerTurnover: Float,
    @Json(name = "double_double_bonus") val doubleDoubleBonus: Float,
    @Json(name = "triple_double_bonus") val tripleDoubleBonus: Float,
    @Json(name = "three_point_made") val threePointMade: Float,
)