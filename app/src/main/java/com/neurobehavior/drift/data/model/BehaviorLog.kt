package com.neurobehavior.drift.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ROOM ENTITY: One record per day of behavioral data.
 * Stored in the local Room database. Never leaves the device.
 * Each field is a behavioral signal extracted from UsageStatsManager.
 */
@Entity(tableName = "behavior_logs")
data class BehaviorLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Unix timestamp (milliseconds) for this record */
    val timestamp: Long = System.currentTimeMillis(),

    /** Date string "YYYY-MM-DD" for easy querying */
    val dateKey: String = "",

    // ── Raw Behavioral Features ──────────────────────────────────────────

    /** Total screen-on time in milliseconds */
    val totalScreenTimeMs: Long = 0L,

    /** Number of times the user switched between different apps */
    val appSwitchCount: Int = 0,

    /** Number of times the device was unlocked */
    val unlockCount: Int = 0,

    /** Minutes spent on phone between 10 PM and 6 AM */
    val nightUsageMinutes: Int = 0,

    /** Average session duration in milliseconds (screenTime / sessions) */
    val avgSessionDurationMs: Long = 0L,

    /** Number of unique apps used today */
    val uniqueAppsUsed: Int = 0,

    // ── ML Outputs (computed after model inference) ──────────────────────

    /** Cognitive strain score [0.0, 1.0] from Random Forest */
    val strainScore: Float = 0f,

    /** Drift score [0.0, 1.0] — deviation from personal baseline */
    val driftScore: Float = 0f,

    /** Anomaly score from Isolation Forest [0.0, 1.0] */
    val anomalyScore: Float = 0f,

    /** Classified strain level: LOW, MODERATE, HIGH, CRITICAL */
    val strainLevel: String = "LOW"
)
