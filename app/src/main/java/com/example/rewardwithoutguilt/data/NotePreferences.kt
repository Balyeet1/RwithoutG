package com.example.rewardwithoutguilt.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.rewardwithoutguilt.util.Constants
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.noteDataStore by preferencesDataStore(name = Constants.PREFS_NOTE)

@Immutable
data class Note(
    val id: String,
    val title: String,
    val content: String,
    val category: String,
    val createdAt: Long = System.currentTimeMillis()
)

class NotePreferences(private val context: Context) {
    companion object {
        private val NOTES_KEY = stringPreferencesKey("notes")
        private val SELECTED_CATEGORY_KEY = stringPreferencesKey("selected_category")
        
        private const val DELIMITER_ITEM = ";"
        private const val DELIMITER_FIELD = "|"
        private const val ESCAPE_NEWLINE = "\\n"
        private const val ESCAPE_FIELD = "\\p"
        private const val ESCAPE_ITEM = "\\s"
    }

    val selectedCategory: Flow<String?> = context.noteDataStore.data.map { it[SELECTED_CATEGORY_KEY] }

    suspend fun updateSelectedCategory(category: String?) {
        context.noteDataStore.edit { preferences ->
            if (category == null) {
                preferences.remove(SELECTED_CATEGORY_KEY)
            } else {
                preferences[SELECTED_CATEGORY_KEY] = category
            }
        }
    }

    val notes: Flow<List<Note>> = context.noteDataStore.data.map { preferences ->
        val notesString = preferences[NOTES_KEY] ?: ""
        if (notesString.isBlank()) return@map emptyList()
        
        notesString.split(DELIMITER_ITEM).mapNotNull { item ->
            if (item.isBlank()) return@mapNotNull null
            val parts = item.split(DELIMITER_FIELD)
            if (parts.size < 3) return@mapNotNull null

            val id = parts[0].trim()
            if (parts.size == 3) { // Legacy format
                val content = parts[1].trim().unescape()
                val createdAt = parts[2].trim().toLongOrNull() ?: System.currentTimeMillis()
                Note(id, "", content, "PERSONAL", createdAt)
            } else if (parts.size >= 5) {
                val title = parts[1].trim().unescape()
                val content = parts[2].trim().unescape()
                val category = parts[3].trim()
                val createdAt = parts[4].trim().toLongOrNull() ?: System.currentTimeMillis()
                Note(id, title, content, category, createdAt)
            } else null
        }
    }

    suspend fun addNote(note: Note) {
        context.noteDataStore.edit { preferences ->
            val currentNotesString = preferences[NOTES_KEY] ?: ""
            val escapedTitle = note.title.escape()
            val escapedContent = note.content.escape()
            val newNoteString = "${note.id}$DELIMITER_FIELD$escapedTitle$DELIMITER_FIELD$escapedContent$DELIMITER_FIELD${note.category}$DELIMITER_FIELD${note.createdAt}"
            
            if (currentNotesString.isBlank()) {
                preferences[NOTES_KEY] = newNoteString
            } else {
                val notesList = currentNotesString.split(DELIMITER_ITEM).toMutableList()
                val existingIndex = notesList.indexOfFirst { it.startsWith("${note.id}$DELIMITER_FIELD") }
                if (existingIndex != -1) {
                    notesList[existingIndex] = newNoteString
                } else {
                    notesList.add(newNoteString)
                }
                preferences[NOTES_KEY] = notesList.filter { it.isNotBlank() }.joinToString(DELIMITER_ITEM)
            }
        }
    }

    suspend fun deleteNote(noteId: String) {
        context.noteDataStore.edit { preferences ->
            val currentNotesString = preferences[NOTES_KEY] ?: ""
            val updatedNotes = currentNotesString.split(DELIMITER_ITEM).filter { item ->
                item.isNotBlank() && !item.startsWith("$noteId$DELIMITER_FIELD")
            }
            preferences[NOTES_KEY] = updatedNotes.joinToString(DELIMITER_ITEM)
        }
    }

    private fun String.escape() = this
        .replace("\n", ESCAPE_NEWLINE)
        .replace(DELIMITER_FIELD, ESCAPE_FIELD)
        .replace(DELIMITER_ITEM, ESCAPE_ITEM)

    private fun String.unescape() = this
        .replace(ESCAPE_NEWLINE, "\n")
        .replace(ESCAPE_FIELD, DELIMITER_FIELD)
        .replace(ESCAPE_ITEM, DELIMITER_ITEM)
}
