package com.timdiedrich.rbblfantasy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.timdiedrich.rbblfantasy.data.model.Standing

@Composable
fun StandingsScreen(viewModel: StandingsViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "League Standings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TabRow(selectedTabIndex = if (uiState.isGlobal) 0 else 1) {
            Tab(
                selected = uiState.isGlobal,
                onClick = { viewModel.toggleLeague(true) },
                text = { Text("Global") }
            )
            Tab(
                selected = !uiState.isGlobal,
                onClick = { viewModel.toggleLeague(false) },
                text = { Text(uiState.privateLeague.takeIf { it.isNotBlank() } ?: "Your League") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val currentStandings = if (uiState.isGlobal) uiState.globalStandings else uiState.privateStandings

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                HeaderRow()
            }
            itemsIndexed(currentStandings) { _, standing ->
                StandingRow(rank = standing.rank, standing = standing)
            }
        }
    }
}

@Composable
fun HeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "#", modifier = Modifier.width(30.dp), fontWeight = FontWeight.Bold)
        Text(text = "Team", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        Text(text = "P", modifier = Modifier.width(40.dp), fontWeight = FontWeight.Bold)
        Text(text = "W/L", modifier = Modifier.width(60.dp), fontWeight = FontWeight.Bold)
        Text(text = "Pts", modifier = Modifier.width(50.dp), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StandingRow(rank: Int, standing: Standing) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (rank == 1) MaterialTheme.colorScheme.primaryContainer 
                             else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = rank.toString(), modifier = Modifier.width(30.dp), fontWeight = FontWeight.Bold)
            Text(text = standing.teamName, modifier = Modifier.weight(1f))
            Text(text = (standing.wins + standing.losses).toString(), modifier = Modifier.width(40.dp))
            Text(text = "${standing.wins}/${standing.losses}", modifier = Modifier.width(60.dp))
            Text(text = standing.points.toString(), modifier = Modifier.width(50.dp), fontWeight = FontWeight.Bold)
        }
    }
}

@Preview
@Composable
fun StandingsScreenPreview() {
    //val viewModel = StandingsViewModel()
    //StandingsScreen(viewModel)
}
