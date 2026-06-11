package com.neurobehavior.drift.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ROOM ENTITY: The user's personal behavioral baseline.
 * Computed as a 7-day rolling average of their normal behavior.
 * Used for drift detection: how far is today from "normal for this user"?
 */
@Entity(tableName = "baseline_profiles")
data class BaselineProfile(
    @PrimaryKey val id: Int = 1,           // Only one baseline at a time
    val lastUpdated: Long = 0L,
    val daysOfData: Int = 0,

    // Average values over the baseline period
    val avgScreenTimeMs: Long = 0L,
    val avgAppSwitches: Int = 0,
    val avgUnlockCount: Int = 0,
    val avgNightUsageMinutes: Int = 0,
    val avgSessionDurationMs: Long = 0L,
    val avgUniqueApps: Int = 0,

    // Standard deviations (for Z-score normalization in drift detection)
    val stdScreenTimeMs: Float = 1f,
    val stdAppSwitches: Float = 1f,
    val stdUnlockCount: Float = 1f,
    val stdNightUsageMinutes: Float = 1f
)
