package com.timdiedrich.rbblfantasy.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class FantasyLeague(
    @Json(name = "id") val id: Int,
    @Json(name = "league_name") val name: String,
    @Json(name = "commissioner_id") val ownerId: Int,
    @Json(name = "max_number_teams") val maxNumberTeams: Int,
    @Json(name = "invite_code") val inviteCode: String?,
    @Json(name = "draft_status") val draftStatus: DraftStatus,
    @Json(name = "draft_date") val draftDate: String?,
    @Json(name = "created_at") val createdAt: String,
)

@JsonClass(generateAdapter = true)
data class CreateLeagueRequest(
    @Json(name = "name") val name: String,
    @Json(name = "numOfTeams") val maxNumberTeams: Int,
)
@JsonClass(generateAdapter = true)
data class GetLeaguesResponse(
    @Json(name = "leagues") val fantasyLeagues: List<FantasyLeague>
)

@JsonClass(generateAdapter = true)
data class UpdateLeagueRequest(
    @Json(name = "league_name") val name: String,
    @Json(name = "draft_status") val draftStatus: DraftStatus?,
    @Json(name = "draft_date") val draftDate: String?
)

@JsonClass(generateAdapter = true)
data class JoinLeagueRequest(
    @Json(name = "invite_code") val inviteCode: String
)

@JsonClass(generateAdapter = true)
data class JoinLeagueResponse(
    @Json(name = "message") val message: String,
    @Json(name = "league") val fantasyLeague: FantasyLeague
)

@JsonClass(generateAdapter = true)
data class LeagueMembership(
    @Json(name = "id") val id: Int,
    @Json(name = "league_id") val leagueId: Int,
    @Json(name = "user_id") val userId: Int,
    @Json(name = "role") val role: Role,
    @Json(name = "joined_at") val createdAt: String,
    @Json(name = "league_name") val leagueName: String,
    @Json(name = "fantasy_team_name") val fantasyTeamName: String,
)

enum class Role {
    COMMISSIONER, MEMBER, ADMIN
}

enum class DraftStatus {
    PREDRAFT, SCHEDULED, DRAFTING, POSTDRAFT, COMPLETED
}