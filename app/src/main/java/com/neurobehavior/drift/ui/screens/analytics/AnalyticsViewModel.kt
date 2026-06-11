package com.neurobehavior.drift.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neurobehavior.drift.data.repository.BehaviorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val labels: List<String> = emptyList(),
    val strainData: List<Float> = emptyList(),
    val screenData: List<Float> = emptyList(),
    val switchData: List<Int> = emptyList(),
    val driftData: List<Float> = emptyList(),
    val avgScreenH: String = "0.0",
    val avgStrain: Float = 0f,
    val avgSwitches: Int = 0,
    val peakDay: String = "N/A",
    val bestDay: String = "N/A"
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repo: BehaviorRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AnalyticsUiState())
    val state: StateFlow<AnalyticsUiState> = _state.asStateFlow()
    init { load(7) }
    fun load(days: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val logs = repo.getRecentLogs(days).reversed()
                val fmt = SimpleDateFormat("MMM d", Locale.getDefault())
                val labels  = logs.map { fmt.format(Date(it.timestamp)) }
                val strain  = logs.map { it.strainScore }
                val screen  = logs.map { it.totalScreenTimeMs / 3_600_000f }
                val switches= logs.map { it.appSwitchCount }
                val drift   = logs.map { it.driftScore }
                val peakIdx = strain.indices.maxByOrNull { strain[it] } ?: 0
                val bestIdx = strain.indices.minByOrNull { strain[it] } ?: 0
                _state.update { it.copy(
                    isLoading = false, labels = labels,
                    strainData = strain, screenData = screen,
                    switchData = switches, driftData = drift,
                    avgScreenH = String.format("%.1f", screen.average()),
                    avgStrain = strain.average().toFloat(),
                    avgSwitches = switches.average().toInt(),
                    peakDay = labels.getOrElse(peakIdx) { "N/A" },
                    bestDay = labels.getOrElse(bestIdx) { "N/A" }
                )}
            } catch (e: Exception) { _state.update { it.copy(isLoading = false) } }
        }
    }
}
