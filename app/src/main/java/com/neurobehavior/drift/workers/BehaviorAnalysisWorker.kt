package com.neurobehavior.drift.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.neurobehavior.drift.data.repository.BehaviorRepository
import com.neurobehavior.drift.ml.CognitiveStrainPredictor
import com.neurobehavior.drift.ml.DriftDetector
import com.neurobehavior.drift.notifications.NotificationHelper
import com.neurobehavior.drift.data.preferences.UserPreferencesRepository
import com.neurobehavior.drift.tracking.UsageStatsCollector
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * BACKGROUND WORKER
 * Runs every 4 hours using WorkManager.
 * Performs the full behavioral analysis pipeline:
 * 1. Collect stats → 2. Save to DB → 3. Detect drift → 4. Predict strain → 5. Alert if needed
 *
 * @HiltWorker enables Hilt dependency injection inside WorkManager workers.
 * WorkManager survives app restarts and can run even when the app is closed.
 */
@HiltWorker
class BehaviorAnalysisWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val usageStatsCollector: UsageStatsCollector,
    private val behaviorRepository: BehaviorRepository,
    private val strainPredictor: CognitiveStrainPredictor,
    private val driftDetector: DriftDetector,
    private val notificationHelper: NotificationHelper,
    private val preferencesRepository: UserPreferencesRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            if (!usageStatsCollector.hasUsagePermission()) return Result.success()

            val prefs = preferencesRepository.userPreferencesFlow.first()
            if (!prefs.trackingEnabled) return Result.success()

            // 1. Collect today's behavioral signals
            val todayStats = usageStatsCollector.collectTodayStats()

            // 2. Save to Room database
            behaviorRepository.saveBehaviorLog(todayStats)

            // 3. Get baseline for drift computation
            val baseline = behaviorRepository.getBaselineProfile()

            // 4. Compute drift score
            val driftScore = if (baseline != null) {
                driftDetector.computeDriftScore(todayStats, baseline)
            } else 0f

            // 5. Run TFLite inference
            val strainScore = strainPredictor.predict(todayStats, driftScore)

            // 6. Classify strain level
            val strainLevel = when {
                strainScore < 0.3f -> "LOW"
                strainScore < 0.6f -> "MODERATE"
                strainScore < 0.8f -> "HIGH"
                else -> "CRITICAL"
            }

            // 7. Compute anomaly score
            val anomalyScore = if (baseline != null) {
                val log = behaviorRepository.getTodayLog()
                if (log != null) driftDetector.computeAnomalyScore(log, baseline) else 0f
            } else 0f

            // 8. Update the log with ML results
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            behaviorRepository.updateLogWithMLResults(today, strainScore, driftScore, anomalyScore, strainLevel)

            // 9. Send notifications if strain is high
            if (prefs.strainAlertsEnabled && strainScore >= prefs.strainThreshold) {
                when {
                    strainScore >= 0.85f -> notificationHelper.sendCriticalStrainAlert(strainScore)
                    strainScore >= prefs.strainThreshold -> notificationHelper.sendHighStrainAlert(strainScore)
                }
            }

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(false)
                .build()

            val periodicWork = PeriodicWorkRequestBuilder<BehaviorAnalysisWorker>(
                repeatInterval = 4,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
            .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "behavior_analysis",
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicWork
            )
        }
    }
}
