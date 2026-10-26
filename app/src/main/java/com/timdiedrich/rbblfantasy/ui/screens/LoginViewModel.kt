package com.timdiedrich.rbblfantasy.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.timdiedrich.rbblfantasy.RbblFantasyApplication
import com.timdiedrich.rbblfantasy.data.local.TokenManager
import com.timdiedrich.rbblfantasy.data.model.LoginRequest
import com.timdiedrich.rbblfantasy.data.repository.RbblRepository
import com.timdiedrich.rbblfantasy.data.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val hasLeagues: Boolean = false,
    val errorMessage: String? = null,
    val usernameInput: String = "",
    val passwordInput: String = ""
)

class LoginViewModel(
    private val repository: RbblRepository,
    private val sessionRepository: SessionRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsernameChanged(username: String) {
        _uiState.update { it.copy(usernameInput = username, errorMessage = null) }
    }

    fun onPasswordChanged(newPassword: String){
        _uiState.update { it.copy(passwordInput = newPassword, errorMessage = null) }
    }

    fun onLoginClick() {
        val username = _uiState.value.usernameInput.trim()
        val password = _uiState.value.passwordInput

        if (username.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please fill in all fields") }
            return
        }

        viewModelScope.launch{

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val request = LoginRequest(username = username, pw = password)

            repository.login(request)
                .onSuccess { response ->
                    tokenManager.saveToken(response.token)
                    sessionRepository.setCurrentUser(response.user)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = false,
                            errorMessage = error.localizedMessage ?: "Login failed"
                        )
                    }
                }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as RbblFantasyApplication)
                LoginViewModel(
                    repository = application.repository,
                    sessionRepository = application.sessionRepository,
                    tokenManager = application.tokenManager
                )
            }
        }
    }
}