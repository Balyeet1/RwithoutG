package com.example.rewardwithoutguilt.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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
        private val UNEARNED_PIECES_KEY = stringPreferencesKey("unearned_pieces")
        private val HINT_ENABLED_KEY = booleanPreferencesKey("hint_enabled")
        private val UNSEEN_EARNED_PIECES_KEY = intPreferencesKey("unseen_earned_pieces")
    }

    val placedPieces: Flow<Map<String, String>> = context.puzzleDataStore.data.map { preferences ->
        val raw = preferences[PLACED_PIECES_KEY] ?: ""
        if (raw.isBlank()) {
            emptyMap()
        } else {
            val map = mutableMapOf<String, String>()
            raw.split(",").forEach { pair ->
                val parts = pair.split("=")
                if (parts.size == 2) {
                    map[parts[0]] = parts[1]
                }
            }
            map
        }
    }

    val trayPieces: Flow<List<String>> = context.puzzleDataStore.data.map { preferences ->
        val raw = preferences[TRAY_PIECES_KEY] ?: ""
        if (raw.isBlank()) emptyList() else raw.split(",")
    }

    val unearnedPieces: Flow<List<String>> = context.puzzleDataStore.data.map { preferences ->
        val raw = preferences[UNEARNED_PIECES_KEY]
        if (raw == null) {
            val allPieces = mutableListOf<String>()
            for (row in 1..6) {
                for (col in 1..6) {
                    allPieces.add("${row}_${col}")
                }
            }
            allPieces
        } else if (raw.isBlank()) {
            emptyList()
        } else {
            raw.split(",")
        }
    }

    val isHintEnabled: Flow<Boolean> = context.puzzleDataStore.data.map { preferences ->
        preferences[HINT_ENABLED_KEY] ?: false
    }

    val unseenEarnedPieces: Flow<Int> = context.puzzleDataStore.data.map { preferences ->
        preferences[UNSEEN_EARNED_PIECES_KEY] ?: 0
    }

    suspend fun saveGameState(
        placed: Map<String, String>,
        tray: List<String>
    ) {
        context.puzzleDataStore.edit { preferences ->
            preferences[PLACED_PIECES_KEY] = placed.entries.joinToString(",") { "${it.key}=${it.value}" }
            preferences[TRAY_PIECES_KEY] = tray.joinToString(",")
        }
    }

    suspend fun setHintEnabled(enabled: Boolean) {
        context.puzzleDataStore.edit { preferences ->
            preferences[HINT_ENABLED_KEY] = enabled
        }
    }

    suspend fun clearUnseenEarnedPieces() {
        context.puzzleDataStore.edit { preferences ->
            preferences[UNSEEN_EARNED_PIECES_KEY] = 0
        }
    }

    suspend fun awardPuzzlePiece() {
        context.puzzleDataStore.edit { preferences ->
            val unearnedRaw = preferences[UNEARNED_PIECES_KEY]
            val unearned = if (unearnedRaw == null) {
                val allPieces = mutableListOf<String>()
                for (row in 1..6) {
                    for (col in 1..6) {
                        allPieces.add("${row}_${col}")
                    }
                }
                allPieces
            } else if (unearnedRaw.isBlank()) {
                emptyList()
            } else {
                unearnedRaw.split(",")
            }

            if (unearned.isNotEmpty()) {
                val pieceToAward = unearned.random()
                val newUnearned = unearned - pieceToAward
                preferences[UNEARNED_PIECES_KEY] = newUnearned.joinToString(",")

                val trayRaw = preferences[TRAY_PIECES_KEY] ?: ""
                val tray = if (trayRaw.isBlank()) emptyList() else trayRaw.split(",")
                val newTray = tray + pieceToAward
                preferences[TRAY_PIECES_KEY] = newTray.joinToString(",")

                val unseen = preferences[UNSEEN_EARNED_PIECES_KEY] ?: 0
                preferences[UNSEEN_EARNED_PIECES_KEY] = unseen + 1
            }
        }
    }

    suspend fun resetGame(shuffledTray: List<String>) {
        context.puzzleDataStore.edit { preferences ->
            preferences[PLACED_PIECES_KEY] = ""
            preferences[UNEARNED_PIECES_KEY] = shuffledTray.joinToString(",")
            preferences[TRAY_PIECES_KEY] = ""
            preferences[UNSEEN_EARNED_PIECES_KEY] = 0
        }
    }
}
