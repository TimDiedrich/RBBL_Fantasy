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
fun CreateTeamScreen(
    onTeamCreated: () -> Unit,
    viewModel: CreateTeamViewModel = viewModel(factory = CreateTeamViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isCreated) {
        if (uiState.isCreated) {
            onTeamCreated()
        }
    }

    CreateTeamContent(
        uiState = uiState,
        onCreateTeam = { viewModel.createTeam(it) }
    )
}

@Composable
fun CreateTeamContent(
    uiState: CreateTeamUiState,
    onCreateTeam: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var teamName by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create Your Team",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Give your fantasy team a name to get started.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = teamName,
            onValueChange = { teamName = it },
            label = { Text("Team Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onCreateTeam(teamName) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading && teamName.isNotBlank()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Create Team")
            }
        }
    }
}

@Preview(showBackground = true, name = "Default State")
@Composable
fun CreateTeamContentPreview() {
    RBBLFantasyTheme {
        Surface {
            CreateTeamContent(
                uiState = CreateTeamUiState(
                    isLoading = false,
                    errorMessage = null
                ),
                onCreateTeam = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
fun CreateTeamLoadingPreview() {
    RBBLFantasyTheme {
        Surface {
            CreateTeamContent(
                uiState = CreateTeamUiState(isLoading = true),
                onCreateTeam = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Error State")
@Composable
fun CreateTeamErrorPreview() {
    RBBLFantasyTheme {
        Surface {
            CreateTeamContent(
                uiState = CreateTeamUiState(errorMessage = "This team name is already taken."),
                onCreateTeam = {}
            )
        }
    }
}

