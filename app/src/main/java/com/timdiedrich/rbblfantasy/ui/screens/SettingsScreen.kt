package com.timdiedrich.rbblfantasy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToLeagueSettings: (leagueId: Int, isAdmin: Boolean) -> Unit,
    onNavigateToLeagueSelection: () -> Unit,
    onNavigateToTeamSelection: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.navigateToLeagueSelection) {
        if (uiState.navigateToLeagueSelection) {
            onNavigateToLeagueSelection()
            viewModel.onLeagueSelectionNavigated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        )
        {

            // --- Account Section ---
            Text(
                text = "Account",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.logout() }
            ) {
                ListItem(
                    headlineContent = { 
                        Text(
                            text = "Logout",
                            color = MaterialTheme.colorScheme.error
                        ) 
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Logout,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // --- Teams Section ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Teams",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = { onNavigateToTeamSelection() }) {
                    Text("Switch Team")
                }
            }

            if (uiState.myTeams.isEmpty()) {
                Text(
                    text = "No teams found",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                uiState.myTeams.forEach { team ->
                    val isActive = team.id == uiState.activeTeamId
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { viewModel.switchTeam(team) },
                        colors = if (isActive) {
                            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        } else {
                            CardDefaults.cardColors()
                        }
                    ) {
                        ListItem(
                            headlineContent = { 
                                Text(
                                    text = team.teamName,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                                ) 
                            },
                            trailingContent = {
                                if (isActive) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ) {
                                        Text("Active", modifier = Modifier.padding(horizontal = 4.dp))
                                    }
                                }
                            },
                            colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // --- Leagues Section ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Leagues",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = { viewModel.toLeagueSelection() }) {
                    Text("Join/Create New")
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                android.util.Log.d("SettingsScreen", "activeLeagueId: ${uiState.activeLeagueId}, myLeagues: ${uiState.myLeagues}")
                val activeLeague = uiState.myLeagues.find { it.id == uiState.activeLeagueId }
                android.util.Log.d("SettingsScreen", "Found activeLeague: $activeLeague")
                if (activeLeague == null) {
                    Text(
                        text = "No active league found",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (activeLeague.isCommissioner) {
                                onNavigateToLeagueSettings(activeLeague.id,true)
                            }
                        }
                    ) {
                        ListItem(
                            headlineContent = { Text(activeLeague.leagueName) },
                            supportingContent = {
                                if (activeLeague.isCommissioner) {
                                    Text(
                                        text = "Commissioner",
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            },
                            trailingContent = {
                                if (activeLeague.isCommissioner) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Manage",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Rounded.Settings,
                                            contentDescription = "Manage",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            },
                            colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                        )
                    }
                }
            }

            HorizontalDivider()

            Text(
                text = "App Info",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}
