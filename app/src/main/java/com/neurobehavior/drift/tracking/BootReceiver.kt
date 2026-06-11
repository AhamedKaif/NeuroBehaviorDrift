package com.neurobehavior.drift.tracking

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import com.neurobehavior.drift.workers.BehaviorAnalysisWorker
import java.util.concurrent.TimeUnit

/**
 * Restarts the WorkManager periodic task after device reboot.
 * Without this, scheduled workers are cancelled when the device restarts.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            scheduleBehaviorAnalysis(context)
        }
    }

    private fun scheduleBehaviorAnalysis(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<BehaviorAnalysisWorker>(
            repeatInterval = 4,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
        .setConstraints(constraints)
        .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "behavior_analysis",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
