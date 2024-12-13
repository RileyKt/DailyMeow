package com.example.dailymeow

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.likesDataStore by preferencesDataStore("likes")

class LikesRepository(private val context: Context) {
    private val LIKES_KEY = intPreferencesKey("likes_")

    fun getLikes(imageUrl: String): Flow<Int> {
        val key = intPreferencesKey("$LIKES_KEY$imageUrl")
        return context.likesDataStore.data.map { preferences ->
            preferences[key] ?: 0
        }
    }

    suspend fun incrementLikes(imageUrl: String) {
        val key = intPreferencesKey("$LIKES_KEY$imageUrl")
        context.likesDataStore.edit { preferences ->
            val currentLikes = preferences[key] ?: 0
            preferences[key] = currentLikes + 1
        }
    }
}
