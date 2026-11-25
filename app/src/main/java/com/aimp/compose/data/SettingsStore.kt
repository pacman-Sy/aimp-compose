package com.aimp.compose.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("aimp_settings")

class SettingsStore(private val context: Context) {
    private val autoLyricsKey = booleanPreferencesKey("auto_lyrics")
    private val darkThemeKey = booleanPreferencesKey("dark_theme")
    private val lyricsModeKey = intPreferencesKey("lyrics_mode") // 0 synced, 1 plain

    val autoLyrics: Flow<Boolean> = context.dataStore.data.map { it[autoLyricsKey] ?: true }
    val darkTheme: Flow<Boolean> = context.dataStore.data.map { it[darkThemeKey] ?: true }
    val lyricsMode: Flow<Int> = context.dataStore.data.map { it[lyricsModeKey] ?: 0 }

    suspend fun setAutoLyrics(value: Boolean) = context.dataStore.edit { it[autoLyricsKey] = value }
    suspend fun setDarkTheme(value: Boolean) = context.dataStore.edit { it[darkThemeKey] = value }
    suspend fun setLyricsMode(value: Int) = context.dataStore.edit { it[lyricsModeKey] = value }
}
