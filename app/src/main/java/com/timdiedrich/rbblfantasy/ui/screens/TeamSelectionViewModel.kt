package com.timdiedrich.rbblfantasy.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.timdiedrich.rbblfantasy.RbblFantasyApplication
import com.timdiedrich.rbblfantasy.data.model.FantasyLeague
import com.timdiedrich.rbblfantasy.data.model.FantasyTeam
import com.timdiedrich.rbblfantasy.data.repository.RbblRepository
import com.timdiedrich.rbblfantasy.data.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class SelectTeamUiState(
    val teams: List<FantasyTeam> = emptyList(),
    val selectedTeam: FantasyTeam? = null,
    val selectedLeague: FantasyLeague? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class TeamSelectionViewModel (
    private val repository: RbblRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SelectTeamUiState())
    val uiState = _uiState.asStateFlow()

    init {
        getAllTeams()
    }

    fun getAllTeams(){
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getAllUserTeams()
                .onSuccess { teams ->
                    _uiState.update { it.copy(teams = teams, isLoading = false) }
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = exception.message) }
                }
        }
    }

    fun selectTeam(team: FantasyTeam, onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            sessionRepository.setActiveTeam(team)
            repository.getLeague(team.leagueId).onSuccess { league ->
                sessionRepository.setActiveLeague(league)
                _uiState.update { it.copy(isLoading = false) }
                onComplete()
            }.onFailure { exception ->
                _uiState.update { it.copy(isLoading = false, errorMessage = exception.message) }
            }
        }
    }



    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RbblFantasyApplication)
                TeamSelectionViewModel(
                    repository = app.repository,
                    sessionRepository = app.sessionRepository
                )
            }
        }
    }
}