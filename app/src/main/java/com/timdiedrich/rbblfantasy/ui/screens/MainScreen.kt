package com.timdiedrich.rbblfantasy.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Leaderboard
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SportsBasketball
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.timdiedrich.rbblfantasy.ui.navigation.Destination
import com.timdiedrich.rbblfantasy.ui.navigation.RbblNavGraph

data class NavItem(
    val label: String,
    val icon: ImageVector,
    val destination: Destination
)

val navItems = listOf(
    NavItem("Dashboard", Icons.Rounded.Dashboard, Destination.Dashboard),
    NavItem("Scouting", Icons.Rounded.Search, Destination.Scouting),
    NavItem("Matches", Icons.Rounded.SportsBasketball, Destination.Matches),
    NavItem("Standings", Icons.Rounded.Leaderboard, Destination.Standings),
    NavItem("Settings", Icons.Rounded.Settings, Destination.Settings)
)

@Composable
fun MainScreen(
    startDestination: Destination,
    mainViewModel: MainViewModel
) {
    val navController = rememberNavController()
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is SplashUiState.LoggedOut) {
            navController.navigate(Destination.Login) {
                popUpTo(navController.graph.id) {
                    inclusive = true
                }
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = currentDestination?.hierarchy?.any {
        it.hasRoute<Destination.Login>() || 
        it.hasRoute<Destination.CreateAccount>() || 
        it.hasRoute<Destination.LeagueSelection>() ||
        it.hasRoute<Destination.CreateTeam>() ||
        it.hasRoute<Destination.TeamSelection>() ||
        it.hasRoute<Destination.LeagueSettings>()
    } != true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    navItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.hasRoute(item.destination::class) } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.destination) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        RbblNavGraph(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
