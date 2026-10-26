package com.timdiedrich.rbblfantasy.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.timdiedrich.rbblfantasy.RbblFantasyApplication
import com.timdiedrich.rbblfantasy.data.local.TokenManager
import com.timdiedrich.rbblfantasy.data.model.RegisterRequest
import com.timdiedrich.rbblfantasy.data.repository.RbblRepository
import com.timdiedrich.rbblfantasy.data.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val logs: List<String> = emptyList()
)


class AccountViewModel (
    private val repository: RbblRepository,
    private val tokenManager: TokenManager,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun registerUser(username: String, email: String, password: String, confirmPassword: String) {
        addLog("Registration started for: $username")
        if (username.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            val error = "Please fill in all fields"
            addLog("Validation failed: $error")
            _uiState.update { it.copy(errorMessage = error) }
            return
        }
        if (password != confirmPassword) {
            val error = "Passwords do not match"
            addLog("Validation failed: $error")
            _uiState.update { it.copy(errorMessage = error) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            addLog("Sending registration request...")
            val request = RegisterRequest(username = username, email = email, pw = password)

            repository.registerUser(request)
                .onSuccess { response->
                    addLog("Registration successful. Saving token and user...")
                    tokenManager.saveToken(response.token)
                    sessionRepository.setCurrentUser(response.user)
                    addLog("Account setup complete.")
                    _uiState.update { it.copy(isSuccess = true, isLoading = false) }

                }
                .onFailure { exception ->
                    val errorMsg = exception.message
                    addLog("Registration failed: $errorMsg")
                    _uiState.update {
                        it.copy(
                            errorMessage = errorMsg,
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun addLog(message: String) {
        Log.d("AccountViewModel", message)
        _uiState.update { state ->
            state.copy(logs = state.logs + message)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RbblFantasyApplication)
                AccountViewModel(
                    repository = application.repository,
                    tokenManager = application.tokenManager,
                    sessionRepository = application.sessionRepository
                )
            }
        }
    }
}