package com.example.dailymeow

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("favorites")

class FavoritesRepository(private val context: Context) {
    private val FAVORITES_KEY = stringSetPreferencesKey("favorites")

    // Retrieve the list of favorites
    val favorites: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[FAVORITES_KEY] ?: emptySet()
    }

    // Add a favorite image URL
    suspend fun addFavorite(imageUrl: String) {
        context.dataStore.edit { preferences ->
            val currentFavorites = preferences[FAVORITES_KEY] ?: emptySet()
            preferences[FAVORITES_KEY] = currentFavorites + imageUrl
        }
    }

    // Remove a favorite image URL
    suspend fun removeFavorite(imageUrl: String) {
        context.dataStore.edit { preferences ->
            val currentFavorites = preferences[FAVORITES_KEY] ?: emptySet()
            preferences[FAVORITES_KEY] = currentFavorites - imageUrl
        }
    }

    // Toggle the favorite state of an image URL
    suspend fun toggleFavorite(imageUrl: String) {
        context.dataStore.edit { preferences ->
            val currentFavorites = preferences[FAVORITES_KEY] ?: emptySet()
            preferences[FAVORITES_KEY] = if (imageUrl in currentFavorites) {
                currentFavorites - imageUrl
            } else {
                currentFavorites + imageUrl
            }
        }
    }
}
