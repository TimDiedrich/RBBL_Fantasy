package com.timdiedrich.rbblfantasy.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.timdiedrich.rbblfantasy.RbblFantasyApplication
import com.timdiedrich.rbblfantasy.data.model.DraftStatus
import com.timdiedrich.rbblfantasy.data.model.FantasyLeague
import com.timdiedrich.rbblfantasy.data.model.LeagueAdminSettings
import com.timdiedrich.rbblfantasy.data.model.LeagueMembership
import com.timdiedrich.rbblfantasy.data.model.UpdateLeagueRequest
import com.timdiedrich.rbblfantasy.data.repository.RbblRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LeagueSettingsUiState(
    val leagueMembers: List<LeagueMembership> = emptyList(),
    val maxNumberTeams: Int = 0,
    val currentNumberTeam: Int = 0,
    val league: FantasyLeague? = null,
    val settings: LeagueAdminSettings? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val leagueNameInput: String = "",
    val maxRosterSizeInput: String = "",
    val maxLineupPointsInput: String = "",
    val pointsPerRealPointInput: String = "",
    val pointsPerReboundInput: String = "",
    val pointsPerAssistInput: String = "",
    val pointsPerStealInput: String = "",
    val pointsPerBlockInput: String = "",
    val pointsPerTurnoverInput: String = "",
    val threePointMadeInput: String = "",
    val doubleDoubleBonusInput: String = "",
    val tripleDoubleBonusInput: String = "",
    val draftDateInput: String = ""
)

class LeagueSettingsViewModel(
    private val repository: RbblRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeagueSettingsUiState())
    val uiState: StateFlow<LeagueSettingsUiState> = _uiState.asStateFlow()

    fun loadLeague(leagueId: Int) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val leagueResult = repository.getLeague(leagueId)
            val settingsResult = repository.getLeagueAdminSettings(leagueId)
            val leagueMembersResult = repository.getAllLeagueMembers(leagueId)

            if (leagueResult.isSuccess && settingsResult.isSuccess && leagueMembersResult.isSuccess) {
                val league = leagueResult.getOrNull()!!
                val settings = settingsResult.getOrNull()!!
                val leagueMembers = leagueMembersResult.getOrNull()!!
                val maxNumberTeams = league.maxNumberTeams
                _uiState.update {
                    it.copy(
                        leagueMembers = leagueMembers,
                        maxNumberTeams = maxNumberTeams,
                        currentNumberTeam = leagueMembers.size,
                        league = league,
                        settings = settings,
                        leagueNameInput = league.name,
                        maxRosterSizeInput = settings.maxRosterSize.toString(),
                        maxLineupPointsInput = settings.maxLineupPoints.toString(),
                        pointsPerRealPointInput = settings.pointsPerRealPoint.toString(),
                        pointsPerReboundInput = settings.pointsPerRebound.toString(),
                        pointsPerAssistInput = settings.pointsPerAssist.toString(),
                        pointsPerStealInput = settings.pointsPerSteal.toString(),
                        pointsPerBlockInput = settings.pointsPerBlock.toString(),
                        pointsPerTurnoverInput = settings.pointsPerTurnover.toString(),
                        threePointMadeInput = settings.threePointMade.toString(),
                        doubleDoubleBonusInput = settings.doubleDoubleBonus.toString(),
                        tripleDoubleBonusInput = settings.tripleDoubleBonus.toString(),
                        draftDateInput = league.draftDate ?: "",
                        isLoading = false
                    )
                }
            } else {
                val error = leagueResult.exceptionOrNull() ?: settingsResult.exceptionOrNull()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error?.localizedMessage ?: "Failed to load league details"
                    )
                }
            }
        }
    }

    fun onLeagueNameChanged(newName: String) {
        _uiState.update { it.copy(leagueNameInput = newName) }
    }

    fun onMaxRosterSizeChanged(input: String) {
        _uiState.update { it.copy(maxRosterSizeInput = input) }
    }

    fun onMaxLineupPointsChanged(input: String) {
        _uiState.update { it.copy(maxLineupPointsInput = input) }
    }

    fun onPointsPerRealPointChanged(input: String) {
        _uiState.update { it.copy(pointsPerRealPointInput = input) }
    }

    fun onPointsPerReboundChanged(input: String) {
        _uiState.update { it.copy(pointsPerReboundInput = input) }
    }

    fun onPointsPerAssistChanged(input: String) {
        _uiState.update { it.copy(pointsPerAssistInput = input) }
    }

    fun onPointsPerStealChanged(input: String) {
        _uiState.update { it.copy(pointsPerStealInput = input) }
    }

    fun onPointsPerBlockChanged(input: String) {
        _uiState.update { it.copy(pointsPerBlockInput = input) }
    }

    fun onPointsPerTurnoverChanged(input: String) {
        _uiState.update { it.copy(pointsPerTurnoverInput = input) }
    }

    fun onThreePointMadeChanged(input: String) {
        _uiState.update { it.copy(threePointMadeInput = input) }
    }

    fun onDoubleDoubleBonusChanged(input: String) {
        _uiState.update { it.copy(doubleDoubleBonusInput = input) }
    }

    fun onTripleDoubleBonusChanged(input: String) {
        _uiState.update { it.copy(tripleDoubleBonusInput = input) }
    }

    fun onDraftDateChanged(newDate: String) {
        _uiState.update { it.copy(draftDateInput = newDate) }
    }

    fun updateLeague() {
        val leagueId = _uiState.value.league?.id ?: return
        val newName = _uiState.value.leagueNameInput
        val newDraftDate = _uiState.value.draftDateInput.ifBlank { null }
        val newDraftStatus =  if(newDraftDate != null) DraftStatus.SCHEDULED else DraftStatus.PREDRAFT

        if (newName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "League name cannot be empty") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, isSuccess = false) }
        viewModelScope.launch {
            repository.updateLeague(leagueId, UpdateLeagueRequest(name = newName, draftStatus = newDraftStatus, draftDate = newDraftDate))
                .onSuccess { updatedLeague ->
                    _uiState.update { 
                        it.copy(
                            league = updatedLeague, 
                            isLoading = false,
                            isSuccess = true 
                        ) 
                    }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = error.localizedMessage ?: "Failed to update league"
                        ) 
                    }
                }
        }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RbblFantasyApplication)
                LeagueSettingsViewModel(
                    repository = application.repository
                )
            }
        }
    }
}
