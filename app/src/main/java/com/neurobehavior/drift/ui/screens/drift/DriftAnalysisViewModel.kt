package com.neurobehavior.drift.ui.screens.drift

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neurobehavior.drift.data.repository.BehaviorRepository
import com.neurobehavior.drift.ml.DriftDetector
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeatureDrift(
    val name: String,
    val baselineValue: String,
    val todayValue: String,
    val driftPct: Int,
    val iconName: String   // "screen", "switch", "unlock", "night"
)

data class DriftUiState(
    val isLoading: Boolean = true,
    val overallDrift: Float = 0f,
    val anomalyScore: Float = 0f,
    val isAnomaly: Boolean = false,
    val trend: String = "stable",
    val featureDrifts: List<FeatureDrift> = emptyList(),
    val baselineValues: List<String> = emptyList(),
    val todayValues: List<String> = emptyList(),
    val hasData: Boolean = false,
    val isListening: Boolean = false,
    val voiceResult: String? = null,
    val voiceError: String? = null
)

@HiltViewModel
class DriftAnalysisViewModel @Inject constructor(
    private val repo: BehaviorRepository,
    private val detector: DriftDetector,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(DriftUiState())
    val state: StateFlow<DriftUiState> = _state.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val today    = repo.getTodayLog()
                val baseline = repo.getBaselineProfile()

                if (today == null || baseline == null) {
                    _state.update { it.copy(isLoading = false, hasData = false) }
                    return@launch
                }

                val drift   = today.driftScore.takeIf { it > 0f }
                    ?: detector.computeDriftScore(
                        com.neurobehavior.drift.data.model.TodayStats(
                            totalScreenTimeMs = today.totalScreenTimeMs,
                            appSwitchCount    = today.appSwitchCount,
                            unlockCount       = today.unlockCount,
                            nightUsageMinutes = today.nightUsageMinutes
                        ), baseline)

                val anomaly = detector.computeAnomalyScore(today, baseline)

                val recent   = repo.getRecentLogs(3)
                val trend    = detector.getDriftTrend(recent)
                val driftMap = detector.getFeatureDriftPercentages(today, baseline)

                val features = listOf(
                    FeatureDrift("Screen Time",  "${baseline.avgScreenTimeMs/3_600_000}h",
                        "${today.totalScreenTimeMs/3_600_000}h", driftMap["Screen Time"]  ?: 0, "screen"),
                    FeatureDrift("App Switches", "${baseline.avgAppSwitches}",
                        "${today.appSwitchCount}", driftMap["App Switches"] ?: 0, "switch"),
                    FeatureDrift("Unlocks",      "${baseline.avgUnlockCount}",
                        "${today.unlockCount}",    driftMap["Unlocks"]      ?: 0, "unlock"),
                    FeatureDrift("Night Usage",  "${baseline.avgNightUsageMinutes}m",
                        "${today.nightUsageMinutes}m", driftMap["Night Usage"] ?: 0, "night")
                )

                _state.update { it.copy(
                    isLoading    = false,
                    hasData      = true,
                    overallDrift = drift,
                    anomalyScore = anomaly,
                    isAnomaly    = anomaly > 0.65f,
                    trend        = trend,
                    featureDrifts= features,
                    baselineValues = listOf(
                        "${baseline.avgScreenTimeMs/3_600_000}h",
                        "${baseline.avgAppSwitches}",
                        "${baseline.avgUnlockCount}",
                        "${baseline.avgNightUsageMinutes}m"
                    ),
                    todayValues = listOf(
                        "${today.totalScreenTimeMs/3_600_000}h",
                        "${today.appSwitchCount}",
                        "${today.unlockCount}",
                        "${today.nightUsageMinutes}m"
                    )
                )}
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, hasData = false) }
            }
        }
    }

    fun startListening() {
        if (_state.value.isListening) return

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _state.update { it.copy(voiceError = "Speech recognition is not available on this device") }
            return
        }

        // Fix: Re-initialize on every start to prevent GSA internal "Busy" state (Error 65561)
        setupSpeechRecognizer()

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }
        
        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _state.update { it.copy(isListening = false, voiceError = "Could not start voice search") }
        }
    }

    private fun setupSpeechRecognizer() {
        try {
            speechRecognizer?.apply {
                cancel()
                destroy()
            }
            
            // Fix: On Android 11+ (API 30+), system role checks can cause RoleControllerManager timeouts.
            // Using on-device recognizer on API 31+ or adding a small delay avoids this.
            val recognizer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                SpeechRecognizer.isOnDeviceRecognitionAvailable(context)) {
                SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
            } else {
                SpeechRecognizer.createSpeechRecognizer(context)
            }
            
            speechRecognizer = recognizer.apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _state.update { it.copy(isListening = true, voiceError = null, voiceResult = null) }
                    }
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        _state.update { it.copy(isListening = false) }
                    }
                    override fun onError(error: Int) {
                        val msg = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                            SpeechRecognizer.ERROR_NETWORK -> "Network error"
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Voice recognizer is busy"
                            SpeechRecognizer.ERROR_SERVER -> "Google server error"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                            else -> {
                                if (error == 65561) {
                                    "Speech service internal error (GSA). Please try again or restart the Google app."
                                } else {
                                    "Voice service error ($error). Please try again."
                                }
                            }
                        }
                        _state.update { it.copy(isListening = false, voiceError = msg) }
                        // Cleanup on error to prevent subsequent attempts from failing with "Busy"
                        speechRecognizer?.cancel()
                    }
                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            _state.update { it.copy(voiceResult = matches[0], voiceError = null) }
                        }
                        _state.update { it.copy(isListening = false) }
                    }
                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        } catch (e: Exception) {
            _state.update { it.copy(voiceError = "Failed to initialize voice service") }
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
        } catch (e: Exception) {}
        _state.update { it.copy(isListening = false) }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.stopListening()
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
