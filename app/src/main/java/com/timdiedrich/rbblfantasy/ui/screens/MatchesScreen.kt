package com.timdiedrich.rbblfantasy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FiberManualRecord
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.timdiedrich.rbblfantasy.data.model.Match
import com.timdiedrich.rbblfantasy.data.model.MatchStatus
import com.timdiedrich.rbblfantasy.utils.formatMatchTime

@Composable
fun MatchesScreen(viewModel: MatchesViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Match Tracker",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            WeekSelector(
                weeks = uiState.availableWeeks,
                selectedWeek = uiState.selectedWeek,
                onWeekSelected = { viewModel.selectWeek(it) }
            )
        }

        TabRow(selectedTabIndex = uiState.selectedTab) {
            Tab(
                selected = uiState.selectedTab == 0,
                onClick = { viewModel.selectTab(0) },
                text = { Text("Fantasy") }
            )
            Tab(
                selected = uiState.selectedTab == 1,
                onClick = { viewModel.selectTab(1) },
                text = { Text("Real Games") }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.selectedTab == 0) {
                if (uiState.fantasyMatches.isEmpty()) {
                    item {
                        Text(text = "This week is a bye week",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                    }
                } else {
                    items(uiState.fantasyMatches) { match ->
                        FantasyMatchCard(match = match)
                    }
                }
            } else {
                if (uiState.realMatches.isEmpty()) {
                    item {
                        Text(text = "This week is a bye week",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                    }
                } else {
                    items(uiState.realMatches) { match ->
                        MatchCard(match = match)
                    }
                }

            }
        }
    }
}

@Composable
fun WeekSelector(
    weeks: List<Int>,
    selectedWeek: Int,
    onWeekSelected: (Int) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(weeks) { week ->
            val isSelected = week == selectedWeek
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onWeekSelected(week) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = week.toString(),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary 
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FantasyMatchCard(match: FantasyMatch) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            StatusIndicator(status = match.status)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TeamColumn(
                    name = match.homeTeamName,
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                )
                Text(
                    text = "VS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                TeamColumn(
                    name = match.awayTeamName,
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                )
            }
        }
    }
}

@Composable
fun MatchCard(match: Match) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusIndicator(status = match.status)
                Text(text = formatMatchTime(match.date), style = MaterialTheme.typography.labelSmall)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TeamColumn(
                    name = match.homeTeamName,
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                )
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = match.homeLogoUrl,
                        contentDescription = match.homeTeamName,
                        contentScale = ContentScale.Fit
                    )
                    Text(
                        text = match.homeScore?.toString() ?: "-",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "VS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = match.awayScore?.toString() ?: "-",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    AsyncImage(
                        model = match.awayLogoUrl,
                        contentDescription = match.awayTeamName,
                        contentScale = ContentScale.Fit
                    )
                }

                TeamColumn(
                    name = match.awayTeamName,
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                )
            }

            if (match.status == MatchStatus.LIVE || match.status == MatchStatus.FINISHED) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Top Performer:", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = "D. Nowitzki (25 pts, 10 reb)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun TeamColumn(
    name: String,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally
) {
    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = when (horizontalAlignment) {
                Alignment.Start -> TextAlign.Start
                Alignment.End -> TextAlign.End
                else -> TextAlign.Center
            }
        )
    }
}

@Composable
fun StatusIndicator(status: MatchStatus) {
    val (text, color) = when (status) {
        MatchStatus.LIVE -> "LIVE" to Color.Red
        MatchStatus.FINISHED -> "FINISHED" to Color.Gray
        MatchStatus.SCHEDULED -> "UPCOMING" to MaterialTheme.colorScheme.primary
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Rounded.FiberManualRecord,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview
@Composable
fun MatchCardPreview(){
    val match = Match(
        id = 1,
        homeTeamName = "Team A",
        awayTeamName = "Team B",
        homeScore = 10,
        awayScore = 5,
        date = "2023-08-01T12:00:00Z",
        homeLogoUrl = "",
        awayLogoUrl = "",
        status = MatchStatus.LIVE
    )
    MatchCard(match = match)
}
