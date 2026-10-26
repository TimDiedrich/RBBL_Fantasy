package com.timdiedrich.rbblfantasy.ui.screens

import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.timdiedrich.rbblfantasy.RbblFantasyApplication
import com.timdiedrich.rbblfantasy.data.local.TokenManager
import com.timdiedrich.rbblfantasy.data.model.AddToRosterRequest
import com.timdiedrich.rbblfantasy.data.model.Match
import com.timdiedrich.rbblfantasy.data.model.Roster
import com.timdiedrich.rbblfantasy.data.repository.RbblRepository
import com.timdiedrich.rbblfantasy.data.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val leagueName: String = "",
    val leagueId: Int? = null,
    val teamName: String = "",
    val teamId: Int? = null,
    val roster: List<Roster> = emptyList(),
    val upcomingMatch: Match? = null,
    val totalPoints: Float = 0f,
    val isEditMode: Boolean = false,
    val lineUpClassification: Float = 0.0f,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val token: String? = null
)

class DashboardViewModel(
    private val repository: RbblRepository,
    private val sessionRepository: SessionRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeSession()
        observeToken()
    }

    private fun observeToken() {
        viewModelScope.launch {
            tokenManager.token.collect { token ->
                _uiState.update { it.copy(token = token ?: "") }
            }
        }
    }
    private fun observeSession() {
        viewModelScope.launch {
            sessionRepository.activeLeague.collectLatest { league ->
                _uiState.update { it.copy(
                    leagueName = league?.name ?: "No League",
                    leagueId = league?.id
                ) }
            }
        }
        viewModelScope.launch {
            sessionRepository.activeTeam.collectLatest { team ->
                _uiState.update {
                    it.copy(
                        teamName = team?.teamName ?: "No Team",
                        teamId = team?.id
                    )
                }
                if (team != null) {
                    loadRoster(team.id)
                } else {
                    _uiState.update { it.copy(roster = emptyList()) }
                }
            }
        }
    }

    fun toggleEditMode() {
        if (uiState.value.isEditMode && isLineUpValid()){
            saveRoster(uiState.value.roster)
        }
        _uiState.update { it.copy(isEditMode = !it.isEditMode) }
    }

    fun saveRoster(roster: List<Roster>) {
        val teamId = uiState.value.teamId ?: return
        val requests = roster.map {
            AddToRosterRequest(
                rosterId = it.rosterId, // Assuming playerId maps to rosterId for updates
                fantasyTeamId = teamId,
                playerId = it.playerId,
                isStarter = it.isStarter
            )
        }
        viewModelScope.launch {
            repository.saveRoster(requests)
                .onSuccess { updatedRoster ->
                    Log.d("DashboardViewModel", "Successfully saved roster")
                    _uiState.update { it.copy(roster = updatedRoster) }
                }
                .onFailure { exception ->
                    Log.e("DashboardViewModel", "Failed to save roster", exception)
                    _uiState.update { it.copy(errorMessage = "Failed to save roster: ${exception.message}") }
                }
        }
    }

    fun toggleStarterStatus(playerId: Int) {
        _uiState.update { state ->
            val updatedRoster = state.roster.map {
                if (it.playerId == playerId) it.copy(isStarter = !it.isStarter) else it
            }
            state.copy(roster = updatedRoster)
        }
        isLineUpValid()
    }

    fun onMovePlayer(playerId: Int, toStarter: Boolean) {
        _uiState.update { state ->
            val updatedRoster = state.roster.map {
                if (it.playerId == playerId) it.copy(isStarter = toStarter) else it
            }
            state.copy(roster = updatedRoster)
        }
    }

    private fun loadRoster(teamId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getRoster(teamId)
                .onSuccess { rosterResponse ->
                    Log.d("DashboardViewModel", "Successfully loaded ${rosterResponse.roster.size} players")
                    _uiState.update { it.copy(roster = rosterResponse.roster, isLoading = false) }
                    isLineUpValid()
                }
                .onFailure { exception ->
                    Log.e("DashboardViewModel", "Failed to load players from API", exception)
                    _uiState.update { it.copy(isLoading = false, errorMessage = exception.message) }
                }
        }
    }

    fun isLineUpValid(): Boolean {
        val roster = _uiState.value.roster
        val starters = roster.filter { it.isStarter }
        var points: Float = 0.0f
        for (starter in starters) {
            points += (starter.sportClass - starter.bonus)
        }
        _uiState.update { it.copy(lineUpClassification = points) }
        return if (points > 14.5f) false else true
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RbblFantasyApplication)
                DashboardViewModel(
                    repository = application.repository,
                    sessionRepository = application.sessionRepository,
                    tokenManager = application.tokenManager
                )
            }
        }
    }
}
