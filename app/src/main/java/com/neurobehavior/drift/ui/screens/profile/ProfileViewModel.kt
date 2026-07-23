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
    val username: String = "",
    val email: String = "",
    val createdAt: String = "",
    val age: String = "",
    val gender: String = "Prefer not to say",
    val occupation: String = "Student",
    val institution: String = "",
    val department: String = "",
    val academicYear: String = "",
    val workingHours: String = "",
    val avgScreenTime: String = "",
    val avgSleepHours: String = "",
    val preferredWorkTime: String = "Morning",
    val stressLevel: Int = 5,
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

                    _uiState.update { it.copy(
                        isLoading = false,
                        fullName = account.optString("full_name", ""),
                        username = account.optString("username", ""),
                        email = account.optString("email", ""),
                        createdAt = account.optString("created_at", ""),
                        age = profile.optString("age", ""),
                        gender = profile.optString("gender", "Prefer not to say"),
                        occupation = profile.optString("occupation", "Student"),
                        institution = profile.optString("institution", ""),
                        department = profile.optString("department", ""),
                        academicYear = profile.optString("academic_year", ""),
                        workingHours = profile.optString("working_hours", ""),
                        avgScreenTime = profile.optString("avg_screen_time", ""),
                        avgSleepHours = profile.optString("avg_sleep_hours", ""),
                        preferredWorkTime = profile.optString("preferred_work_time", "Morning"),
                        stressLevel = profile.optInt("stress_level", 5)
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

    fun updateAllFields(
        fullName: String,
        age: String,
        occupation: String,
        gender: String,
        institution: String,
        department: String,
        academicYear: String,
        workingHours: String,
        avgScreenTime: String,
        avgSleepHours: String,
        preferredWorkTime: String,
        stressLevel: Int
    ) {
        _uiState.update { it.copy(
            fullName = fullName,
            age = age,
            occupation = occupation,
            gender = gender,
            institution = institution,
            department = department,
            academicYear = academicYear,
            workingHours = workingHours,
            avgScreenTime = avgScreenTime,
            avgSleepHours = avgSleepHours,
            preferredWorkTime = preferredWorkTime,
            stressLevel = stressLevel
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
                put("gender", state.gender)
                put("institution", state.institution)
                put("department", state.department)
                put("academic_year", state.academicYear)
                put("working_hours", state.workingHours.toDoubleOrNull() ?: 0.0)
                put("avg_screen_time", state.avgScreenTime.toDoubleOrNull() ?: 0.0)
                put("avg_sleep_hours", state.avgSleepHours.toDoubleOrNull() ?: 0.0)
                put("preferred_work_time", state.preferredWorkTime)
                put("stress_level", state.stressLevel)
            }

            when (val res = networkClient.post("/profile", payload, token)) {
                is NetworkResult.Success -> {
                    prefs.updateProfileInfo(state.fullName)
                    _uiState.update { it.copy(
                        isLoading = false,
                        isEditing = false,
                        successMessage = "Profile updated successfully"
                    ) }
                    loadProfile() // refresh
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

    fun deleteAccount(onSuccess: () -> Unit) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val p = prefs.userPreferencesFlow.first()
            val token = p.jwtToken
            when (val res = networkClient.delete("/api/profile/account", token)) {
                is NetworkResult.Success -> {
                    prefs.clearAuth()
                    onSuccess()
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
