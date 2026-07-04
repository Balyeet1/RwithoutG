package com.example.rewardwithoutguilt.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "focus_preferences")

class FocusPreferences(private val context: Context) {
    companion object {
        private val FOCUS_ENABLED = booleanPreferencesKey("focus_enabled")
        private val BLOCKED_APPS = stringSetPreferencesKey("blocked_apps")
        private val TIMER_END_TIME = longPreferencesKey("timer_end_time")
        private val TIMER_DURATION = longPreferencesKey("timer_duration") // in milliseconds
    }

    val isFocusEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[FOCUS_ENABLED] ?: false
        }

    val blockedApps: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[BLOCKED_APPS] ?: emptySet()
        }

    val timerEndTime: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[TIMER_END_TIME] ?: 0L
        }

    val timerDuration: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[TIMER_DURATION] ?: (25 * 60 * 1000L) // Default 25 mins
        }

    suspend fun setFocusEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[FOCUS_ENABLED] = enabled
            if (!enabled) {
                preferences[TIMER_END_TIME] = 0L
            }
        }
    }

    suspend fun setTimer(durationMs: Long, startTimeMs: Long?) {
        context.dataStore.edit { preferences ->
            preferences[TIMER_DURATION] = durationMs
            if (startTimeMs != null) {
                preferences[TIMER_END_TIME] = startTimeMs + durationMs
            } else {
                preferences[TIMER_END_TIME] = 0L
            }
        }
    }

    suspend fun clearTimer() {
        context.dataStore.edit { preferences ->
            preferences[TIMER_END_TIME] = 0L
        }
    }

    suspend fun updateBlockedApps(apps: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[BLOCKED_APPS] = apps
        }
    }

    suspend fun addBlockedApp(packageName: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[BLOCKED_APPS] ?: emptySet()
            preferences[BLOCKED_APPS] = current + packageName
        }
    }

    suspend fun removeBlockedApp(packageName: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[BLOCKED_APPS] ?: emptySet()
            preferences[BLOCKED_APPS] = current - packageName
        }
    }
}
