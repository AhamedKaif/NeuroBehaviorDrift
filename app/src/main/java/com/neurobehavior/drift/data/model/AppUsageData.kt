package com.neurobehavior.drift.data.model

/** Holds per-app usage data (not persisted to DB — transient per session) */
data class AppUsageData(
    val packageName: String,
    val appName: String,
    val usageMinutes: Long
)

/** Transient struct holding today's raw collected stats before DB storage */
data class TodayStats(
    val totalScreenTimeMs: Long = 0L,
    val appSwitchCount: Int = 0,
    val unlockCount: Int = 0,
    val nightUsageMinutes: Int = 0,
    val avgSessionDurationMs: Long = 0L,
    val uniqueAppsUsed: Int = 0,
    val topApps: List<AppUsageData> = emptyList()
)
