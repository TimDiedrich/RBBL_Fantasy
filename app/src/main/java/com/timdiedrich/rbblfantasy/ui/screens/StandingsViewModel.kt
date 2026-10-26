package com.timdiedrich.rbblfantasy.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.timdiedrich.rbblfantasy.RbblFantasyApplication
import com.timdiedrich.rbblfantasy.data.model.FantasyLeague
import com.timdiedrich.rbblfantasy.data.model.Standing
import com.timdiedrich.rbblfantasy.data.repository.RbblRepository
import com.timdiedrich.rbblfantasy.data.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StandingsUiState(
    val globalStandings: List<Standing> = emptyList(),
    val privateStandings: List<Standing> = emptyList(),
    val privateLeague: String = "",
    val isGlobal: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class StandingsViewModel(
    private val repository: RbblRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(StandingsUiState())
    val uiState: StateFlow<StandingsUiState> = _uiState.asStateFlow()

    init {
        observeSession()
    }

    private fun observeSession() {
        viewModelScope.launch {
            sessionRepository.activeLeague.collectLatest { league ->
                if (league != null) {
                    _uiState.update { it.copy(privateLeague = league.name) }
                    loadStandings(league)
                }
            }
        }
    }

    private fun loadStandings(league: FantasyLeague) {
        Log.d("StandingsViewModel", "Loading standings for league: ${league.name}")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getStandings(league)
                .onSuccess { standings ->
                    Log.d("StandingsViewModel", "Standings loaded successfully")
                    _uiState.update { it.copy(privateStandings = standings, isLoading = false) }
                }
                .onFailure { exception ->
                    Log.e("StandingsViewModel", "Failed to load standings", exception)
                    _uiState.update { it.copy(isLoading = false, errorMessage = exception.message) }
                }
        }
    }

    fun toggleLeague(isGlobal: Boolean) {
        _uiState.update { it.copy(isGlobal = isGlobal) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RbblFantasyApplication)
                StandingsViewModel(
                    repository = application.repository,
                    sessionRepository = application.sessionRepository
                )
            }
        }
    }
}

