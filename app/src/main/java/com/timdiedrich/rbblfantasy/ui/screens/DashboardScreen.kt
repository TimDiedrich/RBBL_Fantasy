package com.timdiedrich.rbblfantasy.ui.screens

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timdiedrich.rbblfantasy.BuildConfig
import com.timdiedrich.rbblfantasy.data.model.Match
import com.timdiedrich.rbblfantasy.data.model.MatchStatus
import com.timdiedrich.rbblfantasy.data.model.Roster
import com.timdiedrich.rbblfantasy.ui.components.PlayerAvatar
import com.timdiedrich.rbblfantasy.ui.theme.RBBLFantasyTheme
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToDraft: (Int, String, String, String) -> Unit = { _, _, _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardContent(
        uiState = uiState,
        onToggleEditMode = { viewModel.toggleEditMode() },
        onToggleStarterStatus = { viewModel.toggleStarterStatus(it) },
        onNavigateToDraft = onNavigateToDraft
    )
}

@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    onToggleEditMode: () -> Unit,
    onToggleStarterStatus: (Int) -> Unit,
    onNavigateToDraft: (Int, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Pre-filter the roster to avoid "empty" items in the LazyColumn.
    val starters = remember(uiState.roster) { uiState.roster.filter { it.isStarter } }
    val bench = remember(uiState.roster) { uiState.roster.filter { !it.isStarter } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // --- 1. Header Title ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "My Fantasy Hub",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (uiState.leagueName.isNotBlank() && uiState.teamName.isNotBlank()) {
                        Text(
                            text = "${uiState.leagueName} • ${uiState.teamName}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                IconButton(onClick = onToggleEditMode) {
                    Icon(
                        imageVector = if (uiState.isEditMode) Icons.Rounded.Check else Icons.Rounded.Edit,
                        contentDescription = if (uiState.isEditMode) "Done" else "Edit Roster"
                    )
                }
            }
        }

        // --- 2. Bento Grid Tile Area ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                BentoTile(
                    title = "Total Points",
                    value = "${uiState.totalPoints}",
                    icon = Icons.Rounded.Star,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    color = MaterialTheme.colorScheme.primaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    BentoTile(
                        title = "Draft Room",
                        value = "Enter Now",
                        icon = Icons.Rounded.Handshake,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        onClick = {
                            val leagueId = uiState.leagueId
                            val teamId = uiState.teamId
                            val token = uiState.token
                            if (leagueId != null && teamId != null && token != null) {
                                onNavigateToDraft(
                                    leagueId,
                                    teamId.toString(),
                                    token,
                                    BuildConfig.SOCKET_URL
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BentoTile(
                        title = "Upcoming",
                        value = uiState.upcomingMatch?.let { "${it.homeTeamName} vs ${it.awayTeamName}" } ?: "No games",
                        icon = Icons.Rounded.Event,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    )
                }
            }
        }

        // --- 3. Current Roster Section ---
        item {
            Text(
                text = "Starters",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
            Text(
                text = uiState.lineUpClassification.toString(),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        items(
            items = starters,
            key = { player -> "roster_${player.playerId}" }
        ) { player ->
            RosterItem(
                player = player,
                isEditMode = uiState.isEditMode,
                onRemove = { },
                onToggleStarter = { onToggleStarterStatus(player.playerId) }
            )
        }

        // --- 4. Bench Players Section ---
        item {
            Text(
                text = "Bench Players",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
        }

        items(
            items = bench,
            key = { player -> "bench_${player.playerId}" }
        ) { player ->
            RosterItem(
                player = player,
                isEditMode = uiState.isEditMode,
                onRemove = { },
                onToggleStarter = { onToggleStarterStatus(player.playerId) }
            )
        }
    }
}

@Composable
fun BentoTile(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    color: Color,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.labelMedium)
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun RosterItem(
    player: Roster,
    isEditMode: Boolean = false,
    onRemove: () -> Unit,
    onToggleStarter: () -> Unit = {}
) {
    var offsetY by remember { mutableFloatStateOf(0f) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isEditMode) {
                    Modifier
                        .offset { IntOffset(0, offsetY.roundToInt()) }
                        .zIndex(if (offsetY != 0f) 1f else 0f)
                        .pointerInput(Unit) {
                            detectDragGesturesAfterLongPress(
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    offsetY += dragAmount.y
                                },
                                onDragEnd = {
                                    if (offsetY.absoluteValue > 50f) {
                                        onToggleStarter()
                                    }
                                    offsetY = 0f
                                },
                                onDragCancel = {
                                    offsetY = 0f
                                }
                            )
                        }
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isEditMode && offsetY != 0f)
                MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (offsetY != 0f) 8.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isEditMode) {
                    Icon(
                        Icons.Rounded.DragHandle,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                PlayerAvatar(
                    imageUrl = player.imageUrl,
                    playerName = player.playerName,
                    nationality = player.nationality,
                    modifier = Modifier
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.padding(end = 8.dp)) {
                    Text(
                        text = player.playerName,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(text = player.sportClass.toString(), style = MaterialTheme.typography.bodySmall)
                }
            }
            if (isEditMode) {
                TextButton(
                    onClick = onToggleStarter,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.padding(start = 8.dp)
                ){
                    Text(text = if (player.isStarter) "To Bench" else "To Starters",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold)
                }
            } else {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Remove")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RosterItemPreview() {
    RBBLFantasyTheme {
        Surface {
            val player = Roster(
                playerId = 1,
                shirtNumber = 1,
                playerName = "Geshnigani Seyedmehran",
                imageUrl = null,
                dob = null,
                nationality = null,
                position = null,
                sportClass = 3.0f,
                gender = "MALE",
                bonus = 0.0f,
                teamId = "",
                isStarter = true,
                rosterId = 1
            )
            RosterItem(
                player = player,
                isEditMode = true,
                onRemove = {},
                onToggleStarter = {}
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DashboardScreenPreview() {
    val sampleRoster = listOf(
        Roster(
            playerId = 1,
            shirtNumber = 10,
            playerName = "Player One",
            imageUrl = null,
            dob = null,
            nationality = "GER",
            position = "Guard",
            sportClass = 2.5f,
            gender = "MALE",
            bonus = 0.0f,
            teamId = "1",
            isStarter = true,
            rosterId = 1
        ),
        Roster(
            playerId = 2,
            shirtNumber = 5,
            playerName = "Player Two",
            imageUrl = null,
            dob = null,
            nationality = "USA",
            position = "Forward",
            sportClass = 4.0f,
            gender = "MALE",
            bonus = 0.0f,
            teamId = "1",
            isStarter = false,
            rosterId = 2
        )
    )

    val sampleMatch = Match(
        id = 1,
        homeTeamName = "Lahn-Dill",
        awayTeamName = "Thuringia Bulls",
        homeScore = 0,
        awayScore = 0,
        homeLogoUrl = "",
        awayLogoUrl = "",
        date = "2023-12-01",
        status = MatchStatus.SCHEDULED
    )

    val sampleUiState = DashboardUiState(
        leagueName = "RBBL1",
        teamName = "My Awesome Team",
        roster = sampleRoster,
        upcomingMatch = sampleMatch,
        totalPoints = 125.5f,
        isEditMode = false,
        lineUpClassification = 14.5f
    )

    RBBLFantasyTheme {
        Surface {
            DashboardContent(
                uiState = sampleUiState,
                onToggleEditMode = {},
                onToggleStarterStatus = {},
                onNavigateToDraft = { _, _, _, _ -> }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Dashboard Edit Mode")
@Composable
fun DashboardScreenEditModePreview() {
    val sampleRoster = listOf(
        Roster(
            playerId = 1,
            shirtNumber = 10,
            playerName = "Player One",
            imageUrl = null,
            dob = null,
            nationality = "GER",
            position = "Guard",
            sportClass = 2.5f,
            gender = "MALE",
            bonus = 0.0f,
            teamId = "1",
            isStarter = true,
            rosterId = 1
        ),
        Roster(
            playerId = 2,
            shirtNumber = 5,
            playerName = "Player Two",
            imageUrl = null,
            dob = null,
            nationality = "USA",
            position = "Forward",
            sportClass = 4.0f,
            gender = "MALE",
            bonus = 0.0f,
            teamId = "1",
            isStarter = false,
            rosterId = 2
        )
    )

    val sampleUiState = DashboardUiState(
        leagueName = "RBBL1",
        teamName = "My Awesome Team",
        roster = sampleRoster,
        isEditMode = true,
        lineUpClassification = 14.5f
    )

    RBBLFantasyTheme {
        Surface {
            DashboardContent(
                uiState = sampleUiState,
                onToggleEditMode = {},
                onToggleStarterStatus = {},
                onNavigateToDraft = { _, _, _, _ -> }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dashboard Dark Mode")
@Composable
fun DashboardScreenDarkModePreview() {
    val sampleRoster = listOf(
        Roster(
            playerId = 1,
            shirtNumber = 10,
            playerName = "Player One",
            imageUrl = null,
            dob = null,
            nationality = "GER",
            position = "Guard",
            sportClass = 2.5f,
            gender = "MALE",
            bonus = 0.0f,
            teamId = "1",
            isStarter = true,
            rosterId = 1
        ),
        Roster(
            playerId = 2,
            shirtNumber = 5,
            playerName = "Player Two",
            imageUrl = null,
            dob = null,
            nationality = "USA",
            position = "Forward",
            sportClass = 4.0f,
            gender = "MALE",
            bonus = 0.0f,
            teamId = "1",
            isStarter = false,
            rosterId = 2
        )
    )

    val sampleMatch = Match(
        id = 1,
        homeTeamName = "Lahn-Dill",
        awayTeamName = "Thuringia Bulls",
        homeScore = 0,
        awayScore = 0,
        homeLogoUrl = "",
        awayLogoUrl = "",
        date = "2023-12-01",
        status = MatchStatus.SCHEDULED
    )

    val sampleUiState = DashboardUiState(
        leagueName = "RBBL1",
        teamName = "My Awesome Team",
        roster = sampleRoster,
        upcomingMatch = sampleMatch,
        totalPoints = 125.5f,
        isEditMode = false,
        lineUpClassification = 14.5f
    )

    RBBLFantasyTheme {
        Surface {
            DashboardContent(
                uiState = sampleUiState,
                onToggleEditMode = {},
                onToggleStarterStatus = {},
                onNavigateToDraft = { _, _, _, _ -> }
            )
        }
    }
}
