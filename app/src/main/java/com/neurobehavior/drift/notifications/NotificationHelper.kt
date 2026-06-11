package com.neurobehavior.drift.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.neurobehavior.drift.R
import com.neurobehavior.drift.ui.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(@ApplicationContext private val context: Context) {

    private val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_STRAIN    = "channel_strain_alerts"
        const val CHANNEL_DAILY     = "channel_daily_summary"
        const val NOTIF_HIGH_STRAIN = 1001
        const val NOTIF_DAILY       = 1002
        const val NOTIF_CRITICAL    = 1003
    }

    init { createChannels() }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val strainChannel = NotificationChannel(
                CHANNEL_STRAIN,
                "Cognitive Strain Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when high cognitive strain is detected"
                enableVibration(true)
                enableLights(true)
            }
            val dailyChannel = NotificationChannel(
                CHANNEL_DAILY,
                "Daily Behavioral Summary",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily summary of your behavioral patterns"
            }
            manager.createNotificationChannels(listOf(strainChannel, dailyChannel))
        }
    }

    private fun openAppIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun sendHighStrainAlert(strainScore: Float) {
        val pct = (strainScore * 100).toInt()
        val notification = NotificationCompat.Builder(context, CHANNEL_STRAIN)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⚠️ High Cognitive Strain Detected")
            .setContentText("Your strain score is $pct%. Time for a break!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Your behavioral patterns indicate $pct% cognitive strain. " +
                        "Consider taking a 15-minute screen-free break to restore mental clarity."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent())
            .addAction(android.R.drawable.ic_menu_view, "View Details", openAppIntent())
            .build()
        manager.notify(NOTIF_HIGH_STRAIN, notification)
    }

    fun sendCriticalStrainAlert(strainScore: Float) {
        val pct = (strainScore * 100).toInt()
        val notification = NotificationCompat.Builder(context, CHANNEL_STRAIN)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🚨 Critical Cognitive Strain!")
            .setContentText("Strain at $pct% — Immediate break recommended")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Your cognitive strain is critically high at $pct%. " +
                        "Stop digital activities for at least 30 minutes. " +
                        "Prolonged high strain impairs decision-making and memory."))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent())
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()
        manager.notify(NOTIF_CRITICAL, notification)
    }

    fun sendDailySummary(strainScore: Float, screenTimeHours: Float, driftScore: Float) {
        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("📊 Today's Behavioral Summary")
            .setContentText("Strain: ${(strainScore*100).toInt()}% | Screen: ${screenTimeHours.toInt()}h | Drift: ${(driftScore*100).toInt()}%")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Cognitive strain: ${(strainScore*100).toInt()}%\n" +
                        "Screen time: ${screenTimeHours.toInt()}h ${((screenTimeHours % 1) * 60).toInt()}m\n" +
                        "Behavioral drift: ${(driftScore*100).toInt()}%\n" +
                        "Tap to see detailed analytics."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent())
            .build()
        manager.notify(NOTIF_DAILY, notification)
    }
}
