package com.neurobehavior.drift.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.neurobehavior.drift.data.dao.BaselineProfileDao
import com.neurobehavior.drift.data.dao.BehaviorLogDao
import com.neurobehavior.drift.data.model.BaselineProfile
import com.neurobehavior.drift.data.model.BehaviorLog

/**
 * THE ROOM DATABASE.
 * This is the central access point for all local data storage.
 * Room is an abstraction layer over SQLite — safer and more maintainable.
 *
 * @Database annotation tells Room:
 *   - entities: which tables exist
 *   - version: schema version (increment when you change the schema)
 *
 * The actual DB file is stored at: /data/data/com.neurobehavior.drift/databases/neuro_behavior.db
 * This file NEVER leaves the device — complete privacy guarantee.
 */
@Database(
    entities = [BehaviorLog::class, BaselineProfile::class],
    version = 1,
    exportSchema = false
)
abstract class NeuroBehaviorDatabase : RoomDatabase() {

    /** Provides access to behavior log queries */
    abstract fun behaviorLogDao(): BehaviorLogDao

    /** Provides access to baseline profile queries */
    abstract fun baselineProfileDao(): BaselineProfileDao
}
