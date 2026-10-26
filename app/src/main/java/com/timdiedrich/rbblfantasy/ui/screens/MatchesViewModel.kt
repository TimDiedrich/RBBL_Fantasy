package com.timdiedrich.rbblfantasy.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.timdiedrich.rbblfantasy.RbblFantasyApplication
import com.timdiedrich.rbblfantasy.data.model.FantasyLeague
import com.timdiedrich.rbblfantasy.data.model.Match
import com.timdiedrich.rbblfantasy.data.model.MatchStatus
import com.timdiedrich.rbblfantasy.data.repository.RbblRepository
import com.timdiedrich.rbblfantasy.data.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MatchesUiState(
    val realMatches: List<Match> = emptyList(),
    val fantasyMatches: List<FantasyMatch> = emptyList(),
    val availableWeeks: List<Int> = (1..27).toList(),
    val selectedWeek: Int = 1,
    val selectedTab: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class FantasyMatch(
    val id: String,
    val homeTeamName: String,
    val awayTeamName: String,
    val homeScore: Int?,
    val awayScore: Int?,
    val homeLogoUrl: String,
    val awayLogoUrl: String,
    val status: MatchStatus
)

class MatchesViewModel(
    private val repository: RbblRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MatchesUiState())
    val uiState: StateFlow<MatchesUiState> = _uiState.asStateFlow()

    init {
        observeSession()
    }

    private fun observeSession() {
        viewModelScope.launch {
            sessionRepository.activeLeague.collectLatest { _ ->
                loadRealMatches(uiState.value.selectedWeek)
            }
        }
    }

    fun loadRealMatches(week: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            if (week == 0) {
                repository.getRealMatches()
                    .onSuccess { matchesResponse ->
                        _uiState.update { it.copy(realMatches = matchesResponse.matches, isLoading = false) }
                    }
                    .onFailure { exception ->
                        _uiState.update { it.copy(isLoading = false, errorMessage = exception.message) }
                        }
            } else {
                repository.getRealMatchesByWeek(week)
                    .onSuccess { matchesResponse ->
                        _uiState.update { it.copy(realMatches = matchesResponse.matches, isLoading = false) }
                    }.onFailure { exception ->
                        _uiState.update { it.copy(isLoading = false, errorMessage = exception.message) }
                    }
            }
        }
    }
    fun selectWeek(week: Int) {
        _uiState.update { it.copy(selectedWeek = week) }
        loadRealMatches(week)
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    private fun loadMatches(league: FantasyLeague) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getMatches().onSuccess { matches ->
                // For now, assume repository returns all matches and we filter or it's just real matches
                _uiState.update { it.copy(realMatches = matches, isLoading = false) }
            }.onFailure { exception ->
                // Mock data for demo/testing purposes

                val mockFantasyMatches = listOf(
                    FantasyMatch("f1", "Team Tim", "Team Alex", 112, 105, "", "", MatchStatus.FINISHED),
                    FantasyMatch("f2", "Baskets 44", "Dunking Kings", 45, 52, "", "", MatchStatus.LIVE),
                    FantasyMatch(
                        "f3",
                        "My Fantasy Squad",
                        "Opponent Team",
                        null,
                        null,
                        homeLogoUrl = "",
                        awayLogoUrl = "",
                        MatchStatus.SCHEDULED
                    )
                )
                _uiState.update {
                    it.copy(
                        fantasyMatches = mockFantasyMatches,
                        isLoading = false,
                        errorMessage = exception.message
                    )
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RbblFantasyApplication)
                MatchesViewModel(
                    repository = application.repository,
                    sessionRepository = application.sessionRepository
                )
            }
        }
    }
}


