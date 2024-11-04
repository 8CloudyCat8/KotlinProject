package com.example.kotlinproject.models

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.example.kotlinproject.dataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferences(private val context: Context) {
    companion object {
        val LANGUAGE_KEY = stringPreferencesKey("language")
        val FAVORITE_MOVIES_KEY = stringPreferencesKey("favorite_movies")
        val SLIDER_VALUE_KEY = floatPreferencesKey("slider_value")
        val IS_MODIFIED_KEY = booleanPreferencesKey("is_modified")
    }

    private val defaultLanguage = "ru-RU"
    private val defaultSliderValue = 1.0f

    val isModifiedFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_MODIFIED_KEY] ?: false
        }

    suspend fun saveLanguage(language: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[LANGUAGE_KEY] = language
            }
        } catch (e: Exception) {
            Log.e("UserPreferences", "Error saving language: ${e.message}")
        }
    }

    val languageFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[LANGUAGE_KEY] ?: defaultLanguage
        }

    suspend fun saveFavoriteMovies(favorites: List<Movie>) {
        try {
            val json = Gson().toJson(favorites)
            context.dataStore.edit { preferences ->
                preferences[FAVORITE_MOVIES_KEY] = json
            }
        } catch (e: Exception) {
            Log.e("UserPreferences", "Error saving favorite movies: ${e.message}")
        }
    }

    suspend fun saveSliderValue(value: Float) {
        try {
            context.dataStore.edit { preferences ->
                preferences[SLIDER_VALUE_KEY] = value
                val newState = value != defaultSliderValue
                preferences[IS_MODIFIED_KEY] = newState
            }
        } catch (e: Exception) {
            Log.e("UserPreferences", "Error saving slider value: ${e.message}")
        }
    }

    val favoriteMoviesFlow: Flow<List<Movie>> = context.dataStore.data
        .map { preferences ->
            try {
                val json = preferences[FAVORITE_MOVIES_KEY] ?: return@map emptyList()
                val type = object : TypeToken<List<Movie>>() {}.type
                Gson().fromJson<List<Movie>>(json, type) ?: emptyList()
            } catch (e: Exception) {
                Log.e("UserPreferences", "Error loading favorite movies: ${e.message}")
                emptyList()
            }
        }

    val sliderValueFlow: Flow<Float> = context.dataStore.data
        .map { preferences ->
            preferences[SLIDER_VALUE_KEY] ?: defaultSliderValue
        }
}
