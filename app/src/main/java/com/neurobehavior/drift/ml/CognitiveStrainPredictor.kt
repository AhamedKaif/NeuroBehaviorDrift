package com.neurobehavior.drift.ml

import android.content.Context
import android.util.Log
import com.neurobehavior.drift.data.model.TodayStats
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TENSORFLOW LITE INFERENCE ENGINE
 *
 * Loads the cognitive_strain_model.tflite file from the app's assets folder
 * and runs inference to predict cognitive strain score from behavioral features.
 *
 * Model input:  [screenTimeHours, appSwitches, unlockCount, nightUsageMins, driftScore, dayOfWeek]
 * Model output: [strainScore]  — float in [0.0, 1.0]
 *
 * If the TFLite model file is not found (e.g., during development before training),
 * it falls back to a rule-based heuristic so the app still functions.
 *
 * TO ADD THE REAL MODEL:
 *  1. Train the Python model (see ml-model/scripts/train_model.py)
 *  2. Copy the output cognitive_strain_model.tflite to:
 *     android-app/app/src/main/assets/cognitive_strain_model.tflite
 *  3. Rebuild the app
 */
@Singleton
class CognitiveStrainPredictor @Inject constructor(@ApplicationContext private val context: Context) {

    private val TAG = "CognitiveStrainPredictor"
    private val MODEL_FILE = "cognitive_strain_model.tflite"

    // Number of input features the model expects
    private val NUM_FEATURES = 6

    // Feature normalization ranges (must match Python preprocessing)
    private val NORM_RANGES = mapOf(
        "screenTime"   to Pair(0f, 12f),     // 0–12 hours
        "appSwitches"  to Pair(0f, 200f),    // 0–200 switches
        "unlockCount"  to Pair(0f, 100f),    // 0–100 unlocks
        "nightUsage"   to Pair(0f, 120f),    // 0–120 minutes
        "driftScore"   to Pair(0f, 1f),      // Already normalized
        "dayOfWeek"    to Pair(0f, 6f)       // 0=Monday, 6=Sunday
    )

    private var interpreter: Interpreter? = null

    init {
        loadModel()
    }

    private fun loadModel() {
        try {
            val modelBuffer = loadModelFile()
            val options = Interpreter.Options().apply {
                setNumThreads(2)
                setUseXNNPACK(true)      // Hardware acceleration for ARM chips
            }
            interpreter = Interpreter(modelBuffer, options)
            Log.i(TAG, "TFLite model loaded successfully")
        } catch (e: Exception) {
            Log.w(TAG, "TFLite model not found — using heuristic fallback. Error: ${e.message}")
            // App continues working with rule-based prediction
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(MODEL_FILE)
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            assetFileDescriptor.startOffset,
            assetFileDescriptor.declaredLength
        )
    }

    /**
     * Main prediction function.
     * Normalizes input features, runs TFLite inference, returns strain score.
     */
    fun predict(stats: TodayStats, driftScore: Float): Float {
        val tflite = interpreter
        return if (tflite != null) {
            runTFLiteInference(tflite, stats, driftScore)
        } else {
            heuristicPrediction(stats, driftScore)
        }
    }

    private fun runTFLiteInference(
        tflite: Interpreter,
        stats: TodayStats,
        driftScore: Float
    ): Float {
        // Build normalized input tensor
        val inputBuffer = ByteBuffer.allocateDirect(NUM_FEATURES * 4).apply {
            order(ByteOrder.nativeOrder())
            // Feature 1: Screen time in hours (normalized 0–1)
            putFloat(normalize(stats.totalScreenTimeMs / 3_600_000f, 0f, 12f))
            // Feature 2: App switches (normalized 0–1)
            putFloat(normalize(stats.appSwitchCount.toFloat(), 0f, 200f))
            // Feature 3: Unlock count (normalized 0–1)
            putFloat(normalize(stats.unlockCount.toFloat(), 0f, 100f))
            // Feature 4: Night usage minutes (normalized 0–1)
            putFloat(normalize(stats.nightUsageMinutes.toFloat(), 0f, 120f))
            // Feature 5: Drift score (already [0,1])
            putFloat(driftScore)
            // Feature 6: Day of week (Monday=0, Sunday=6, normalized)
            val dayOfWeek = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) - 1
            putFloat(normalize(dayOfWeek.toFloat(), 0f, 6f))
        }

        // Output tensor: single float — strain score
        val outputBuffer = Array(1) { FloatArray(1) }

        tflite.run(inputBuffer, outputBuffer)
        return outputBuffer[0][0].coerceIn(0f, 1f)
    }

    /**
     * Heuristic fallback when TFLite model is not available.
     * Uses domain knowledge from cognitive load research.
     * Replace with the trained TFLite model for production accuracy.
     */
    private fun heuristicPrediction(stats: TodayStats, driftScore: Float): Float {
        var score = 0f

        // Screen time contribution (>6h is high strain)
        val hours = stats.totalScreenTimeMs / 3_600_000f
        score += when {
            hours > 8f -> 0.35f
            hours > 6f -> 0.25f
            hours > 4f -> 0.15f
            else -> 0.05f
        }

        // App switching contribution (>80 switches is fragmenting)
        score += when {
            stats.appSwitchCount > 120 -> 0.25f
            stats.appSwitchCount > 80  -> 0.18f
            stats.appSwitchCount > 50  -> 0.10f
            else -> 0.03f
        }

        // Unlock count contribution (>60 is compulsive)
        score += when {
            stats.unlockCount > 80 -> 0.20f
            stats.unlockCount > 60 -> 0.14f
            stats.unlockCount > 40 -> 0.08f
            else -> 0.02f
        }

        // Night usage contribution
        score += when {
            stats.nightUsageMinutes > 60 -> 0.15f
            stats.nightUsageMinutes > 30 -> 0.10f
            stats.nightUsageMinutes > 15 -> 0.05f
            else -> 0f
        }

        // Drift contribution
        score += driftScore * 0.20f

        return score.coerceIn(0f, 1f)
    }

    private fun normalize(value: Float, min: Float, max: Float): Float =
        if (max == min) 0f else ((value - min) / (max - min)).coerceIn(0f, 1f)

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}
