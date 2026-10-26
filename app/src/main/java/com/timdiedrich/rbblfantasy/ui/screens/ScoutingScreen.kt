package com.timdiedrich.rbblfantasy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.timdiedrich.rbblfantasy.data.model.FantasyPlayer
import com.timdiedrich.rbblfantasy.ui.theme.RBBLFantasyTheme

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.*
import com.timdiedrich.rbblfantasy.ui.components.PlayerAvatar

@Composable
fun ScoutingScreen(viewModel: ScoutingViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedFantasyPlayer by remember { mutableStateOf<FantasyPlayer?>(null) }
    var tabSelected by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Player Scouting",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text("Search players...") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            shape = MaterialTheme.shapes.medium
        )

        TabRow(selectedTabIndex = if (tabSelected) 0 else 1) {
            Tab(
                selected = tabSelected,
                onClick = { tabSelected = true },
                text = { Text("All Players") }
            )
            Tab(
                selected = !tabSelected,
                onClick = { tabSelected = false },
                text = { Text("Undrafted Players") }
            )

        }

        val displayPlayers = if (tabSelected) uiState.filteredFantasyPlayers else uiState.filteredUndrafted

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = displayPlayers,
                key = { it.id } // Use ID instead of name to ensure uniqueness
            ) { player ->
                //val isInRoster = uiState.undrafted.any { it.name == player.name }
                PlayerScoutingItem(
                    fantasyPlayer = player,
                    isInRoster = tabSelected, // only undrafted can be added to roster
                    onAdd = { viewModel.addToRoster(player) },
                    onClick = { selectedFantasyPlayer = player }
                )
            }
        }
    }

    if (selectedFantasyPlayer != null) {
        PlayerDetailsDialog(
            fantasyPlayer = selectedFantasyPlayer!!,
            onDismiss = { selectedFantasyPlayer = null }
        )
    }
}

@Composable
fun PlayerScoutingItem(fantasyPlayer: FantasyPlayer, isInRoster: Boolean, onAdd: () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PlayerAvatar(
                imageUrl = fantasyPlayer.imageUrl,
                playerName = fantasyPlayer.name,
                nationality = fantasyPlayer.nationality,
                modifier = Modifier
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = fantasyPlayer.name, fontWeight = FontWeight.Bold)
                Text(text = fantasyPlayer.gender, style = MaterialTheme.typography.bodySmall)
                if (fantasyPlayer.bonus > 0) Text(text = "Class: ${fantasyPlayer.sportClass} • Bonus: ${fantasyPlayer.bonus}", style = MaterialTheme.typography.labelSmall)
                else Text(text = "Class: ${fantasyPlayer.sportClass}", style = MaterialTheme.typography.labelSmall)

                }
            if (isInRoster) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = "Added",
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                IconButton(onClick = onAdd) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add to Roster")
                }
            }
        }
    }
}

@Composable
fun PlayerDetailsDialog(fantasyPlayer: FantasyPlayer, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
            PlayerAvatar(
                imageUrl = fantasyPlayer.imageUrl,
                playerName = fantasyPlayer.name,
                nationality = fantasyPlayer.nationality,
                size = 96.dp
            )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = fantasyPlayer.name, style = MaterialTheme.typography.titleLarge)
                    Text(text = fantasyPlayer.gender, style = MaterialTheme.typography.bodyMedium)
                    if (fantasyPlayer.bonus > 0) Text(text = "Class: ${fantasyPlayer.sportClass} • Bonus: ${fantasyPlayer.bonus}", style = MaterialTheme.typography.labelSmall)
                    else Text(text = "Class: ${fantasyPlayer.sportClass}", style = MaterialTheme.typography.labelSmall)
                    Text(text = "Nationality: ${fantasyPlayer.nationality}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Born: ${fantasyPlayer.dob}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Position: ${fantasyPlayer.position}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
    )
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Preview
@Composable
fun ScoutingScreenPreview() {
    RBBLFantasyTheme {

        // val viewModel = DashboardViewModel()
        // ScoutingScreen(viewModel)
    }
}
