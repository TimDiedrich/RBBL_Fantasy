package com.timdiedrich.rbblfantasy.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.timdiedrich.rbblfantasy.data.model.DraftPlayer
import com.timdiedrich.rbblfantasy.data.model.DraftSortOrder
import com.timdiedrich.rbblfantasy.data.model.DraftTeamOrder
import com.timdiedrich.rbblfantasy.data.model.DraftTurnState
import com.timdiedrich.rbblfantasy.data.model.DraftUiState
import com.timdiedrich.rbblfantasy.ui.theme.RBBLFantasyTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraftRoomScreen(
    leagueId: String,
    userTeamId: String,
    jwtToken: String,
    serverUrl: String,
    viewModel: DraftViewModel = viewModel(factory = DraftViewModel.Factory)
) {
    LaunchedEffect(leagueId) {
        viewModel.connectToDraftRoom(
            serverUrl = serverUrl,
            jwtToken = jwtToken,
            leagueId = leagueId,
            userTeamId = userTeamId
        )
    }
    val uiState by viewModel.uiState.collectAsState()
    val secondsRemaining by viewModel.secondsRemaining.collectAsState()

    DraftRoomContent(
        uiState = uiState,
        secondsRemaining = secondsRemaining,
        onMakePick = { playerId -> viewModel.makePick(leagueId, playerId) },
        onClearError = { viewModel.clearError() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraftRoomContent(
    uiState: DraftUiState,
    secondsRemaining: Long,
    onMakePick: (String) -> Unit,
    onClearError: () -> Unit
) {
    var selectedClassGroups by remember { mutableStateOf(setOf(1, 2, 3, 4)) }
    var sortOrder by remember { mutableStateOf(DraftSortOrder.NAME) }

    val filteredAndSortedPlayers = remember(uiState.availablePlayers, selectedClassGroups, sortOrder) {
        uiState.availablePlayers
            .filter { player ->
                val group = (player.sportClass - player.bonus).toInt().coerceAtLeast(1)
                selectedClassGroups.contains(group)
            }
            .let { list ->
                when (sortOrder) {
                    DraftSortOrder.NAME -> list.sortedBy { it.name }
                    DraftSortOrder.CLASS_ASC -> list.sortedBy { it.sportClass }
                    DraftSortOrder.CLASS_DESC -> list.sortedByDescending { it.sportClass }
                }
            }
    }

    val isUserTurn = uiState.turnState.currentFantasyTeamId == uiState.userTeamId
            && uiState.turnState.status == "IN_PROGRESS"

    // Clear error toast/snackbar
    uiState.errorMessage?.let { err ->
        LaunchedEffect(err) {
            // Display error, then clear
            delay(3000)
            onClearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Draft Room") },
                actions = {

                    val draftStatusColor = if (uiState.turnState.status == "NOT_STARTED") Color.Red else if(uiState.turnState.status == "SCHEDULED") Color.Yellow else Color.Green
                    Text(
                        uiState.turnState.status,
                        color = draftStatusColor,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 16.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    val connectionStatusColor = if (uiState.isConnected) Color.Green else Color.Red
                    Text(
                        text = if (uiState.isConnected) "Connected" else "Disconnected",
                        color = connectionStatusColor,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. Timer & Turn Banner
            if (uiState.turnState.status == "SCHEDULED") {
                Log.d("DraftRoomScreen", "Scheduled for ${uiState.scheduledFor}")
                Text(
                    text = "Draft is scheduled for ${uiState.scheduledFor}",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outline
                )
            } else if (uiState.turnState.status == "IN_PROGRESS") {
                DraftHeaderBanner(
                    turnState = uiState.turnState,
                    secondsRemaining = secondsRemaining,
                    isUserTurn = isUserTurn
                )
            }


            // 1.5 Draft Order Bar
            DraftOrderBar(
                draftOrder = uiState.draftOrder,
                currentFantasyTeamId = uiState.turnState.currentFantasyTeamId
            )

            // 2. Class / Tier Eligibility Indicator
            if (isUserTurn) {
                EligibleClassesChipRow(eligibleClasses = uiState.turnState.eligibleSportClasses)
            }

            // 3. Error Banner
            uiState.errorMessage?.let { err ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // 4. Available Players List
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Available Players",
                    style = MaterialTheme.typography.titleMedium
                )

                Row {
                    var sortMenuExpanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { sortMenuExpanded = true }) {
                            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Name") },
                                onClick = { sortOrder = DraftSortOrder.NAME; sortMenuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Class (Low-High)") },
                                onClick = { sortOrder = DraftSortOrder.CLASS_ASC; sortMenuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Class (High-Low)") },
                                onClick = { sortOrder = DraftSortOrder.CLASS_DESC; sortMenuExpanded = false }
                            )
                        }
                    }

                    var filterMenuExpanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { filterMenuExpanded = true }) {
                            Icon(Icons.Default.FilterAlt, contentDescription = "Filter")
                        }
                        DropdownMenu(
                            expanded = filterMenuExpanded,
                            onDismissRequest = { filterMenuExpanded = false }
                        ) {
                            listOf(1, 2, 3, 4).forEach { group ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = selectedClassGroups.contains(group),
                                                onCheckedChange = null
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            if (group == 1) {
                                                Text(text = "<= 1.5")
                                            } else {
                                                Text(text = "$group.x")
                                            }

                                        }
                                    },
                                    onClick = {
                                        selectedClassGroups = if (selectedClassGroups.contains(group)) {
                                            selectedClassGroups - group
                                        } else {
                                            selectedClassGroups + group
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredAndSortedPlayers) { player ->
                    val isClassEligible = uiState.turnState.eligibleSportClasses.contains(player.sportClass)

                    PlayerDraftCard(
                        player = player,
                        canPick = isUserTurn && isClassEligible,
                        onPickClicked = {
                            onMakePick(player.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DraftOrderBar(
    draftOrder: List<DraftTeamOrder>,
    currentFantasyTeamId: String
) {
    if (draftOrder.isEmpty()) {
        Text(
            text = "No teams in draft order, because draft is not scheduled",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.outline
        )
        return
    }

    val listState = rememberLazyListState()

    LaunchedEffect(currentFantasyTeamId) {
        val index = draftOrder.indexOfFirst { it.fantasyTeamId == currentFantasyTeamId }
        if (index != -1) {
            listState.animateScrollToItem(index)
        }
    }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = "Draft Order",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.outline
        )
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(draftOrder) { index, team ->
                val isActive = team.fantasyTeamId == currentFantasyTeamId
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${index + 1}.",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isActive) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = team.fantasyTeamName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isActive) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DraftHeaderBanner(
    turnState: DraftTurnState,
    secondsRemaining: Long,
    isUserTurn: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isUserTurn) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isUserTurn) "YOUR TURN TO PICK!" else "On The Clock",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Round ${turnState.currentRound} • Pick #${turnState.currentPickNumber}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Timer Display
            val timerColor = if (secondsRemaining <= 10) Color.Red else MaterialTheme.colorScheme.onSurface
            Text(
                text = "${secondsRemaining}s",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = timerColor
            )
        }
    }
}

@Composable
fun EligibleClassesChipRow(eligibleClasses: List<Double>) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "Eligible Sport Classes for your team:",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            items(eligibleClasses) { sportClass ->
                SuggestionChip(
                    onClick = { },
                    label = { Text("Class $sportClass") }
                )
            }
        }
    }
}

@Composable
fun PlayerDraftCard(
    player: DraftPlayer,
    canPick: Boolean,
    onPickClicked: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = player.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Class ${player.sportClass}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                if(player.bonus != 0.0){
                    Text(
                        text = "Bonus: ${player.bonus}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Effective Class: ${player.sportClass - player.bonus}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

            }

            Button(
                onClick = onPickClicked,
                enabled = canPick
            ) {
                Text("Draft")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DraftRoomScreenPreview() {
    RBBLFantasyTheme {
        val sampleDraftTurnState = DraftTurnState(
            currentRound = 1,
            currentPickNumber = 5,
            currentFantasyTeamId = "team1",
            currentTeamName = "My Team",
            status = "IN_PROGRESS",
            eligibleSportClasses = listOf(1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5)
        )

        val sampleDraftOrder = listOf(
            DraftTeamOrder(1, "team1", "Team A", 1),
            DraftTeamOrder(1, "team2", "Team B", 2),
            DraftTeamOrder(1, "team3", "Team C", 3),
            DraftTeamOrder(1, "team4", "Team D", 4),
            DraftTeamOrder(1, "team5", "Team E", 5)
        )

        val samplePlayers = listOf(
            DraftPlayer("1", "Player One", 2.0),
            DraftPlayer("2", "Player Two", 4.5),
            DraftPlayer("3", "Player Three", 1.0, bonus = 0.5),
            DraftPlayer("4", "Player Four", 3.0),
            DraftPlayer("5", "Player Five", 2.5)
        )

        val uiState = DraftUiState(
            isConnected = true,
            userTeamId = "team1",
            turnState = sampleDraftTurnState,
            draftOrder = sampleDraftOrder,
            availablePlayers = samplePlayers
        )

        DraftRoomContent(
            uiState = uiState,
            secondsRemaining = 45L,
            onMakePick = {},
            onClearError = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PlayerDraftCardPreview() {
    RBBLFantasyTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PlayerDraftCard(
                player = DraftPlayer("1", "John Doe", 2.5),
                canPick = true,
                onPickClicked = {}
            )
            PlayerDraftCard(
                player = DraftPlayer("2", "Jane Smith", 4.0, bonus = 0.5),
                canPick = false,
                onPickClicked = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EligibleClassesChipRowPreview() {
    RBBLFantasyTheme {
        EligibleClassesChipRow(
            eligibleClasses = listOf(1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DraftHeaderBannerPreview() {
    RBBLFantasyTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DraftHeaderBanner(
                turnState = DraftTurnState(
                    currentRound = 1,
                    currentPickNumber = 5,
                    currentFantasyTeamId = "team1",
                    currentTeamName = "My Team",
                    status = "IN_PROGRESS",
                    eligibleSportClasses = listOf(1.0, 1.5, 2.0)
                ),
                secondsRemaining = 45L,
                isUserTurn = true
            )
            DraftHeaderBanner(
                turnState = DraftTurnState(
                    currentRound = 1,
                    currentPickNumber = 6,
                    currentFantasyTeamId = "team2",
                    currentTeamName = "Opponent Team",
                    status = "IN_PROGRESS",
                    eligibleSportClasses = listOf(1.0, 1.5, 2.0)
                ),
                secondsRemaining = 8L,
                isUserTurn = false
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DraftOrderBarPreview() {
    RBBLFantasyTheme {
        val sampleDraftOrder = listOf(
            DraftTeamOrder(1, "team1", "Team A", 1),
            DraftTeamOrder(1, "team2", "Team B", 2),
            DraftTeamOrder(1, "team3", "Team C", 3),
            DraftTeamOrder(1, "team4", "Team D", 4),
            DraftTeamOrder(1, "team5", "Team E", 5),
            DraftTeamOrder(1, "team6", "Team F", 6)
        )
        DraftOrderBar(
            draftOrder = sampleDraftOrder,
            currentFantasyTeamId = "team3"
        )
    }
}
