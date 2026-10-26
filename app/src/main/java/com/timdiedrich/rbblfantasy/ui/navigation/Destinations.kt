package com.timdiedrich.rbblfantasy.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination {
    @Serializable
    data class CreateTeam(val leagueId: Int) : Destination
    @Serializable
    data object Login : Destination

    @Serializable
    data object CreateAccount : Destination

    @Serializable
    data object Dashboard : Destination

    @Serializable
    data object Scouting : Destination

    @Serializable
    data object Matches : Destination

    @Serializable
    data object Standings : Destination

    @Serializable
    data object LeagueSelection : Destination

    @Serializable
    data object TeamSelection : Destination

    @Serializable
    data object Settings : Destination

    @Serializable
    data class DraftRoom(val leagueId: String,
                         val userTeamId: String,
                         val jwtToken: String,
                         val serverUrl: String) : Destination

    @Serializable
    data class LeagueSettings(val leagueId: Int, val isAdmin: Boolean) : Destination
}
