package com.timdiedrich.rbblfantasy.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GetRealMatchesResponse(
    @Json(name = "week_number") val weekNumber: Int,
    @Json(name = "matches") val matches: List<Match>
)
@JsonClass(generateAdapter = true)
data class Match(
    @Json(name = "id") val id: Int,
    @Json(name = "home_team_name") val homeTeamName: String, // currently the number to the website identifier
    @Json(name = "away_team_name") val awayTeamName: String, // currently the number to the website identifier
    @Json(name = "home_score") val homeScore: Int?,
    @Json(name = "away_score") val awayScore: Int?,
    @Json(name = "home_logo_url") val homeLogoUrl: String,
    @Json(name = "away_logo_url") val awayLogoUrl: String,
    @Json(name = "game_date") val date: String,
    @Json(name = "status") val status: MatchStatus // to be determined by backend
    // maybe more categories link to box score or something
)

enum class MatchStatus {
    LIVE, FINISHED, SCHEDULED
}
