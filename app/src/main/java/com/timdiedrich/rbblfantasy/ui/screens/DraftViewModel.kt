package com.timdiedrich.rbblfantasy.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.timdiedrich.rbblfantasy.RbblFantasyApplication
import com.timdiedrich.rbblfantasy.data.model.DraftPlayer
import com.timdiedrich.rbblfantasy.data.model.DraftTeamOrder
import com.timdiedrich.rbblfantasy.data.model.DraftUiState
import com.timdiedrich.rbblfantasy.data.repository.RbblRepository
import io.socket.client.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import io.socket.client.Socket
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.Duration
import java.time.Instant

class DraftViewModel(
    private val repository: RbblRepository
)  : ViewModel() {

    private val _uiState = MutableStateFlow(DraftUiState())
    val uiState: StateFlow<DraftUiState> = _uiState.asStateFlow()

    private val _secondsRemaining = MutableStateFlow(0L)
    val secondsRemaining: StateFlow<Long> = _secondsRemaining.asStateFlow()

    private var socket: Socket? = null

    fun connectToDraftRoom(serverUrl: String, jwtToken: String, leagueId: String, userTeamId: String) {
        viewModelScope.launch {
            repository.getDraftOrder(leagueId)
                .onSuccess { order ->
                    _uiState.update { it.copy(draftOrder = order) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.localizedMessage) }
                }
        }

        _uiState.update { it.copy(userTeamId = userTeamId) }

        try {
            val options = IO.Options().apply {
                auth = mapOf("token" to "Bearer $jwtToken")
            }
            socket = IO.socket(serverUrl, options)

            socket?.on(Socket.EVENT_CONNECT) {
                _uiState.update { it.copy(isConnected = true) }

                // Join Draft Room
                val joinDate = JSONObject().apply{ put("leagueId" ,leagueId)}
                socket?.emit("draft:join_room", joinDate)
            }
            socket?.on(Socket.EVENT_DISCONNECT) {
                _uiState.update { it.copy(isConnected = false) }
            }

            // Connection Error Handling
            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                val err = if (args.isNotEmpty() && args[0] is Exception) {
                    (args[0] as Exception).localizedMessage
                } else {
                    args.getOrNull(0)?.toString() ?: "Connection error"
                }
                Log.e("DraftViewModel", "Socket connection error: $err")
                _uiState.update { it.copy(isConnected = false, errorMessage = err) }
                }

            socket?.on("draft:started") { args ->
                if (args.isNotEmpty() && args[0] is JSONObject) {
                    val data = args[0] as JSONObject
                    _uiState.update { state ->
                        state.copy(
                            turnState = state.turnState.copy(status = "IN_PROGRESS"),
                        )
                    }
                    handleTurnUpdate(data)
                }
            }

            socket?.on("draft:pick_made") { args ->
                if (args.isNotEmpty() && args[0] is JSONObject) {
                    val data = args[0] as JSONObject
                    val draftedPlayerId = data.optString("fantasyPlayerId", "")

                    _uiState.update { state ->
                        val updatedPlayers = state.availablePlayers.filterNot {
                            it.id == draftedPlayerId
                        }

                        val isCompleted = data.optString("status") == "COMPLETED"

                        state.copy(
                            availablePlayers = updatedPlayers,
                            turnState = state.turnState.copy(
                                status = if (isCompleted) "COMPLETED" else state.turnState.status
                            )
                        )

                    }
                    if (data.has("nextTurn")) {
                        handleTurnUpdate(data.getJSONObject("nextTurn"))
                    }
                }
            }

            socket?.on("draft:error") { args ->
                if (args.isNotEmpty() && args[0] is JSONObject) {
                    val err = (args[0] as JSONObject).optString("message", "An error occurred")
                    _uiState.update { it.copy(errorMessage = err) }
                }
            }

            // Listen for initial room state on join
            socket?.on("draft:room_state") { args ->
                if (args.isNotEmpty() && args[0] is JSONObject) {
                    val json = args[0] as JSONObject

                    val status = json.optString("status", "ERROR_FALLBACK")
                    //setStatus(status)
                    val scheduleFor = json.optString("scheduled_for", "ERROR_SCHEDULEDFOR")

                    // Parse Available Players
                    val playerList = mutableListOf<DraftPlayer>()
                    if (json.has("availablePlayers")) {
                        val playersArray: JSONArray = json.getJSONArray("availablePlayers")
                        Log.d("DraftViewModel", "Available Players: $playersArray")
                        for (i in 0 until playersArray.length()) {
                            val p = playersArray.getJSONObject(i)
                            playerList.add(
                                DraftPlayer(
                                    id = p.optString("id", "Unknown Player"),
                                    name = p.optString("name", "Unknown Player"),
                                    sportClass = p.optDouble("sportClass", 0.0),
                                    bonus = p.optDouble("bonus", 0.0),
                                    position = p.optString("position", null),
                                    isDrafted = p.optBoolean("isDrafted", false)
                                )
                            )
                        }
                    }

                    // Parse Current Turn State
                    val turnData = if (json.has("currentTurn")) json.getJSONObject("currentTurn") else json
                    handleTurnUpdate(turnData)

                    if (status == "SCHEDULED" || status == "IN_PROGRESS" || status == "COMPLETED") {
                        viewModelScope.launch {
                            repository.getDraftOrder(leagueId)
                                .onSuccess { order ->
                                    _uiState.update { it.copy(draftOrder = order) }
                                }
                                .onFailure { error ->
                                    _uiState.update { it.copy(errorMessage = error.localizedMessage) }
                                }
                        }

                    }

                    // Update UI State
                    _uiState.update { state ->
                        state.copy(
                            availablePlayers = playerList,
                            turnState = state.turnState.copy(status = status),
                            scheduledFor = scheduleFor

                        )
                    }
                }
            }

            socket?.connect()
            startTimerCountdown()
        } catch (e: Exception) {
            Log.d("DraftViewModel", "Error connecting to socket: ${e.message}")
            _uiState.update { it.copy(errorMessage = e.localizedMessage) }
        }
    }

    private fun parseDraftOrder(json: JSONObject): List<DraftTeamOrder> {
        val draftOrder = mutableListOf<DraftTeamOrder>()
        if (json.has("draftOrder")) {
            val orderArray = json.getJSONArray("draftOrder")
            for (i in 0 until orderArray.length()) {
                val team = orderArray.getJSONObject(i)
                draftOrder.add(
                    DraftTeamOrder(
                        leagueId = team.optInt("leagueId"),
                        fantasyTeamId = team.optString("teamId"),
                        fantasyTeamName = team.optString("teamName"),
                        draftPosition = team.optInt("draftPosition")
                    )
                )
            }
        }
        return draftOrder
    }

    private fun handleTurnUpdate(json: JSONObject) {
        val round = json.optInt("round", json.optInt("currentRound", 1))
        val pickNum = json.optInt("pickNumber", json.optInt("currentPickNumber", 1))


        val activeTeamId = json.optString("fantasyTeamId", json.optString("currentFantasyTeamId", ""))
        val activeTeamName = json.optString("teamName", json.optString("currentTeamName", ""))
        val deadline = json.optString("pickDeadline", "")

        val eligibleClasses = mutableListOf<Double>()
        if (json.has("eligibleSportClasses")) {
            val array: JSONArray = json.getJSONArray("eligibleSportClasses")
            for (i in 0 until array.length()) {
                eligibleClasses.add(array.getDouble(i))
            }
        }
        _uiState.update { state ->
            state.copy(
                turnState = state.turnState.copy(
                    currentRound = round,
                    currentPickNumber = pickNum,
                    currentFantasyTeamId = activeTeamId,
                    currentTeamName = activeTeamName.ifBlank {state.turnState.currentTeamName},
                    pickDeadlineIso = deadline,
                    eligibleSportClasses = eligibleClasses
                )
            )
        }
    }
    private fun startTimerCountdown() {
        viewModelScope.launch {
            while (true) {
                val deadlineIso = _uiState.value.turnState.pickDeadlineIso
                if (!deadlineIso.isNullOrEmpty()) {
                    try {
                        val deadline = Instant.parse(deadlineIso)
                        val now = Instant.now()
                        val diff = Duration.between(now, deadline).seconds
                        _secondsRemaining.value = if (diff > 0) diff else 0
                    } catch (_: Exception) {
                        _secondsRemaining.value = 0
                    }
                } else {
                    _secondsRemaining.value = 0
                }
                delay(1000)
            }
        }
    }

    fun makePick(leagueId: String, playerId: String) {
        val payload = JSONObject().apply {
            put("leagueId", leagueId)
            put("fantasyPlayerId", playerId)
        }
        socket?.emit("draft:make_pick", payload)
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        socket?.disconnect()
        socket?.off()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RbblFantasyApplication)
                DraftViewModel(
                    repository = application.repository
                )
            }
        }
    }
}