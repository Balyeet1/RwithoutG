package com.example.rewardwithoutguilt.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.rewardwithoutguilt.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.puzzleDataStore by preferencesDataStore(name = Constants.PREFS_PUZZLE)

class PuzzlePreferences(private val context: Context) {
    companion object {
        private val PLACED_PIECES_KEY = stringPreferencesKey("placed_pieces")
        private val TRAY_PIECES_KEY = stringPreferencesKey("tray_pieces")
        private val HINT_ENABLED_KEY = booleanPreferencesKey("hint_enabled")
        private val MOVES_COUNT_KEY = intPreferencesKey("moves_count")
        private val TIME_ELAPSED_KEY = longPreferencesKey("time_elapsed")
    }

    val placedPieces: Flow<Set<String>> = context.puzzleDataStore.data.map { preferences ->
        val raw = preferences[PLACED_PIECES_KEY] ?: ""
        if (raw.isBlank()) emptySet() else raw.split(",").toSet()
    }

    val trayPieces: Flow<List<String>> = context.puzzleDataStore.data.map { preferences ->
        val raw = preferences[TRAY_PIECES_KEY] ?: ""
        if (raw.isBlank()) emptyList() else raw.split(",")
    }

    val isHintEnabled: Flow<Boolean> = context.puzzleDataStore.data.map { preferences ->
        preferences[HINT_ENABLED_KEY] ?: false
    }

    val movesCount: Flow<Int> = context.puzzleDataStore.data.map { preferences ->
        preferences[MOVES_COUNT_KEY] ?: 0
    }

    val timeElapsed: Flow<Long> = context.puzzleDataStore.data.map { preferences ->
        preferences[TIME_ELAPSED_KEY] ?: 0L
    }

    suspend fun saveGameState(
        placed: Set<String>,
        tray: List<String>,
        moves: Int,
        time: Long
    ) {
        context.puzzleDataStore.edit { preferences ->
            preferences[PLACED_PIECES_KEY] = placed.joinToString(",")
            preferences[TRAY_PIECES_KEY] = tray.joinToString(",")
            preferences[MOVES_COUNT_KEY] = moves
            preferences[TIME_ELAPSED_KEY] = time
        }
    }

    suspend fun setHintEnabled(enabled: Boolean) {
        context.puzzleDataStore.edit { preferences ->
            preferences[HINT_ENABLED_KEY] = enabled
        }
    }

    suspend fun incrementMoves() {
        context.puzzleDataStore.edit { preferences ->
            val current = preferences[MOVES_COUNT_KEY] ?: 0
            preferences[MOVES_COUNT_KEY] = current + 1
        }
    }

    suspend fun updateTimeElapsed(time: Long) {
        context.puzzleDataStore.edit { preferences ->
            preferences[TIME_ELAPSED_KEY] = time
        }
    }

    suspend fun resetGame(shuffledTray: List<String>) {
        context.puzzleDataStore.edit { preferences ->
            preferences[PLACED_PIECES_KEY] = ""
            preferences[TRAY_PIECES_KEY] = shuffledTray.joinToString(",")
            preferences[MOVES_COUNT_KEY] = 0
            preferences[TIME_ELAPSED_KEY] = 0L
        }
    }
}
