package com.timdiedrich.rbblfantasy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.timdiedrich.rbblfantasy.ui.theme.RBBLFantasyTheme

@Composable
fun LeagueSelectionScreen(
    onLeagueJoined: (Int) -> Unit,
    viewModel: LeagueSelectionViewModel = viewModel(factory = LeagueSelectionViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.joinedLeagueId) {
        uiState.joinedLeagueId?.let { leagueId ->
            onLeagueJoined(leagueId)
        }
    }

    LeagueSelectionContent(
        uiState = uiState,
        onCreateLeague = { name, count -> viewModel.createLeague(name, count) },
        onJoinLeague = { code -> viewModel.joinLeague(code) }
    )
}

@Composable
fun LeagueSelectionContent(
    uiState: LeagueSelectionUiState,
    onCreateLeague: (String, Int) -> Unit,
    onJoinLeague: (String) -> Unit
) {
    var leagueName by remember { mutableStateOf("") }
    var inviteCode by remember { mutableStateOf("") }
    var selectedTeamCount by remember { mutableIntStateOf(4) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to RBBL Fantasy!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Create a new league or join an existing one to get started.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Create League Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Create a League", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = leagueName,
                    onValueChange = { leagueName = it },
                    label = { Text("League Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Max Teams", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(4, 6, 8).forEach { count ->
                        FilterChip(
                            selected = selectedTeamCount == count,
                            onClick = { selectedTeamCount = count },
                            label = { Text("$count Teams") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onCreateLeague(leagueName, selectedTeamCount) },
                    modifier = Modifier.align(Alignment.End),
                    enabled = !uiState.isLoading
                ) {
                    Text("Create")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Text(text = "OR", style = MaterialTheme.typography.labelLarge)
        
        Spacer(modifier = Modifier.height(24.dp))

        // Join League Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Join a League", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = inviteCode,
                    onValueChange = { inviteCode = it },
                    label = { Text("Invite Code") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onJoinLeague(inviteCode) },
                    modifier = Modifier.align(Alignment.End),
                    enabled = !uiState.isLoading
                ) {
                    Text("Join")
                }
            }
        }

        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (uiState.isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LeagueSelectionPreview() {
    RBBLFantasyTheme {
        LeagueSelectionContent(
            uiState = LeagueSelectionUiState(),
            onCreateLeague = { _, _ -> },
            onJoinLeague = { }
        )
    }
}
