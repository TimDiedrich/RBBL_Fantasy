package com.timdiedrich.rbblfantasy.data.local

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TokenManager(context: Context) {
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    private val _token = MutableStateFlow(getToken())
    val token: StateFlow<String?> = _token.asStateFlow()

    fun saveToken(token: String) {
        prefs.edit { putString("jwt_token", token) }
        _token.value = token
    }

    fun getToken(): String? {
        return prefs.getString("jwt_token", null)
    }

    fun clearToken() {
        prefs.edit { remove("jwt_token") }
        _token.value = null
    }
}
