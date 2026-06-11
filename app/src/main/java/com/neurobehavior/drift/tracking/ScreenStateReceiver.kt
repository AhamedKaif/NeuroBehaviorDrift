package com.neurobehavior.drift.tracking

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

/**
 * BroadcastReceiver that listens for SCREEN_ON / SCREEN_OFF / USER_PRESENT events.
 * USER_PRESENT fires when the user successfully unlocks the device.
 * We count unlocks as a behavioral signal — high unlock frequency correlates with
 * compulsive checking behavior and cognitive fragmentation.
 */
class ScreenStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_USER_PRESENT -> {
                // Device unlocked — increment today's unlock counter
                incrementUnlockCount(context)
            }
            Intent.ACTION_SCREEN_OFF -> {
                // Screen turned off — could save session end time if needed
            }
            Intent.ACTION_SCREEN_ON -> {
                // Screen turned on — could track screen-on frequency
            }
        }
    }

    companion object {
        private const val PREFS_NAME = "screen_state_prefs"
        private const val KEY_UNLOCK_COUNT = "unlock_count"
        private const val KEY_UNLOCK_DATE = "unlock_date"

        private fun getPrefs(context: Context): SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        private fun incrementUnlockCount(context: Context) {
            val prefs = getPrefs(context)
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date())
            val savedDate = prefs.getString(KEY_UNLOCK_DATE, "")

            if (savedDate != today) {
                // New day — reset counter
                prefs.edit().putString(KEY_UNLOCK_DATE, today).putInt(KEY_UNLOCK_COUNT, 1).apply()
            } else {
                val current = prefs.getInt(KEY_UNLOCK_COUNT, 0)
                prefs.edit().putInt(KEY_UNLOCK_COUNT, current + 1).apply()
            }
        }

        fun getUnlockCount(context: Context): Int {
            val prefs = getPrefs(context)
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date())
            val savedDate = prefs.getString(KEY_UNLOCK_DATE, "")
            return if (savedDate == today) prefs.getInt(KEY_UNLOCK_COUNT, 0) else 0
        }
    }
}
