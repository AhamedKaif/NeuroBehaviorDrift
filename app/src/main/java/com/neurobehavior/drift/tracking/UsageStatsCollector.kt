package com.neurobehavior.drift.tracking

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import com.neurobehavior.drift.data.model.AppUsageData
import com.neurobehavior.drift.data.model.TodayStats
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageStatsCollector @Inject constructor(@ApplicationContext private val context: Context) {

    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val packageManager = context.packageManager

    private val EXCLUDED = setOf(
        "android","com.android.launcher","com.android.launcher2",
        "com.android.launcher3","com.android.systemui",
        "com.google.android.apps.nexuslauncher",context.packageName
    )

    fun hasUsagePermission(): Boolean {
        val ops = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ops.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        } else {
            @Suppress("DEPRECATION")
            ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    suspend fun collectTodayStats(): TodayStats {
        val (start, end) = todayRange()
        val events = usageStatsManager.queryEvents(start, end)
        val statsMap = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)
            ?: emptyList()

        var screenMs = 0L; var switches = 0; var sessions = 0
        var lastStart = 0L; var lastPkg = ""
        val sessionDurations = mutableListOf<Long>()
        val uniquePkgs = mutableSetOf<String>()

        val ev = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(ev)
            val pkg = ev.packageName ?: continue
            if (pkg in EXCLUDED) continue
            uniquePkgs.add(pkg)
            when (ev.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    if (pkg != lastPkg && lastPkg.isNotEmpty()) switches++
                    lastPkg = pkg; lastStart = ev.timeStamp
                }
                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    if (lastStart > 0 && pkg == lastPkg) {
                        val dur = ev.timeStamp - lastStart
                        if (dur > 1_000) { screenMs += dur; sessionDurations += dur; sessions++ }
                        lastStart = 0
                    }
                }
            }
        }

        val avgSession = if (sessionDurations.isNotEmpty()) sessionDurations.average().toLong() else 0L
        val nightMins = computeNightUsage(start, end)
        val topApps = computeTopApps(statsMap)

        return TodayStats(
            totalScreenTimeMs  = screenMs,
            appSwitchCount     = switches,
            unlockCount        = ScreenStateReceiver.getUnlockCount(context),
            nightUsageMinutes  = nightMins,
            avgSessionDurationMs = avgSession,
            uniqueAppsUsed     = uniquePkgs.size,
            topApps            = topApps
        )
    }

    private fun computeNightUsage(dayStart: Long, dayEnd: Long): Int {
        val earlyEnd = Calendar.getInstance().apply {
            timeInMillis = dayStart
            set(Calendar.HOUR_OF_DAY, 6); set(Calendar.MINUTE, 0)
        }.timeInMillis
        val lateStart = Calendar.getInstance().apply {
            timeInMillis = dayStart
            set(Calendar.HOUR_OF_DAY, 22); set(Calendar.MINUTE, 0)
        }.timeInMillis
        var ms = 0L
        if (dayStart < earlyEnd)
            ms += (usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, dayStart, earlyEnd)
                ?: emptyList()).filter { it.packageName !in EXCLUDED }.sumOf { it.totalTimeInForeground }
        if (lateStart < dayEnd)
            ms += (usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, lateStart, dayEnd)
                ?: emptyList()).filter { it.packageName !in EXCLUDED }.sumOf { it.totalTimeInForeground }
        return (ms / 60_000).toInt()
    }

    private fun computeTopApps(statsList: List<android.app.usage.UsageStats>): List<AppUsageData> =
        statsList.filter { it.packageName !in EXCLUDED && it.totalTimeInForeground > 60_000 }
            .sortedByDescending { it.totalTimeInForeground }.take(5)
            .map { s ->
                val name = try { packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(s.packageName, 0)).toString()
                } catch (e: PackageManager.NameNotFoundException) {
                    s.packageName.substringAfterLast(".")
                }
                AppUsageData(s.packageName, name, s.totalTimeInForeground / 60_000)
            }

    private fun todayRange(): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return start to System.currentTimeMillis()
    }

    suspend fun collectStatsForDay(daysAgo: Int): TodayStats {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -daysAgo) }
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0);      cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59)
        val end = cal.timeInMillis
        val statsMap = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end) ?: emptyList()
        val total = statsMap.filter { it.packageName !in EXCLUDED }.sumOf { it.totalTimeInForeground }
        return TodayStats(
            totalScreenTimeMs = total,
            appSwitchCount    = 0,
            unlockCount       = 0,
            nightUsageMinutes = computeNightUsage(start, end),
            topApps           = computeTopApps(statsMap)
        )
    }
}
