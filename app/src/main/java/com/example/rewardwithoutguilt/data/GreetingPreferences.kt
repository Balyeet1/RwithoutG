package com.example.rewardwithoutguilt.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.rewardwithoutguilt.util.Constants
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = Constants.PREFS_GREETING)

@Immutable
data class GreetingImage(
    val uri: String? = null,
    val size: Float = Constants.DEFAULT_IMAGE_SIZE
)

class GreetingPreferences(private val context: Context) {
    companion object {
        private val GREETING_TEXT = stringPreferencesKey("greeting_text")
        private val GREETING_IMAGE_URI = stringPreferencesKey("greeting_image_uri")
        private val GREETING_IMAGE_SIZE = floatPreferencesKey("greeting_image_size")
    }

    val greetingText: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[GREETING_TEXT] ?: Constants.DEFAULT_GREETING
    }

    val greetingImage: Flow<GreetingImage> = context.dataStore.data.map { preferences ->
        GreetingImage(
            uri = preferences[GREETING_IMAGE_URI],
            size = preferences[GREETING_IMAGE_SIZE] ?: Constants.DEFAULT_IMAGE_SIZE
        )
    }

    suspend fun saveGreeting(text: String, image: GreetingImage) {
        context.dataStore.edit { preferences ->
            preferences[GREETING_TEXT] = text
            if (image.uri != null) {
                preferences[GREETING_IMAGE_URI] = image.uri
            } else {
                preferences.remove(GREETING_IMAGE_URI)
            }
            preferences[GREETING_IMAGE_SIZE] = image.size
        }
    }
}
