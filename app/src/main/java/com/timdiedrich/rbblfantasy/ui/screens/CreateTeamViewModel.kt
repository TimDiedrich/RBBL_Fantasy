package com.timdiedrich.rbblfantasy.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.timdiedrich.rbblfantasy.RbblFantasyApplication
import com.timdiedrich.rbblfantasy.data.model.CreateTeamRequest
import com.timdiedrich.rbblfantasy.data.repository.RbblRepository
import com.timdiedrich.rbblfantasy.data.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateTeamUiState(
    val leagueId: Int? = null,
    val isLoading: Boolean = false,
    val isCreated: Boolean = false,
    val errorMessage: String? = null
)

class CreateTeamViewModel(
    private val repository: RbblRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateTeamUiState())
    val uiState = _uiState.asStateFlow()

    fun setLeagueId(leagueId: Int) {
        _uiState.update { it.copy(leagueId = leagueId) }
    }

    fun createTeam(teamName: String) {
        val leagueId = _uiState.value.leagueId
        if (leagueId == null) {
            _uiState.update { it.copy(errorMessage = "Missing league ID") }
            return
        }
        if (teamName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Team name cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.createTeam(CreateTeamRequest(leagueId, teamName))
                .onSuccess { team ->
                    sessionRepository.setActiveTeam(team)
                    _uiState.update { it.copy(isLoading = false, isCreated = true) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Failed to create team") }
                }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RbblFantasyApplication)
                CreateTeamViewModel(
                    repository = app.repository,
                    sessionRepository = app.sessionRepository
                )
            }
        }
    }
}
