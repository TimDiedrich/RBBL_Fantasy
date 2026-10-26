package com.timdiedrich.rbblfantasy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.toRoute
import com.timdiedrich.rbblfantasy.ui.screens.DashboardViewModel
import com.timdiedrich.rbblfantasy.ui.screens.DashboardScreen
import com.timdiedrich.rbblfantasy.ui.screens.MatchesViewModel
import com.timdiedrich.rbblfantasy.ui.screens.MatchesScreen
import com.timdiedrich.rbblfantasy.ui.screens.StandingsViewModel
import com.timdiedrich.rbblfantasy.ui.screens.StandingsScreen
import com.timdiedrich.rbblfantasy.ui.screens.LoginScreen
import com.timdiedrich.rbblfantasy.ui.screens.CreateAccountScreen
import com.timdiedrich.rbblfantasy.ui.screens.LeagueSelectionScreen
import com.timdiedrich.rbblfantasy.ui.screens.SettingsScreen
import com.timdiedrich.rbblfantasy.ui.screens.SettingsViewModel
import com.timdiedrich.rbblfantasy.ui.screens.ScoutingScreen
import com.timdiedrich.rbblfantasy.ui.screens.CreateTeamScreen
import com.timdiedrich.rbblfantasy.ui.screens.CreateTeamViewModel
import com.timdiedrich.rbblfantasy.ui.screens.ScoutingViewModel
import com.timdiedrich.rbblfantasy.ui.screens.TeamSelectionViewModel
import com.timdiedrich.rbblfantasy.ui.screens.TeamSelectionScreen
import com.timdiedrich.rbblfantasy.ui.screens.LeagueSettingsScreen

@Composable
fun RbblNavGraph(
    navController: NavHostController,
    startDestination: Destination,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable<Destination.Login> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Destination.TeamSelection) {
                        popUpTo<Destination.Login> { inclusive = true }
                    }
                },
                onCreateAccountClick = {
                    navController.navigate(Destination.CreateAccount)
                }
            )
        }
        composable<Destination.CreateAccount> {
            CreateAccountScreen(
                onAccountCreated = {
                    navController.navigate(Destination.TeamSelection) {
                        popUpTo<Destination.Login> { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable<Destination.LeagueSelection> {
            LeagueSelectionScreen(
                onLeagueJoined = { leagueId ->
                    navController.navigate(Destination.CreateTeam(leagueId)) {
                        popUpTo<Destination.LeagueSelection> { inclusive = true }
                    }
                }
            )
        }
        composable<Destination.TeamSelection> {
            val teamSelectionViewModel: TeamSelectionViewModel = viewModel(factory = TeamSelectionViewModel.Factory)
            TeamSelectionScreen(
                viewModel = teamSelectionViewModel,
                onTeamSelected = {
                    navController.navigate(Destination.Dashboard) {
                        popUpTo<Destination.TeamSelection> { inclusive = true }
                    }
                },
                onSelectLeagueClick = {
                    navController.navigate(Destination.LeagueSelection) {
                        popUpTo<Destination.CreateTeam> { inclusive = true }
                    }
                }
            )
        }
        composable<Destination.Dashboard> {
            val dashboardViewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToDraft = { leagueId, userTeamId, jwtToken, serverUrl ->
                    navController.navigate(Destination.DraftRoom(
                        leagueId.toString(),
                        userTeamId,
                        jwtToken,
                        serverUrl
                        ))
                }
            )
        }
        composable<Destination.DraftRoom> { backStackEntry ->
            val draftRoomDestination: Destination.DraftRoom = backStackEntry.toRoute()
            // We might need to get token and teamId from session, but let's start with basic navigation
            com.timdiedrich.rbblfantasy.ui.screens.DraftRoomScreen(
                leagueId = draftRoomDestination.leagueId,
                userTeamId = draftRoomDestination.userTeamId,
                jwtToken = draftRoomDestination.jwtToken,
                serverUrl = draftRoomDestination.serverUrl

            )
        }
        composable<Destination.CreateTeam> { backStackEntry ->
            val createTeamDestination: Destination.CreateTeam = backStackEntry.toRoute()
            val createTeamViewModel: CreateTeamViewModel = viewModel(factory = CreateTeamViewModel.Factory)
            
            LaunchedEffect(createTeamDestination.leagueId) {
                createTeamViewModel.setLeagueId(createTeamDestination.leagueId)
            }

            CreateTeamScreen(
                onTeamCreated = {
                    navController.navigate(Destination.Dashboard) {
                        popUpTo<Destination.CreateTeam> { inclusive = true }
                    }
                },
                viewModel = createTeamViewModel
            )
        }
        composable<Destination.Scouting> {
            // Note: If Scouting needs the same instance as Dashboard, 
            // you might need to scope the ViewModel to the NavGraph or Activity.
            val scoutingViewModel: ScoutingViewModel = viewModel(factory = ScoutingViewModel.Factory)
            ScoutingScreen(scoutingViewModel)
        }
        composable<Destination.Matches> {
            val matchesViewModel: MatchesViewModel = viewModel(factory = MatchesViewModel.Factory)
            MatchesScreen(matchesViewModel)
        }
        composable<Destination.Standings> {
            val standingsViewModel: StandingsViewModel = viewModel(factory = StandingsViewModel.Factory)
            StandingsScreen(standingsViewModel)
        }
        composable<Destination.Settings> {
            val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
            SettingsScreen(
                onNavigateToLeagueSettings = { leagueId, isAdmin ->
                    navController.navigate(Destination.LeagueSettings(leagueId, isAdmin))
                },
                onNavigateToLeagueSelection = {
                    navController.navigate(Destination.LeagueSelection) {
                        popUpTo(Destination.Dashboard) { inclusive = false }
                    }
                },
                onNavigateToTeamSelection = {
                    navController.navigate(Destination.TeamSelection) {
                        popUpTo(Destination.Dashboard) { inclusive = false }
                    }
                },
                viewModel = settingsViewModel
            )
        }
        composable<Destination.LeagueSettings> { backStackEntry ->
            val leagueSettingsDestination: Destination.LeagueSettings = backStackEntry.toRoute()
            LeagueSettingsScreen(
                leagueId = leagueSettingsDestination.leagueId,
                isAdmin = leagueSettingsDestination.isAdmin,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

