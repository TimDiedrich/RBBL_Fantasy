package com.timdiedrich.rbblfantasy.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.timdiedrich.rbblfantasy.RbblFantasyApplication
import com.timdiedrich.rbblfantasy.data.model.CreateLeagueRequest
import com.timdiedrich.rbblfantasy.data.model.FantasyLeague
import com.timdiedrich.rbblfantasy.data.repository.RbblRepository
import com.timdiedrich.rbblfantasy.data.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.onSuccess

data class LeagueSelectionUiState(
    val isLoading: Boolean = false,
    val joinedLeagueId: Int? = null,
    val errorMessage: String? = null
)

class LeagueSelectionViewModel(
    private val repository: RbblRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeagueSelectionUiState())
    val uiState: StateFlow<LeagueSelectionUiState> = _uiState.asStateFlow()

    fun createLeague(leagueName: String, maxTeams: Int = 4) {
        if (leagueName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "League name cannot be empty") }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {

            val request = CreateLeagueRequest(name = leagueName, maxNumberTeams = maxTeams)

            repository.createLeague(request)
                .onSuccess { league ->
                    sessionRepository.setActiveLeague(league)
                    _uiState.update { it.copy(isLoading = false, joinedLeagueId = league.id) }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            errorMessage = exception.localizedMessage ?: "League creation failed",
                            isLoading = false
                        )
                    }
                }

        }
    }

    fun joinLeague(inviteCode: String) {
        if (inviteCode.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Invite code cannot be empty") }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            repository.joinLeague(inviteCode)
                .onSuccess { league ->
                    sessionRepository.setActiveLeague(league)
                    _uiState.update { it.copy(isLoading = false, joinedLeagueId = league.id) }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            errorMessage = exception.localizedMessage ?: "League join failed",
                            isLoading = false
                        )
                    }
                }
        }
    }


    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RbblFantasyApplication)
                LeagueSelectionViewModel(
                    repository = application.repository,
                    sessionRepository = application.sessionRepository
                )
            }
        }
    }
}
