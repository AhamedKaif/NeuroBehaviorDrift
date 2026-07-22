package com.neurobehavior.drift.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neurobehavior.drift.data.network.NetworkClient
import com.neurobehavior.drift.data.network.NetworkResult
import com.neurobehavior.drift.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val registrationError: String? = null,
    val isAuthenticated: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val networkClient: NetworkClient,
    private val prefs: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(username: String, password: String, onSuccess: () -> Unit) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Username and password are required") }
            return
        }

        if (username.length > 50 || username.contains("<") || username.contains(" ")) {
            _uiState.update { it.copy(errorMessage = "Invalid username format") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val payload = JSONObject().apply {
                put("username", username)
                put("password", password)
            }
            when (val res = networkClient.post("/login", payload)) {
                is NetworkResult.Success -> {
                    val token = res.data.getString("token")
                    val userObj = res.data.getJSONObject("user")
                    val email = userObj.optString("email", "")
                    val fullName = userObj.optString("full_name", "")
                    prefs.saveAuth(token, username, fullName, email)
                    _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
                    onSuccess()
                }
                is NetworkResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                }
            }
        }
    }

    fun register(
        fullName: String,
        email: String,
        username: String,
        password: String,
        age: String,
        occupation: String,
        onSuccess: () -> Unit
    ) {
        if (fullName.isBlank() || email.isBlank() || username.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(registrationError = "Full name, email, username, and password are required") }
            return
        }

        val ageInt = age.toIntOrNull()
        if (ageInt == null || ageInt !in 0..120) {
            _uiState.update { it.copy(registrationError = "Age must be between 0 and 120") }
            return
        }

        _uiState.update { it.copy(isLoading = true, registrationError = null) }
        viewModelScope.launch {
            val payload = JSONObject().apply {
                put("full_name", fullName)
                put("email", email)
                put("username", username)
                put("password", password)
                put("age", ageInt)
                put("occupation", occupation)
            }
            when (val res = networkClient.post("/register", payload)) {
                is NetworkResult.Success -> {
                    val token = res.data.getString("token")
                    val userObj = res.data.getJSONObject("user")
                    val retEmail = userObj.optString("email", "")
                    val retFullName = userObj.optString("full_name", "")
                    prefs.saveAuth(token, username, retFullName, retEmail)
                    _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
                    onSuccess()
                }
                is NetworkResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false, registrationError = res.message) }
                }
            }
        }
    }

    fun clearErrors() {
        _uiState.update { it.copy(errorMessage = null, registrationError = null) }
    }
}
