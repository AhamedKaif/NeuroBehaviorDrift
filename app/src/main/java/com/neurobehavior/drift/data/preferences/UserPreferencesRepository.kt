package com.neurobehavior.drift.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "neuro_prefs")

data class UserPreferences(
    val strainAlertsEnabled: Boolean = true,
    val strainThreshold: Float = 0.70f,
    val nightAlertsEnabled: Boolean = true,
    val trackingEnabled: Boolean = true,
    val retentionDays: Int = 30,
    val onboardingComplete: Boolean = false,
    val jwtToken: String = "",
    val username: String = "",
    val fullName: String = "",
    val email: String = ""
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val STRAIN_ALERTS   = booleanPreferencesKey("strain_alerts_enabled")
        val STRAIN_THRESHOLD = floatPreferencesKey("strain_threshold")
        val NIGHT_ALERTS    = booleanPreferencesKey("night_alerts_enabled")
        val TRACKING        = booleanPreferencesKey("tracking_enabled")
        val RETENTION_DAYS  = intPreferencesKey("retention_days")
        val ONBOARDING      = booleanPreferencesKey("onboarding_complete")
        val JWT_TOKEN       = stringPreferencesKey("jwt_token")
        val USERNAME        = stringPreferencesKey("username")
        val FULL_NAME       = stringPreferencesKey("full_name")
        val EMAIL           = stringPreferencesKey("email")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            UserPreferences(
                strainAlertsEnabled = prefs[Keys.STRAIN_ALERTS] ?: true,
                strainThreshold     = prefs[Keys.STRAIN_THRESHOLD] ?: 0.70f,
                nightAlertsEnabled  = prefs[Keys.NIGHT_ALERTS] ?: true,
                trackingEnabled     = prefs[Keys.TRACKING] ?: true,
                retentionDays       = prefs[Keys.RETENTION_DAYS] ?: 30,
                onboardingComplete  = prefs[Keys.ONBOARDING] ?: false,
                jwtToken            = prefs[Keys.JWT_TOKEN] ?: "",
                username            = prefs[Keys.USERNAME] ?: "",
                fullName            = prefs[Keys.FULL_NAME] ?: "",
                email               = prefs[Keys.EMAIL] ?: ""
            )
        }

    suspend fun setStrainAlertsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.STRAIN_ALERTS] = enabled }
    }
    suspend fun setStrainThreshold(threshold: Float) {
        context.dataStore.edit { it[Keys.STRAIN_THRESHOLD] = threshold }
    }
    suspend fun setNightAlertsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NIGHT_ALERTS] = enabled }
    }
    suspend fun setTrackingEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.TRACKING] = enabled }
    }
    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING] = complete }
    }
    suspend fun saveAuth(token: String, username: String, fullName: String, email: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.JWT_TOKEN] = token
            prefs[Keys.USERNAME] = username
            prefs[Keys.FULL_NAME] = fullName
            prefs[Keys.EMAIL] = email
        }
    }
    suspend fun updateProfileInfo(fullName: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FULL_NAME] = fullName
        }
    }
    suspend fun clearAuth() {
        context.dataStore.edit { prefs ->
            prefs[Keys.JWT_TOKEN] = ""
            prefs[Keys.USERNAME] = ""
            prefs[Keys.FULL_NAME] = ""
            prefs[Keys.EMAIL] = ""
        }
    }
}
