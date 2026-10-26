package com.timdiedrich.rbblfantasy.data.repository


import com.timdiedrich.rbblfantasy.data.model.AddToRosterRequest
import com.timdiedrich.rbblfantasy.data.model.AddToRosterResponse
import com.timdiedrich.rbblfantasy.data.model.AuthResponse
import com.timdiedrich.rbblfantasy.data.model.CreateLeagueRequest
import com.timdiedrich.rbblfantasy.data.model.CreateTeamRequest
import com.timdiedrich.rbblfantasy.data.model.DraftTeamOrder
import com.timdiedrich.rbblfantasy.data.model.FantasyLeague
import com.timdiedrich.rbblfantasy.data.model.FantasyTeam
import com.timdiedrich.rbblfantasy.data.model.GetRosterResponse
import com.timdiedrich.rbblfantasy.data.model.LoginRequest
import com.timdiedrich.rbblfantasy.data.model.Match
import com.timdiedrich.rbblfantasy.data.model.FantasyPlayer
import com.timdiedrich.rbblfantasy.data.model.GetRealMatchesResponse
import com.timdiedrich.rbblfantasy.data.model.JoinLeagueRequest
import com.timdiedrich.rbblfantasy.data.model.LeagueAdminSettings
import com.timdiedrich.rbblfantasy.data.model.LeagueMembership
import com.timdiedrich.rbblfantasy.data.model.RegisterRequest
import com.timdiedrich.rbblfantasy.data.model.Roster
import com.timdiedrich.rbblfantasy.data.model.Standing
import com.timdiedrich.rbblfantasy.data.model.UpdateLeagueRequest
import com.timdiedrich.rbblfantasy.data.model.User
import com.timdiedrich.rbblfantasy.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface RbblRepository {
    suspend fun getPlayers(leagueId: Int): Result<List<FantasyPlayer>>
    suspend fun getMatches(): Result<List<Match>>
    suspend fun getStandings(request: FantasyLeague): Result<List<Standing>>

    suspend fun registerUser(request: RegisterRequest): Result<AuthResponse>

    suspend fun login(request: LoginRequest): Result<AuthResponse>

    suspend fun createLeague(name: CreateLeagueRequest): Result<FantasyLeague>

    suspend fun getAllFantasyLeagues(): Result<List<FantasyLeague>>

    suspend fun createTeam(request: CreateTeamRequest): Result<FantasyTeam>

    suspend fun getUserTeamForActiveLeague(): Result<FantasyTeam?>

    suspend fun getAllUserTeams(): Result<List<FantasyTeam>>

    suspend fun getRoster(teamId: Int): Result<GetRosterResponse>

    suspend fun getUndrafted(leagueId: Int): Result<List<FantasyPlayer>>

    suspend fun addToRoster(addToRosterRequest: AddToRosterRequest): Result<AddToRosterResponse>

    suspend fun getLeague(leagueId: Int): Result<FantasyLeague>

    suspend fun updateLeague(leagueId: Int, request: UpdateLeagueRequest): Result<FantasyLeague>

    suspend fun getCurrentUser(): Result<User>

    suspend fun getRealMatches(): Result<GetRealMatchesResponse>

    suspend fun getRealMatchesByWeek(week: Int): Result<GetRealMatchesResponse>

    suspend fun joinLeague(inviteCode: String): Result<FantasyLeague>

    suspend fun getMyLeagueMemberships(): Result<List<LeagueMembership>>

    suspend fun getLeagueAdminSettings(leagueId: Int): Result<LeagueAdminSettings>

    suspend fun saveRoster(roster: List<AddToRosterRequest>): Result<List<Roster>>
    suspend fun getDraftOrder(leagueId: String) : Result<List<DraftTeamOrder>>

    suspend fun getAllLeagueMembers(leagueId: Int): Result<List<LeagueMembership>>
}


class RbblRepositoryImpl(
    private val apiService: ApiService
) : RbblRepository {

    override suspend fun getPlayers(leagueId: Int): Result<List<FantasyPlayer>> = withContext(Dispatchers.IO) {
        runCatching { apiService.getPlayers(leagueId) }
    }

    override suspend fun getMatches(): Result<List<Match>> = withContext(Dispatchers.IO) {
        runCatching { apiService.getMatches() }
    }

    override suspend fun getStandings(request: FantasyLeague): Result<List<Standing>> = withContext(Dispatchers.IO) {
        runCatching { apiService.getStandings(leagueId = request.id).standings }
    }

    override suspend fun registerUser(request: RegisterRequest): Result<AuthResponse> = withContext(Dispatchers.IO) {
        runCatching { apiService.registerUser(request) }
    }

    override suspend fun login(request: LoginRequest): Result<AuthResponse> = withContext(Dispatchers.IO){
        runCatching { apiService.login(request) }
    }


    override suspend fun createLeague(name: CreateLeagueRequest): Result<FantasyLeague> = withContext(Dispatchers.IO) {
        runCatching { apiService.createLeague(name) }
    }

    override suspend fun getAllFantasyLeagues(): Result<List<FantasyLeague>> = withContext(Dispatchers.IO) {
        runCatching { apiService.getAllFantasyLeagues().fantasyLeagues }
    }

    override suspend fun createTeam(request: CreateTeamRequest): Result<FantasyTeam> = withContext(Dispatchers.IO) {
        runCatching { apiService.createTeam(request) }
    }

    override suspend fun getUserTeamForActiveLeague(): Result<FantasyTeam?> = withContext(Dispatchers.IO) {
        runCatching { apiService.getUserTeamForActiveLeague().team }
    }

    override suspend fun getAllUserTeams(): Result<List<FantasyTeam>> = withContext(Dispatchers.IO) {
        runCatching { apiService.getAllUserTeams().teams }
    }

    override suspend fun getRoster(teamId: Int): Result<GetRosterResponse> = withContext(Dispatchers.IO) {
        runCatching { apiService.getRoster(teamId) }
    }

    override suspend fun getUndrafted(leagueId: Int): Result<List<FantasyPlayer>> = withContext(Dispatchers.IO) {
        runCatching { apiService.getUndrafted(leagueId).fantasyPlayers }
    }

    override suspend fun addToRoster(addToRosterRequest: AddToRosterRequest): Result<AddToRosterResponse> = withContext(Dispatchers.IO) {
        runCatching { apiService.addToRoster(addToRosterRequest = addToRosterRequest) }
    }

    override suspend fun getLeague(leagueId: Int): Result<FantasyLeague> = withContext(Dispatchers.IO) {
        runCatching { apiService.getLeague(leagueId) }
    }

    override suspend fun updateLeague(leagueId: Int, request: UpdateLeagueRequest): Result<FantasyLeague> = withContext(Dispatchers.IO) {
        runCatching { apiService.updateLeague(leagueId, request) }
    }

    override suspend fun getCurrentUser(): Result<User> = withContext(Dispatchers.IO) {
        runCatching { apiService.getCurrentUser() }
    }

    override suspend fun getRealMatches(): Result<GetRealMatchesResponse> = withContext(Dispatchers.IO) {
        runCatching { apiService.getRealMatches() }
    }

    override suspend fun getRealMatchesByWeek(week: Int): Result<GetRealMatchesResponse> = withContext(Dispatchers.IO) {
        runCatching { apiService.getRealMatchesByWeek(week = week) }
    }

    override suspend fun joinLeague(inviteCode: String): Result<FantasyLeague> = withContext(Dispatchers.IO) {
        runCatching { apiService.joinLeague(JoinLeagueRequest(inviteCode)).fantasyLeague }
    }

    override suspend fun getMyLeagueMemberships(): Result<List<LeagueMembership>> = withContext(Dispatchers.IO) {
        runCatching { apiService.getMyLeagueMemberships() }
    }

    override suspend fun getLeagueAdminSettings(leagueId: Int): Result<LeagueAdminSettings> = withContext(Dispatchers.IO) {
        runCatching { apiService.getLeagueAdminSettings(leagueId) }
    }

    override suspend fun saveRoster(roster: List<AddToRosterRequest>): Result<List<Roster>> = withContext(Dispatchers.IO){
        runCatching { apiService.saveRoster(roster).roster }
    }

    override suspend fun getDraftOrder(leagueId: String): Result<List<DraftTeamOrder>> = withContext(Dispatchers.IO) {
        runCatching { apiService.getDraftOrder(leagueId) }
    }

    override suspend fun getAllLeagueMembers(leagueId: Int): Result<List<LeagueMembership>> = withContext(Dispatchers.IO) {
        runCatching { apiService.getAllLeagueMembers(leagueId) }
    }
}
