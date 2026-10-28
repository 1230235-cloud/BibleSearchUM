package com.example.blesearchum.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.blesearchum.model.FavoriteVerse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "bible_favorites")

class FavoritesRepository(private val context: Context) {
    private val gson = Gson()
    private val favoritesKey = stringPreferencesKey("favorites_json")

    val favoritesFlow: Flow<List<FavoriteVerse>> = context.dataStore.data.map { preferences ->
        val json = preferences[favoritesKey] ?: "[]"
        val type = object : TypeToken<List<FavoriteVerse>>() {}.type
        runCatching { gson.fromJson<List<FavoriteVerse>>(json, type) }.getOrDefault(emptyList())
    }

    suspend fun toggleFavorite(verse: FavoriteVerse) {
        context.dataStore.edit { preferences ->
            val json = preferences[favoritesKey] ?: "[]"
            val type = object : TypeToken<MutableList<FavoriteVerse>>() {}.type
            val currentList: MutableList<FavoriteVerse> =
                runCatching { gson.fromJson<MutableList<FavoriteVerse>>(json, type) }
                    .getOrDefault(mutableListOf())

            val existingIndex = currentList.indexOfFirst { it.id == verse.id }
            if (existingIndex >= 0) {
                currentList.removeAt(existingIndex)
            } else {
                currentList.add(0, verse)
            }
            preferences[favoritesKey] = gson.toJson(currentList)
        }
    }

    suspend fun removeFavorite(id: String) {
        context.dataStore.edit { preferences ->
            val json = preferences[favoritesKey] ?: "[]"
            val type = object : TypeToken<MutableList<FavoriteVerse>>() {}.type
            val currentList: MutableList<FavoriteVerse> =
                runCatching { gson.fromJson<MutableList<FavoriteVerse>>(json, type) }
                    .getOrDefault(mutableListOf())

            currentList.removeAll { it.id == id }
            preferences[favoritesKey] = gson.toJson(currentList)
        }
    }
}
