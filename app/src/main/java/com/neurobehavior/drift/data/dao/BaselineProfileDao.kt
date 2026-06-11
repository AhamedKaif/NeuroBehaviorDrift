package com.neurobehavior.drift.data.dao

import androidx.room.*
import com.neurobehavior.drift.data.model.BaselineProfile

@Dao
interface BaselineProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: BaselineProfile)

    @Query("SELECT * FROM baseline_profiles WHERE id = 1 LIMIT 1")
    suspend fun getBaseline(): BaselineProfile?

    @Query("DELETE FROM baseline_profiles")
    suspend fun deleteAll()
}
