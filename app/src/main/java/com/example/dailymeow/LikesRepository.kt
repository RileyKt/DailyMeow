package com.example.dailymeow

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Define the DataStore
private val Context.likesDataStore by preferencesDataStore("likes")

class LikesRepository(private val context: Context) {
    private val LIKES_KEY_PREFIX = "likes_"
    private val LIKED_IMAGES_KEY = stringSetPreferencesKey("liked_images")

    // Get the like count for an image (initialize random count if not set)
    fun getLikes(imageUrl: String): Flow<Int> {
        val key = intPreferencesKey("$LIKES_KEY_PREFIX$imageUrl")
        return context.likesDataStore.data.map { preferences ->
            preferences[key] ?: 0 // Default to 0 if not initialized
        }
    }

    // Initialize the like count if not already set
    suspend fun ensureLikesInitialized(imageUrl: String) {
        val key = intPreferencesKey("$LIKES_KEY_PREFIX$imageUrl")
        context.likesDataStore.edit { preferences ->
            if (preferences[key] == null) {
                preferences[key] = (1..1000).random() // Assign a random value only once
            }
        }
    }

    // Check if the user has liked the image
    fun isLiked(imageUrl: String): Flow<Boolean> {
        return context.likesDataStore.data.map { preferences ->
            val likedImages = preferences[LIKED_IMAGES_KEY] ?: emptySet()
            likedImages.contains(imageUrl)
        }
    }

    // Toggle the like state for an image
    suspend fun toggleLike(imageUrl: String) {
        val key = intPreferencesKey("$LIKES_KEY_PREFIX$imageUrl")
        context.likesDataStore.edit { preferences ->
            val likedImages = preferences[LIKED_IMAGES_KEY] ?: emptySet()
            val isLiked = likedImages.contains(imageUrl)

            // Update like count
            val currentLikes = preferences[key] ?: (1..1000).random()
            preferences[key] = if (isLiked) currentLikes - 1 else currentLikes + 1

            // Update liked state
            preferences[LIKED_IMAGES_KEY] = if (isLiked) {
                likedImages - imageUrl
            } else {
                likedImages + imageUrl
            }
        }
    }
}
