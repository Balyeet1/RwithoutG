package com.example.rewardwithoutguilt.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.rewardwithoutguilt.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.notificationDataStore by preferencesDataStore(name = Constants.PREFS_NOTIFICATION)

class NotificationPreferences(private val context: Context) {
    companion object {
        private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
        private val NOTIFICATION_HOUR_KEY = intPreferencesKey("notification_hour")
        private val NOTIFICATION_MINUTE_KEY = intPreferencesKey("notification_minute")
    }

    val notificationsEnabled: Flow<Boolean> = context.notificationDataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED_KEY] ?: true
    }

    val notificationHour: Flow<Int> = context.notificationDataStore.data.map { preferences ->
        preferences[NOTIFICATION_HOUR_KEY] ?: 21
    }

    val notificationMinute: Flow<Int> = context.notificationDataStore.data.map { preferences ->
        preferences[NOTIFICATION_MINUTE_KEY] ?: 0
    }

    suspend fun updateNotificationSettings(enabled: Boolean, hour: Int, minute: Int) {
        context.notificationDataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] = enabled
            preferences[NOTIFICATION_HOUR_KEY] = hour
            preferences[NOTIFICATION_MINUTE_KEY] = minute
        }
    }
}
