package com.neurobehavior.drift.data.repository

import com.neurobehavior.drift.data.dao.BaselineProfileDao
import com.neurobehavior.drift.data.dao.BehaviorLogDao
import com.neurobehavior.drift.data.model.BaselineProfile
import com.neurobehavior.drift.data.model.BehaviorLog
import com.neurobehavior.drift.data.model.TodayStats
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

/**
 * REPOSITORY PATTERN:
 * The Repository is the single source of truth for all data.
 * ViewModels call the repository — they never talk to DAOs directly.
 * This makes it easy to swap the data source (e.g., add a remote API later).
 *
 * The Repository:
 *  1. Translates TodayStats → BehaviorLog (DB entity)
 *  2. Computes and updates the baseline profile
 *  3. Provides clean data interfaces to ViewModels
 */
@Singleton
class BehaviorRepository @Inject constructor(
    private val behaviorLogDao: BehaviorLogDao,
    private val baselineProfileDao: BaselineProfileDao
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // ── Write Operations ─────────────────────────────────────────────────

    suspend fun saveBehaviorLog(stats: TodayStats): BehaviorLog {
        val today = dateFormat.format(Date())
        val existing = behaviorLogDao.getLogByDate(today)

        val log = BehaviorLog(
            id = existing?.id ?: 0,
            timestamp = System.currentTimeMillis(),
            dateKey = today,
            totalScreenTimeMs = stats.totalScreenTimeMs,
            appSwitchCount = stats.appSwitchCount,
            unlockCount = stats.unlockCount,
            nightUsageMinutes = stats.nightUsageMinutes,
            avgSessionDurationMs = stats.avgSessionDurationMs,
            uniqueAppsUsed = stats.uniqueAppsUsed
        )
        behaviorLogDao.insert(log)

        // Recompute baseline after each new day's data
        updateBaselineProfile()

        return log
    }

    suspend fun updateLogWithMLResults(
        dateKey: String,
        strainScore: Float,
        driftScore: Float,
        anomalyScore: Float,
        strainLevel: String
    ) {
        val log = behaviorLogDao.getLogByDate(dateKey) ?: return
        behaviorLogDao.update(log.copy(
            strainScore = strainScore,
            driftScore = driftScore,
            anomalyScore = anomalyScore,
            strainLevel = strainLevel
        ))
    }

    // ── Read Operations ──────────────────────────────────────────────────

    suspend fun getRecentLogs(days: Int): List<BehaviorLog> =
        behaviorLogDao.getRecentLogs(days)

    fun getRecentLogsFlow(days: Int): Flow<List<BehaviorLog>> =
        behaviorLogDao.getRecentLogsFlow(days)

    suspend fun getTodayLog(): BehaviorLog? {
        val today = dateFormat.format(Date())
        return behaviorLogDao.getLogByDate(today)
    }

    suspend fun getBaselineProfile(): BaselineProfile? =
        baselineProfileDao.getBaseline()

    suspend fun getDaysOfData(): Int = behaviorLogDao.getTotalDays()

    // ── Baseline Computation ─────────────────────────────────────────────

    /**
     * Recomputes the baseline using the last 7–30 days of data.
     * Baseline = average + standard deviation for each feature.
     * Needs at least 3 days of data to establish a meaningful baseline.
     */
    private suspend fun updateBaselineProfile() {
        val logs = behaviorLogDao.getLast30Days()
        if (logs.size < 3) return   // Need minimum 3 days

        val n = logs.size.toDouble()

        // Compute means
        val avgScreen = logs.map { it.totalScreenTimeMs }.average().toLong()
        val avgSwitches = logs.map { it.appSwitchCount }.average().toInt()
        val avgUnlocks = logs.map { it.unlockCount }.average().toInt()
        val avgNight = logs.map { it.nightUsageMinutes }.average().toInt()
        val avgSession = logs.map { it.avgSessionDurationMs }.average().toLong()
        val avgApps = logs.map { it.uniqueAppsUsed }.average().toInt()

        // Compute standard deviations (for Z-score normalization)
        val stdScreen = logs.map { it.totalScreenTimeMs }.std().toFloat().coerceAtLeast(1f)
        val stdSwitches = logs.map { it.appSwitchCount.toLong() }.std().toFloat().coerceAtLeast(1f)
        val stdUnlocks = logs.map { it.unlockCount.toLong() }.std().toFloat().coerceAtLeast(1f)
        val stdNight = logs.map { it.nightUsageMinutes.toLong() }.std().toFloat().coerceAtLeast(1f)

        val baseline = BaselineProfile(
            id = 1,
            lastUpdated = System.currentTimeMillis(),
            daysOfData = logs.size,
            avgScreenTimeMs = avgScreen,
            avgAppSwitches = avgSwitches,
            avgUnlockCount = avgUnlocks,
            avgNightUsageMinutes = avgNight,
            avgSessionDurationMs = avgSession,
            avgUniqueApps = avgApps,
            stdScreenTimeMs = stdScreen,
            stdAppSwitches = stdSwitches,
            stdUnlockCount = stdUnlocks,
            stdNightUsageMinutes = stdNight
        )
        baselineProfileDao.insert(baseline)
    }

    suspend fun clearAllData() {
        behaviorLogDao.deleteAll()
        baselineProfileDao.deleteAll()
    }

    // Helper: standard deviation for a list of Longs
    private fun List<Long>.std(): Double {
        val mean = average()
        return sqrt(map { (it - mean) * (it - mean) }.average())
    }
}
