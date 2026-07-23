package com.neurobehavior.drift.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neurobehavior.drift.data.model.AppUsageData
import com.neurobehavior.drift.data.network.NetworkClient
import com.neurobehavior.drift.data.network.NetworkResult
import com.neurobehavior.drift.data.preferences.UserPreferencesRepository
import com.neurobehavior.drift.data.repository.BehaviorRepository
import com.neurobehavior.drift.ml.CognitiveStrainPredictor
import com.neurobehavior.drift.ml.DriftDetector
import com.neurobehavior.drift.tracking.UsageStatsCollector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

enum class StrainLevel { LOW, MODERATE, HIGH, CRITICAL }

data class AlertItem(
    val id: Int,
    val title: String,
    val message: String,
    val severity: String,
    val createdAt: String
)

data class TimeseriesItem(
    val timeLabel: String,
    val driftScore: Float,
    val typingErrorRate: Float
)

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
    val error: String? = null,

    // Backend synchronization matching website
    val username: String = "",
    val token: String = "",
    val strainLabel: String = "Low",
    val strainProbability: Float = 1.0f,
    val alerts: List<AlertItem> = emptyList(),
    val timeseries: List<TimeseriesItem> = emptyList(),
    val baselineMetrics: Map<String, Double> = emptyMap(),
    val currentMetrics: Map<String, Double> = emptyMap()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val behaviorRepository: BehaviorRepository,
    private val usageStatsCollector: UsageStatsCollector,
    private val strainPredictor: CognitiveStrainPredictor,
    private val driftDetector: DriftDetector,
    private val networkClient: NetworkClient,
    private val prefs: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Collect preferences
                val userPrefs = prefs.userPreferencesFlow.first()
                val token = userPrefs.jwtToken
                val username = userPrefs.username

                if (!usageStatsCollector.hasUsagePermission()) {
                    _uiState.update { it.copy(isLoading = false, permissionGranted = false, username = username, token = token) }
                    return@launch
                }

                val stats = usageStatsCollector.collectTodayStats()
                behaviorRepository.saveBehaviorLog(stats)
                val localBaseline = behaviorRepository.getBaselineProfile()
                val localDrift = if (localBaseline != null) driftDetector.computeDriftScore(stats, localBaseline) else 0f
                val localStrain = strainPredictor.predict(stats, localDrift)
                val localLevel = when {
                    localStrain < 0.3f -> StrainLevel.LOW
                    localStrain < 0.6f -> StrainLevel.MODERATE
                    localStrain < 0.8f -> StrainLevel.HIGH
                    else          -> StrainLevel.CRITICAL
                }

                // If logged in, fetch from backend API to get website-synchronized diagnostics
                if (token.isNotBlank()) {
                    val dashRes = networkClient.get("/api/dashboard", token)
                    val alertRes = networkClient.get("/api/notifications?severity=ALL", token)

                    if (dashRes is NetworkResult.Success && alertRes is NetworkResult.Success) {
                        val dashData = dashRes.data
                        val alertArray = alertRes.data.optJSONArray("alerts") ?: alertRes.data.names()?.let { alertRes.data.toJSONArray(it) } ?: org.json.JSONArray()

                        val parsedAlerts = mutableListOf<AlertItem>()
                        val actualArray = if (alertRes.data.has("alerts")) alertRes.data.getJSONArray("alerts") else alertRes.data.optJSONArray("alerts") ?: alertRes.data.names()?.let { alertRes.data.toJSONArray(it) } ?: org.json.JSONArray()
                        
                        // Parse alerts list
                        val targetArray = if (alertRes.data.has("alerts")) {
                            alertRes.data.getJSONArray("alerts")
                        } else {
                            // If array is direct or nested
                            if (alertRes.data is org.json.JSONArray) alertRes.data as org.json.JSONArray else org.json.JSONArray(alertRes.data.toString())
                        }
                        
                        // Let's safe-parse alerts
                        val isDirectArray = alertRes.data.optJSONArray("alerts") == null && alertRes.data.toString().startsWith("[")
                        val arrayToParse = if (isDirectArray) org.json.JSONArray(alertRes.data.toString()) else alertRes.data.optJSONArray("alerts") ?: org.json.JSONArray()
                        
                        for (i in 0 until arrayToParse.length()) {
                            val obj = arrayToParse.getJSONObject(i)
                            parsedAlerts.add(
                                AlertItem(
                                    id = obj.optInt("id", 0),
                                    title = obj.optString("title", "Alert"),
                                    message = obj.optString("message", ""),
                                    severity = obj.optString("severity", "LOW"),
                                    createdAt = obj.optString("created_at", "")
                                )
                            )
                        }

                        // Parse timeseries list
                        val timeSeriesList = mutableListOf<TimeseriesItem>()
                        val tsArray = dashData.optJSONArray("timeseries") ?: org.json.JSONArray()
                        for (i in 0 until tsArray.length()) {
                            val obj = tsArray.getJSONObject(i)
                            timeSeriesList.add(
                                TimeseriesItem(
                                    timeLabel = obj.optString("time_label", ""),
                                    driftScore = obj.optDouble("drift_score", 0.0).toFloat(),
                                    typingErrorRate = obj.optDouble("typing_error_rate", 0.0).toFloat()
                                )
                            )
                        }

                        val latestPred = dashData.optJSONObject("latest_prediction")
                        val strainLabel = latestPred?.optString("strain_label", "Low") ?: "Low"
                        val strainProb = latestPred?.optDouble("strain_probability", 1.0)?.toFloat() ?: 1.0f
                        val driftVal = latestPred?.optDouble("drift_score", 0.0)?.toFloat() ?: 0.0f
                        val cognitiveStrainVal = if (strainLabel == "Low") 0.15f else if (strainLabel == "Medium") 0.45f else 0.75f

                        val level = when (strainLabel) {
                            "Low" -> StrainLevel.LOW
                            "Medium", "Moderate" -> StrainLevel.MODERATE
                            "High" -> StrainLevel.HIGH
                            else -> StrainLevel.CRITICAL
                        }

                        // Extract baseline metrics
                        val baseMetricsObj = dashData.optJSONObject("baseline")
                        val baselineMap = mutableMapOf<String, Double>()
                        baseMetricsObj?.keys()?.forEach { key ->
                            baselineMap[key] = baseMetricsObj.optDouble(key, 0.0)
                        }

                        // Extract current metrics
                        val curMetricsObj = dashData.optJSONObject("latest_metrics")
                        val currentMap = mutableMapOf<String, Double>()
                        curMetricsObj?.keys()?.forEach { key ->
                            currentMap[key] = curMetricsObj.optDouble(key, 0.0)
                        }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                permissionGranted = true,
                                screenTimeHours = (stats.totalScreenTimeMs / 3_600_000).toInt(),
                                screenTimeMinutes = ((stats.totalScreenTimeMs % 3_600_000) / 60_000).toInt(),
                                appSwitches = stats.appSwitchCount,
                                unlockCount = stats.unlockCount,
                                nightUsageMinutes = stats.nightUsageMinutes,
                                cognitiveStrainScore = cognitiveStrainVal,
                                driftScore = driftVal,
                                strainLevel = level,
                                hasBaseline = baseMetricsObj != null,
                                daysOfData = behaviorRepository.getDaysOfData(),
                                topApps = stats.topApps,
                                recommendations = buildRecommendations(level, stats.nightUsageMinutes, stats.appSwitchCount),
                                username = username,
                                token = token,
                                strainLabel = strainLabel,
                                strainProbability = strainProb,
                                alerts = parsedAlerts,
                                timeseries = timeSeriesList,
                                baselineMetrics = baselineMap,
                                currentMetrics = currentMap
                            )
                        }
                        return@launch
                    }
                }

                // Local Fallback
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        permissionGranted = true,
                        screenTimeHours = (stats.totalScreenTimeMs / 3_600_000).toInt(),
                        screenTimeMinutes = ((stats.totalScreenTimeMs % 3_600_000) / 60_000).toInt(),
                        appSwitches = stats.appSwitchCount,
                        unlockCount = stats.unlockCount,
                        nightUsageMinutes = stats.nightUsageMinutes,
                        cognitiveStrainScore = localStrain,
                        driftScore = localDrift,
                        strainLevel = localLevel,
                        hasBaseline = localBaseline != null,
                        daysOfData = behaviorRepository.getDaysOfData(),
                        topApps = stats.topApps,
                        recommendations = buildRecommendations(localLevel, stats.nightUsageMinutes, stats.appSwitchCount),
                        username = username,
                        token = token,
                        strainLabel = when(localLevel) {
                            StrainLevel.LOW -> "Low"
                            StrainLevel.MODERATE -> "Medium"
                            StrainLevel.HIGH -> "High"
                            StrainLevel.CRITICAL -> "High"
                        },
                        strainProbability = 0.9f
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun transmitMetrics(payload: JSONObject, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val token = prefs.userPreferencesFlow.first().jwtToken
                if (token.isNotBlank()) {
                    when (val res = networkClient.post("/api/metrics", payload, token)) {
                        is NetworkResult.Success -> {
                            loadDashboardData()
                            onSuccess()
                        }
                        is NetworkResult.Failure -> {
                            _uiState.update { it.copy(error = res.message) }
                        }
                    }
                } else {
                    onSuccess() // Offline bypass
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearAlerts(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val token = prefs.userPreferencesFlow.first().jwtToken
                if (token.isNotBlank()) {
                    when (val res = networkClient.post("/api/notifications/read-all", JSONObject(), token)) {
                        is NetworkResult.Success -> {
                            loadDashboardData()
                            onSuccess()
                        }
                        is NetworkResult.Failure -> {
                            _uiState.update { it.copy(error = res.message) }
                        }
                    }
                } else {
                    onSuccess()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun retrainModel(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val token = prefs.userPreferencesFlow.first().jwtToken
                if (token.isNotBlank()) {
                    when (val res = networkClient.post("/api/model/retrain", JSONObject(), token)) {
                        is NetworkResult.Success -> {
                            loadDashboardData()
                            onSuccess()
                        }
                        is NetworkResult.Failure -> {
                            onFailure(res.message)
                        }
                    }
                } else {
                    onFailure("Not connected to backend service.")
                }
            } catch (e: Exception) {
                onFailure(e.message ?: "Unknown error")
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            prefs.clearAuth()
            onSuccess()
        }
    }

    private fun buildRecommendations(level: StrainLevel, nightMins: Int, switches: Int): List<String> =
        buildList {
            when (level) {
                StrainLevel.CRITICAL -> {
                    add("🚨 Critical strain — take a 30-min screen break now.")
                    add("Practice 4-7-8 breathing for immediate relief.")
                }
                StrainLevel.HIGH     -> {
                    add("⚠️ High load — try a 15-min screen-free walk.")
                    add("Enable Do Not Disturb for 1 hour.")
                }
                StrainLevel.MODERATE -> add("📱 Reduce screen time by 30 minutes today.")
                StrainLevel.LOW      -> add("✅ Your cognitive health looks good today!")
            }
            if (nightMins > 30) add("🌙 Avoid screens after 10 PM — it disrupts melatonin.")
            if (switches > 80)  add("🔀 Reduce app switching to improve focus.")
        }

    fun refresh() = loadDashboardData()
}
