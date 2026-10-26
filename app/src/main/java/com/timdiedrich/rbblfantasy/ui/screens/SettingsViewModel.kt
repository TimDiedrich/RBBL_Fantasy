package com.timdiedrich.rbblfantasy.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.timdiedrich.rbblfantasy.RbblFantasyApplication
import com.timdiedrich.rbblfantasy.data.local.TokenManager
import com.timdiedrich.rbblfantasy.data.model.FantasyLeague
import com.timdiedrich.rbblfantasy.data.model.FantasyTeam
import com.timdiedrich.rbblfantasy.data.model.LeagueMembership
import com.timdiedrich.rbblfantasy.data.model.Role
import com.timdiedrich.rbblfantasy.data.model.UpdateLeagueRequest
import com.timdiedrich.rbblfantasy.data.repository.RbblRepository
import com.timdiedrich.rbblfantasy.data.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val navigateToLeagueSelection: Boolean = false,
    val leagueMemberships: List<LeagueMembership> = emptyList(),
    val rawLeagues: List<FantasyLeague> = emptyList(), // Store raw models
    val myLeagues: List<FantasyLeagueUiState> = emptyList(),
    val myTeams: List<FantasyTeam> = emptyList(),
    val activeTeamId: Int? = null,
    val activeLeagueId: Int? = null,
    val currentUserId: Int? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class FantasyLeagueUiState(
    val id: Int,
    val maxNumberTeams: Int,
    val leagueName: String,
    val isCommissioner: Boolean = false
)

class SettingsViewModel(
    private val repository: RbblRepository,
    private val tokenManager: TokenManager,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadMyTeams()
        observeActiveTeam()
        observeActiveLeague()
        observeCurrentUser()
    }

    private fun observeActiveTeam() {
        viewModelScope.launch {
            sessionRepository.activeTeam.collectLatest { team ->
                _uiState.update { it.copy(activeTeamId = team?.id) }
            }
        }
    }

    private fun observeActiveLeague() {
        viewModelScope.launch {
            sessionRepository.activeLeague.collectLatest { league ->
                _uiState.update { it.copy(activeLeagueId = league?.id) }
                loadMyLeagues()
            }
        }
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            sessionRepository.currentUser.collectLatest { user ->
                _uiState.update { it.copy(currentUserId = user?.id) }
                updateLeaguesList()
            }
        }
    }

    private fun updateLeaguesList() {
        loadMyLeagues()
    }

    fun loadMyLeagues() {
        val leagueId = _uiState.value.activeLeagueId
        if (leagueId == null) {
            _uiState.update { it.copy(isLoading = false) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.getLeague(leagueId)
                .onSuccess { league ->
                    val l =  FantasyLeagueUiState(
                        id = league.id,
                        maxNumberTeams = league.maxNumberTeams,
                        leagueName = league.name,
                        isCommissioner = (league.ownerId == _uiState.value.currentUserId)
                    )
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = null,
                        myLeagues = listOf(l)
                    )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false,
                        errorMessage = error.message) }
                }

        }


    }

    fun loadMyTeams() {
        viewModelScope.launch {
            repository.getAllUserTeams()
                .onSuccess { teams ->
                    _uiState.update { it.copy(myTeams = teams) }
                    updateLeaguesList()
                }
                .onFailure {
                    _uiState.update { it.copy(myTeams = emptyList()) }
                }
        }
    }

    fun switchTeam(team: FantasyTeam) {
        viewModelScope.launch {
            // Update active team
            sessionRepository.setActiveTeam(team)

            var matchingLeague = _uiState.value.rawLeagues.find { it.id == team.leagueId }
            // If not found in state, fetch it from repository
            if (matchingLeague == null) {
                matchingLeague = repository.getLeague(team.leagueId).getOrNull()
            }
            sessionRepository.setActiveLeague(matchingLeague)
        }
    }

    // Call your updated backend endpoint when editing a league name
    fun updateLeagueName(leagueId: Int, newName: String) {
        viewModelScope.launch {
            repository.updateLeague(leagueId, UpdateLeagueRequest(newName, null,null))
                .onSuccess {
                    // Refresh leagues list on success
                    loadMyLeagues()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
        }
    }

    fun toLeagueSelection() {
        _uiState.update { it.copy(navigateToLeagueSelection = true) }
    }

    fun onLeagueSelectionNavigated() {
        _uiState.update { it.copy(navigateToLeagueSelection = false) }
    }

    fun logout() {
        tokenManager.clearToken()
        sessionRepository.clearSession()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RbblFantasyApplication)
                SettingsViewModel(
                    repository = application.repository,
                    tokenManager = application.tokenManager,
                    sessionRepository = application.sessionRepository
                )
            }
        }
    }
}