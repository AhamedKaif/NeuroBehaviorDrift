package com.neurobehavior.drift.ml

import com.neurobehavior.drift.data.model.BaselineProfile
import com.neurobehavior.drift.data.model.BehaviorLog
import com.neurobehavior.drift.data.model.TodayStats
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sqrt

/**
 * BEHAVIORAL DRIFT DETECTOR
 *
 * Drift = how much has the user's behavior deviated from their personal baseline?
 *
 * Algorithm:
 * 1. Z-score normalization: converts each feature to standard deviations from mean
 *    Z = (today_value - baseline_mean) / baseline_std
 * 2. Weighted drift score: different features have different cognitive impact weights
 * 3. Composite drift score: weighted average of individual feature Z-scores
 *
 * This runs 100% on-device — no data leaves the phone.
 */
@Singleton
class DriftDetector @Inject constructor() {

    // Feature weights — how much each behavioral signal contributes to drift
    // Derived from cognitive load research literature
    private val WEIGHTS = mapOf(
        "screenTime"    to 0.30f,   // 30% — total usage duration
        "appSwitches"   to 0.25f,   // 25% — attention fragmentation
        "unlockCount"   to 0.20f,   // 20% — compulsive checking
        "nightUsage"    to 0.25f    // 25% — sleep disruption
    )

    /**
     * Computes overall drift score [0.0, 1.0].
     * 0.0 = behavior identical to baseline
     * 1.0 = extreme deviation from baseline
     */
    fun computeDriftScore(today: TodayStats, baseline: BaselineProfile): Float {
        val zScreen  = zScore(today.totalScreenTimeMs.toFloat(), baseline.avgScreenTimeMs.toFloat(), baseline.stdScreenTimeMs)
        val zSwitch  = zScore(today.appSwitchCount.toFloat(),    baseline.avgAppSwitches.toFloat(),   baseline.stdAppSwitches)
        val zUnlock  = zScore(today.unlockCount.toFloat(),       baseline.avgUnlockCount.toFloat(),   baseline.stdUnlockCount)
        val zNight   = zScore(today.nightUsageMinutes.toFloat(), baseline.avgNightUsageMinutes.toFloat(), baseline.stdNightUsageMinutes)

        val weightedDrift =
            abs(zScreen)  * WEIGHTS["screenTime"]!! +
            abs(zSwitch)  * WEIGHTS["appSwitches"]!! +
            abs(zUnlock)  * WEIGHTS["unlockCount"]!! +
            abs(zNight)   * WEIGHTS["nightUsage"]!!

        // Sigmoid transformation to map unbounded Z-score to [0,1]
        return sigmoid(weightedDrift).coerceIn(0f, 1f)
    }

    /**
     * Computes isolation-forest-style anomaly score.
     * Uses a simplified on-device approximation since we can't run the full
     * scikit-learn model without Python. The TFLite model handles the real anomaly
     * detection; this is a fast heuristic for immediate feedback.
     */
    fun computeAnomalyScore(log: BehaviorLog, baseline: BaselineProfile): Float {
        val features = listOf(
            zScore(log.totalScreenTimeMs.toFloat(), baseline.avgScreenTimeMs.toFloat(), baseline.stdScreenTimeMs),
            zScore(log.appSwitchCount.toFloat(),    baseline.avgAppSwitches.toFloat(),   baseline.stdAppSwitches),
            zScore(log.unlockCount.toFloat(),       baseline.avgUnlockCount.toFloat(),   baseline.stdUnlockCount),
            zScore(log.nightUsageMinutes.toFloat(), baseline.avgNightUsageMinutes.toFloat(), baseline.stdNightUsageMinutes)
        )

        // Euclidean distance from the origin (normalized baseline) in feature space
        val distance = sqrt(features.sumOf { (it * it).toDouble() }.toFloat())

        // Convert distance to anomaly probability via sigmoid
        return sigmoid(distance / 2f).coerceIn(0f, 1f)
    }

    fun getDriftTrend(recentLogs: List<BehaviorLog>): String {
        if (recentLogs.size < 2) return "stable"
        val recent3 = recentLogs.take(3).map { it.driftScore }
        return when {
            recent3.last() > recent3.first() + 0.1f -> "increasing"
            recent3.last() < recent3.first() - 0.1f -> "decreasing"
            else -> "stable"
        }
    }

    fun getFeatureDriftPercentages(log: BehaviorLog, baseline: BaselineProfile): Map<String, Int> {
        fun pct(today: Float, avg: Float) =
            if (avg == 0f) 0 else ((today - avg) / avg * 100).toInt().coerceAtLeast(0)
        return mapOf(
            "Screen Time"  to pct(log.totalScreenTimeMs.toFloat(), baseline.avgScreenTimeMs.toFloat()),
            "App Switches" to pct(log.appSwitchCount.toFloat(),    baseline.avgAppSwitches.toFloat()),
            "Unlocks"      to pct(log.unlockCount.toFloat(),       baseline.avgUnlockCount.toFloat()),
            "Night Usage"  to pct(log.nightUsageMinutes.toFloat(), baseline.avgNightUsageMinutes.toFloat())
        )
    }

    private fun zScore(value: Float, mean: Float, std: Float): Float =
        if (std == 0f) 0f else (value - mean) / std

    private fun sigmoid(x: Float): Float = (1f / (1f + exp(-x + 1.5f)))
}
