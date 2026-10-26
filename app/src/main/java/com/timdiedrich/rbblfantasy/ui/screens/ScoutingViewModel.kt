package com.timdiedrich.rbblfantasy.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.timdiedrich.rbblfantasy.RbblFantasyApplication
import com.timdiedrich.rbblfantasy.data.model.AddToRosterRequest
import com.timdiedrich.rbblfantasy.data.model.FantasyPlayer
import com.timdiedrich.rbblfantasy.data.repository.RbblRepository
import com.timdiedrich.rbblfantasy.data.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScoutingUiState(
    val leagueName: String = "",
    val leagueId: Int? = null,
    val allFantasyPlayers: List<FantasyPlayer> = emptyList(),
    val filteredFantasyPlayers: List<FantasyPlayer> = emptyList(),
    val undrafted: List<FantasyPlayer> = emptyList(),
    val filteredUndrafted: List<FantasyPlayer> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ScoutingViewModel(
    private val repository: RbblRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ScoutingUiState())
    val uiState: StateFlow<ScoutingUiState> = _uiState.asStateFlow()

    init {
        observeSession()
    }

    private fun observeSession() {
        viewModelScope.launch {
            sessionRepository.activeLeague.collectLatest { league ->

                val currentLeagueId = league?.id
                _uiState.update { it.copy(
                    leagueName = league?.name ?: "No League",
                    leagueId = currentLeagueId
                ) }

                // Call these directly or use launch(this) to run in parallel within the collector scope

                currentLeagueId?.let {
                    loadPlayers(it)
                    loadUndrafted(it) }

            }
        }
    }

    private fun loadPlayers(leagueId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getPlayers(leagueId)
                .onSuccess { players ->
                    Log.d("ScoutingViewModel", "Successfully loaded ${players.size} players")
                    _uiState.update { it.copy(allFantasyPlayers = players, filteredFantasyPlayers = players, isLoading = false) }
                }
                .onFailure { exception ->
                    Log.e("ScoutingViewModel", "Failed to load players from API", exception)
                    _uiState.update { it.copy(isLoading = false, errorMessage = exception.message) }
                }
        }
    }

    private fun loadUndrafted(leagueId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getUndrafted(leagueId = leagueId)
                .onSuccess { players ->
                    Log.d("ScoutingViewModel", "Successfully loaded ${players.size} undrafted players")
                    _uiState.update { it.copy(undrafted = players, filteredUndrafted = players, isLoading = false) }
                }
                .onFailure { exception ->
                    Log.e("ScoutingViewModel", "Failed to load undrafted players from API", exception)
                    _uiState.update { it.copy(isLoading = false, errorMessage = exception.message) }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }

        viewModelScope.launch(Dispatchers.Default) {
            val allPlayers = _uiState.value.allFantasyPlayers
            val allPlayersRostered = _uiState.value.undrafted
            val filtered = if (query.isEmpty()) {
                allPlayers
            } else {
                allPlayers.filter { it.name.contains(query, ignoreCase = true) }
            }
            val filteredRostered = if (query.isEmpty()) {
                allPlayersRostered
            } else {
                allPlayersRostered.filter { it.name.contains(query, ignoreCase = true) }
            }
            _uiState.update { it.copy(filteredFantasyPlayers = filtered) }
            _uiState.update { it.copy(filteredUndrafted = filteredRostered) }
        }
    }

    fun addToRoster(fantasyPlayer: FantasyPlayer) {
        viewModelScope.launch {
            val activeTeam = sessionRepository.activeTeam.value ?: return@launch
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val request = AddToRosterRequest(
                rosterId = 0,
                fantasyTeamId = activeTeam.id,
                playerId = fantasyPlayer.id,
                isStarter = false
            )
            repository.addToRoster(request)
                .onSuccess {
                    Log.d("ScoutingViewModel", "Successfully added ${fantasyPlayer.name} to roster")
                    _uiState.update { it.copy(isLoading = false) }
                    loadUndrafted(_uiState.value.leagueId ?: 0)
                }
                .onFailure { exception ->
                    Log.e("ScoutingViewModel", "Failed to add ${fantasyPlayer.name} to roster", exception)
                    _uiState.update { it.copy(isLoading = false, errorMessage = exception.message) }
                }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RbblFantasyApplication)
                ScoutingViewModel(
                    repository = application.repository,
                    sessionRepository = application.sessionRepository
                )
            }
        }
    }
}