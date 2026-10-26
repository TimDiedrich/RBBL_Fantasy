package com.timdiedrich.rbblfantasy.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.timdiedrich.rbblfantasy.RbblFantasyApplication
import com.timdiedrich.rbblfantasy.data.local.TokenManager
import com.timdiedrich.rbblfantasy.data.repository.RbblRepository
import com.timdiedrich.rbblfantasy.data.repository.SessionRepository
import com.timdiedrich.rbblfantasy.ui.navigation.Destination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface SplashUiState {
    data object Loading : SplashUiState
    data class Success(val startDestination: Destination) : SplashUiState
    data object LoggedOut : SplashUiState
}

class MainViewModel(
    private val tokenManager: TokenManager,
    private val repository: RbblRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        observeToken()
    }

    private fun observeToken() {
        viewModelScope.launch {
            Log.d("MainViewModel", "Observing token...")
            tokenManager.token.collect { token ->
                Log.d("MainViewModel", "Token collected: ${if (token.isNullOrBlank()) "null/blank" else "present"}")
                if (token.isNullOrBlank()) {
                    Log.d("MainViewModel", "No token found, setting state to LoggedOut")
                    sessionRepository.clearSession()
                    _uiState.value = SplashUiState.LoggedOut
                } else if (_uiState.value is SplashUiState.Loading || _uiState.value is SplashUiState.LoggedOut) {
                    Log.d("MainViewModel", "Token found, checking initial destination")
                    checkInitialDestination()
                }
            }
        }
    }


    private fun checkInitialDestination() {
        viewModelScope.launch {
            Log.d("MainViewModel", "checkInitialDestination: Fetching current user...")
            repository.getCurrentUser()
                .onSuccess { user ->
                    Log.d("MainViewModel", "checkInitialDestination: User fetched successfully: ${user.username}")
                    sessionRepository.setCurrentUser(user)
                    Log.d("MainViewModel", "checkInitialDestination: Fetching user teams...")
                    repository.getAllUserTeams()
                        .onSuccess { teams ->
                            Log.d("MainViewModel", "checkInitialDestination: Found ${teams.size} teams")
                            if (teams.isEmpty()) {
                                _uiState.value = SplashUiState.Success(Destination.LeagueSelection)
                            } else {
                                val defaultTeam = teams.first()
                                Log.d("MainViewModel", "checkInitialDestination: Setting active team: ${defaultTeam.teamName}")
                                sessionRepository.setActiveTeam(defaultTeam)
                                repository.getAllFantasyLeagues()
                                    .onSuccess { leagues ->
                                        val matchingLeague = leagues.find { it.id == defaultTeam.leagueId }
                                        Log.d("MainViewModel", "checkInitialDestination: Setting active league: ${matchingLeague?.name}")
                                        sessionRepository.setActiveLeague(matchingLeague)
                                    }
                                    .onFailure { exception ->
                                        Log.e("MainViewModel", "checkInitialDestination: Error fetching leagues: ${exception.message}")
                                    }
                                _uiState.value = SplashUiState.Success(Destination.TeamSelection)
                            }
                        }
                        .onFailure {
                            Log.e("MainViewModel", "checkInitialDestination: Error fetching teams: ${it.message}")
                            _uiState.value = SplashUiState.Success(Destination.Login)
                        }
                }
                .onFailure {
                    Log.e("MainViewModel", "checkInitialDestination: Error fetching current user: ${it.message}")
                    _uiState.value = SplashUiState.Success(Destination.Login)
                }
        }
    }


    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RbblFantasyApplication)
                MainViewModel(
                    tokenManager = app.tokenManager,
                    repository = app.repository,
                    sessionRepository = app.sessionRepository
                )
            }
        }
    }
}