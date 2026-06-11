package com.neurobehavior.drift.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neurobehavior.drift.data.preferences.UserPreferencesRepository
import com.neurobehavior.drift.data.repository.BehaviorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val strainAlertsEnabled: Boolean = true,
    val strainThreshold: Float = 0.70f,
    val nightAlertsEnabled: Boolean = true,
    val trackingEnabled: Boolean = true,
    val retentionDays: Int = 30
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: BehaviorRepository,
    private val prefs: UserPreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.userPreferencesFlow.collect { p ->
                _state.update { it.copy(
                    strainAlertsEnabled = p.strainAlertsEnabled,
                    strainThreshold     = p.strainThreshold,
                    nightAlertsEnabled  = p.nightAlertsEnabled,
                    trackingEnabled     = p.trackingEnabled
                )}
            }
        }
    }

    fun setStrainAlerts(v: Boolean)   { viewModelScope.launch { prefs.setStrainAlertsEnabled(v) } }
    fun setThreshold(v: Float)        { viewModelScope.launch { prefs.setStrainThreshold(v) } }
    fun setNightAlerts(v: Boolean)    { viewModelScope.launch { prefs.setNightAlertsEnabled(v) } }
    fun setTracking(v: Boolean)       { viewModelScope.launch { prefs.setTrackingEnabled(v) } }
    fun clearAllData()                { viewModelScope.launch { repo.clearAllData() } }
}
