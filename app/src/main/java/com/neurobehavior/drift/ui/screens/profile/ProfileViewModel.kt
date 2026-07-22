package com.neurobehavior.drift.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neurobehavior.drift.data.network.NetworkClient
import com.neurobehavior.drift.data.network.NetworkResult
import com.neurobehavior.drift.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val fullName: String = "",
    val age: String = "",
    val occupation: String = "",
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val networkClient: NetworkClient,
    private val prefs: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val p = prefs.userPreferencesFlow.first()
            val token = p.jwtToken
            if (token.isBlank()) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Not authenticated") }
                return@launch
            }

            when (val res = networkClient.get("/profile", token)) {
                is NetworkResult.Success -> {
                    val account = res.data.getJSONObject("account")
                    val profile = res.data.getJSONObject("profile")
                    
                    val name = account.optString("full_name", "")
                    val ageVal = profile.optString("age", "")
                    val occ = profile.optString("occupation", "")
                    
                    _uiState.update { it.copy(
                        isLoading = false,
                        fullName = name,
                        age = ageVal,
                        occupation = occ
                    ) }
                }
                is NetworkResult.Failure -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = res.message
                    ) }
                }
            }
        }
    }

    fun updateField(name: String, ageVal: String, occ: String) {
        _uiState.update { it.copy(
            fullName = name,
            age = ageVal,
            occupation = occ
        ) }
    }

    fun setEditing(editing: Boolean) {
        _uiState.update { it.copy(isEditing = editing, successMessage = null) }
    }

    fun saveProfile() {
        val state = _uiState.value
        val ageInt = state.age.toIntOrNull()
        if (state.fullName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Full name is required") }
            return
        }
        if (ageInt == null || ageInt !in 0..120) {
            _uiState.update { it.copy(errorMessage = "Age must be between 0 and 120") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
        viewModelScope.launch {
            val p = prefs.userPreferencesFlow.first()
            val token = p.jwtToken
            val payload = JSONObject().apply {
                put("full_name", state.fullName)
                put("age", ageInt)
                put("occupation", state.occupation)
            }

            when (val res = networkClient.post("/profile", payload, token)) {
                is NetworkResult.Success -> {
                    prefs.updateProfileInfo(state.fullName)
                    _uiState.update { it.copy(
                        isLoading = false,
                        isEditing = false,
                        successMessage = "Profile updated successfully"
                    ) }
                }
                is NetworkResult.Failure -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = res.message
                    ) }
                }
            }
        }
    }
}
