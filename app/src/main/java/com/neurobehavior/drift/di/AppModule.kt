package com.neurobehavior.drift.di

import android.content.Context
import androidx.room.Room
import com.neurobehavior.drift.data.dao.BaselineProfileDao
import com.neurobehavior.drift.data.dao.BehaviorLogDao
import com.neurobehavior.drift.data.database.NeuroBehaviorDatabase
import com.neurobehavior.drift.ml.CognitiveStrainPredictor
import com.neurobehavior.drift.ml.DriftDetector
import com.neurobehavior.drift.tracking.ScreenStateReceiver
import com.neurobehavior.drift.tracking.UsageStatsCollector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * HILT MODULE: Tells Hilt HOW to create each dependency.
 * Every @Provides function is called by Hilt when something needs that type.
 * @Singleton means only one instance is created for the entire app lifetime.
 *
 * This replaces manual dependency creation and makes the code testable.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ── Room Database ────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NeuroBehaviorDatabase =
        Room.databaseBuilder(
            context,
            NeuroBehaviorDatabase::class.java,
            "neuro_behavior.db"
        )
        .fallbackToDestructiveMigration()   // For dev — replace with proper migrations in production
        .build()

    @Provides
    @Singleton
    fun provideBehaviorLogDao(db: NeuroBehaviorDatabase): BehaviorLogDao =
        db.behaviorLogDao()

    @Provides
    @Singleton
    fun provideBaselineProfileDao(db: NeuroBehaviorDatabase): BaselineProfileDao =
        db.baselineProfileDao()

    // ── ML Components ────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideCognitiveStrainPredictor(@ApplicationContext context: Context): CognitiveStrainPredictor =
        CognitiveStrainPredictor(context)

    @Provides
    @Singleton
    fun provideDriftDetector(): DriftDetector = DriftDetector()

    // ── Tracking ─────────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideUsageStatsCollector(@ApplicationContext context: Context): UsageStatsCollector =
        UsageStatsCollector(context)
}
