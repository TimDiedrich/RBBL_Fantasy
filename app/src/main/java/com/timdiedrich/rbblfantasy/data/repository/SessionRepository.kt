package com.timdiedrich.rbblfantasy.data.repository

import com.timdiedrich.rbblfantasy.data.model.FantasyLeague
import com.timdiedrich.rbblfantasy.data.model.FantasyTeam
import com.timdiedrich.rbblfantasy.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface SessionRepository {
    val activeLeague: StateFlow<FantasyLeague?>
    val activeTeam: StateFlow<FantasyTeam?>
    val currentUser: StateFlow<User?>

    fun setActiveLeague(league: FantasyLeague?)
    fun setActiveTeam(team: FantasyTeam?)
    fun setCurrentUser(user: User?)
    fun clearSession()
}

class SessionRepositoryImpl : SessionRepository {
    private val _activeLeague = MutableStateFlow<FantasyLeague?>(null)
    override val activeLeague: StateFlow<FantasyLeague?> = _activeLeague.asStateFlow()

    private val _activeTeam = MutableStateFlow<FantasyTeam?>(null)
    override val activeTeam: StateFlow<FantasyTeam?> = _activeTeam.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    override fun setActiveLeague(league: FantasyLeague?) {
        _activeLeague.value = league
    }

    override fun setActiveTeam(team: FantasyTeam?) {
        _activeTeam.value = team
    }

    override fun setCurrentUser(user: User?) {
        _currentUser.value = user
    }

    override fun clearSession() {
        _activeLeague.value = null
        _activeTeam.value = null
        _currentUser.value = null
    }
}
