package com.neurobehavior.drift.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neurobehavior.drift.data.model.AppUsageData
import com.neurobehavior.drift.data.repository.BehaviorRepository
import com.neurobehavior.drift.ml.CognitiveStrainPredictor
import com.neurobehavior.drift.ml.DriftDetector
import com.neurobehavior.drift.tracking.UsageStatsCollector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class StrainLevel { LOW, MODERATE, HIGH, CRITICAL }

data class DashboardUiState(
    val isLoading: Boolean = true,
    val permissionGranted: Boolean = false,
    val screenTimeHours: Int = 0,
    val screenTimeMinutes: Int = 0,
    val appSwitches: Int = 0,
    val unlockCount: Int = 0,
    val nightUsageMinutes: Int = 0,
    val cognitiveStrainScore: Float = 0f,
    val driftScore: Float = 0f,
    val strainLevel: StrainLevel = StrainLevel.LOW,
    val hasBaseline: Boolean = false,
    val daysOfData: Int = 0,
    val topApps: List<AppUsageData> = emptyList(),
    val recommendations: List<String> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val behaviorRepository: BehaviorRepository,
    private val usageStatsCollector: UsageStatsCollector,
    private val strainPredictor: CognitiveStrainPredictor,
    private val driftDetector: DriftDetector
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init { loadDashboardData() }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                if (!usageStatsCollector.hasUsagePermission()) {
                    _uiState.update { it.copy(isLoading = false, permissionGranted = false) }
                    return@launch
                }
                val stats = usageStatsCollector.collectTodayStats()
                behaviorRepository.saveBehaviorLog(stats)
                val baseline = behaviorRepository.getBaselineProfile()
                val drift = if (baseline != null) driftDetector.computeDriftScore(stats, baseline) else 0f
                val strain = strainPredictor.predict(stats, drift)
                val level = when {
                    strain < 0.3f -> StrainLevel.LOW
                    strain < 0.6f -> StrainLevel.MODERATE
                    strain < 0.8f -> StrainLevel.HIGH
                    else          -> StrainLevel.CRITICAL
                }
                _uiState.update {
                    it.copy(
                        isLoading = false, permissionGranted = true,
                        screenTimeHours   = (stats.totalScreenTimeMs / 3_600_000).toInt(),
                        screenTimeMinutes = ((stats.totalScreenTimeMs % 3_600_000) / 60_000).toInt(),
                        appSwitches       = stats.appSwitchCount,
                        unlockCount       = stats.unlockCount,
                        nightUsageMinutes = stats.nightUsageMinutes,
                        cognitiveStrainScore = strain, driftScore = drift,
                        strainLevel = level, hasBaseline = baseline != null,
                        daysOfData  = behaviorRepository.getDaysOfData(),
                        topApps     = stats.topApps,
                        recommendations = buildRecommendations(level, stats.nightUsageMinutes, stats.appSwitchCount)
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun buildRecommendations(level: StrainLevel, nightMins: Int, switches: Int): List<String> =
        buildList {
            when (level) {
                StrainLevel.CRITICAL -> { add("🚨 Critical strain — take a 30-min screen break now."); add("Practice 4-7-8 breathing for immediate relief.") }
                StrainLevel.HIGH     -> { add("⚠️ High load — try a 15-min screen-free walk."); add("Enable Do Not Disturb for 1 hour.") }
                StrainLevel.MODERATE -> add("📱 Reduce screen time by 30 minutes today.")
                StrainLevel.LOW      -> add("✅ Your cognitive health looks good today!")
            }
            if (nightMins > 30) add("🌙 Avoid screens after 10 PM — it disrupts melatonin.")
            if (switches > 80)  add("🔀 Reduce app switching to improve focus.")
        }

    fun refresh() = loadDashboardData()
}
