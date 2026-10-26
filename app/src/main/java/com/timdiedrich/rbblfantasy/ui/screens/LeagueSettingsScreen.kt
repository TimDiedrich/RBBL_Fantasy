package com.timdiedrich.rbblfantasy.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.timdiedrich.rbblfantasy.data.model.DraftStatus
import com.timdiedrich.rbblfantasy.utils.formatMatchTime
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeagueSettingsScreen(
    leagueId: Int,
    isAdmin: Boolean,
    onBackClick: () -> Unit,
    viewModel: LeagueSettingsViewModel = viewModel(factory = LeagueSettingsViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(leagueId) {
        viewModel.loadLeague(leagueId)
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            snackbarHostState.showSnackbar("League updated successfully")
            viewModel.resetSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("League Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (uiState.isLoading && uiState.league == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // League Name Section
                if (isAdmin) {
                    OutlinedTextField(
                        value = uiState.leagueNameInput,
                        onValueChange = { viewModel.onLeagueNameChanged(it) },
                        label = { Text("League Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = { viewModel.updateLeague() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading && uiState.leagueNameInput.isNotBlank()
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save Name")
                        }
                    }
                } else {
                    Text(
                        text = uiState.league?.name ?: "",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "DRAFT STATUS: ${uiState.league?.draftStatus}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                HorizontalDivider()

                DraftScheduleSection(
                    isAdmin = isAdmin,
                    draftDate = uiState.league?.draftDate,
                    draftDateInput = uiState.draftDateInput,
                    onDateChange = { viewModel.onDraftDateChanged(it) },
                    onSaveClick = { viewModel.updateLeague() },
                    isLoading = uiState.isLoading
                )

                Text(
                    text = "${uiState.currentNumberTeam} of required ${uiState.maxNumberTeams} teams in this league.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                HorizontalDivider()

                // Invite Code Section
                if(isAdmin){
                    Text(
                        text = "Invite Members",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Invite Code",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = uiState.league?.inviteCode ?: "Generating...",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Row {
                                    IconButton(onClick = {
                                        uiState.league?.inviteCode?.let {
                                            clipboardManager.setText(AnnotatedString(it))
                                        }
                                    }) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                                    }
                                    IconButton(onClick = {
                                        uiState.league?.inviteCode?.let { code ->
                                            val sendIntent: Intent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, "Join my RBBL Fantasy league with code: $code")
                                                type = "text/plain"
                                            }
                                            val shareIntent = Intent.createChooser(sendIntent, null)
                                            context.startActivity(shareIntent)
                                        }
                                    }) {
                                        Icon(Icons.Default.Share, contentDescription = "Share")
                                    }
                                }
                            }
                        }
                    }
                }

                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                uiState.settings?.let { settings ->
                    HorizontalDivider()
                    Text(
                        text = "Scoring & Roster Rules",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isAdmin && uiState.league?.draftStatus == DraftStatus.PREDRAFT) {
                                LeagueSettingEditItem("Max Roster Size", uiState.maxRosterSizeInput) { viewModel.onMaxRosterSizeChanged(it) }
                                LeagueSettingEditItem("Max Lineup Points", uiState.maxLineupPointsInput) { viewModel.onMaxLineupPointsChanged(it) }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                LeagueSettingEditItem("Points per Real Point", uiState.pointsPerRealPointInput) { viewModel.onPointsPerRealPointChanged(it) }
                                LeagueSettingEditItem("Points per Rebound", uiState.pointsPerReboundInput) { viewModel.onPointsPerReboundChanged(it) }
                                LeagueSettingEditItem("Points per Assist", uiState.pointsPerAssistInput) { viewModel.onPointsPerAssistChanged(it) }
                                LeagueSettingEditItem("Points per Steal", uiState.pointsPerStealInput) { viewModel.onPointsPerStealChanged(it) }
                                LeagueSettingEditItem("Points per Block", uiState.pointsPerBlockInput) { viewModel.onPointsPerBlockChanged(it) }
                                LeagueSettingEditItem("Points per Turnover (Should be negative)", uiState.pointsPerTurnoverInput) { viewModel.onPointsPerTurnoverChanged(it) }
                                LeagueSettingEditItem("3-Pointer Made Bonus", uiState.threePointMadeInput) { viewModel.onThreePointMadeChanged(it) }
                                LeagueSettingEditItem("Double-Double Bonus", uiState.doubleDoubleBonusInput) { viewModel.onDoubleDoubleBonusChanged(it) }
                                LeagueSettingEditItem("Triple-Double Bonus", uiState.tripleDoubleBonusInput) { viewModel.onTripleDoubleBonusChanged(it) }

                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { /* TODO: Implement save settings */ },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !uiState.isLoading
                                ) {
                                    Text("Save Settings")
                                }
                            } else {
                                LeagueSettingItem("Max Roster Size", settings.maxRosterSize.toString())
                                LeagueSettingItem("Max Lineup Points", settings.maxLineupPoints.toString())
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                LeagueSettingItem("Points per Real Point", settings.pointsPerRealPoint.toString())
                                LeagueSettingItem("Points per Rebound", settings.pointsPerRebound.toString())
                                LeagueSettingItem("Points per Assist", settings.pointsPerAssist.toString())
                                LeagueSettingItem("Points per Steal", settings.pointsPerSteal.toString())
                                LeagueSettingItem("Points per Block", settings.pointsPerBlock.toString())
                                LeagueSettingItem("Points per Turnover", settings.pointsPerTurnover.toString())
                                LeagueSettingItem("3-Pointer Made Bonus", settings.threePointMade.toString())
                                LeagueSettingItem("Double-Double Bonus", settings.doubleDoubleBonus.toString())
                                LeagueSettingItem("Triple-Double Bonus", settings.tripleDoubleBonus.toString())
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DraftScheduleSection(
    isAdmin: Boolean,
    draftDate: String?,
    draftDateInput: String,
    onDateChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    isLoading: Boolean
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Draft Schedule",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        if (isAdmin) {
            OutlinedTextField(
                value = if (draftDateInput.isNotEmpty()) formatMatchTime(draftDateInput) else "Not set",
                onValueChange = { },
                label = { Text("Draft Date & Time") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.Event, contentDescription = "Select Date")
                    }
                }
            )

            if (showDatePicker) {
                val calendar = Calendar.getInstance()
                android.app.DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        android.app.TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                val selectedDateTime = LocalDateTime.of(year, month + 1, dayOfMonth, hourOfDay, minute)
                                val isoString = selectedDateTime.atZone(ZoneId.systemDefault()).toInstant().toString()
                                onDateChange(isoString)
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        ).show()
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
                showDatePicker = false
            }

            Button(
                onClick = onSaveClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && draftDateInput.isNotEmpty()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save Draft Schedule")
                }
            }
        } else {
            Text(
                text = if (draftDate != null) "Scheduled for: ${formatMatchTime(draftDate)}" else "Draft date not yet set",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun LeagueSettingEditItem(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
}

@Composable
private fun LeagueSettingItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
