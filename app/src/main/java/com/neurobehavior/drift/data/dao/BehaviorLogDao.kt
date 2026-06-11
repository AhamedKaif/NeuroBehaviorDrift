package com.neurobehavior.drift.data.dao

import androidx.room.*
import com.neurobehavior.drift.data.model.BehaviorLog
import kotlinx.coroutines.flow.Flow

/**
 * DAO = Data Access Object.
 * Defines all SQL queries for the behavior_logs table.
 * Room generates the actual SQL implementation at compile-time.
 * All functions are suspend (coroutine) for non-blocking database access.
 */
@Dao
interface BehaviorLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: BehaviorLog): Long

    @Update
    suspend fun update(log: BehaviorLog)

    @Query("SELECT * FROM behavior_logs ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentLogs(limit: Int): List<BehaviorLog>

    @Query("SELECT * FROM behavior_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogsFlow(limit: Int): Flow<List<BehaviorLog>>

    @Query("SELECT * FROM behavior_logs WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getLogByDate(dateKey: String): BehaviorLog?

    @Query("SELECT COUNT(*) FROM behavior_logs")
    suspend fun getTotalDays(): Int

    @Query("SELECT * FROM behavior_logs ORDER BY timestamp DESC LIMIT 7")
    suspend fun getLast7Days(): List<BehaviorLog>

    @Query("SELECT * FROM behavior_logs ORDER BY timestamp DESC LIMIT 30")
    suspend fun getLast30Days(): List<BehaviorLog>

    @Query("SELECT AVG(totalScreenTimeMs) FROM behavior_logs")
    suspend fun getAvgScreenTime(): Double?

    @Query("SELECT AVG(appSwitchCount) FROM behavior_logs")
    suspend fun getAvgAppSwitches(): Double?

    @Query("SELECT AVG(unlockCount) FROM behavior_logs")
    suspend fun getAvgUnlockCount(): Double?

    @Query("SELECT AVG(nightUsageMinutes) FROM behavior_logs")
    suspend fun getAvgNightUsage(): Double?

    @Query("DELETE FROM behavior_logs")
    suspend fun deleteAll()

    @Query("DELETE FROM behavior_logs WHERE timestamp < :cutoffMs")
    suspend fun deleteOlderThan(cutoffMs: Long)

    @Query("SELECT * FROM behavior_logs ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestLog(): BehaviorLog?
}
