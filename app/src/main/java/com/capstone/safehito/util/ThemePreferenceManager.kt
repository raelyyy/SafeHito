// ThemePreferenceManager.kt
package com.capstone.safehito.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("theme_settings")
private val THEME_MODE_KEY = intPreferencesKey("theme_mode")

enum class ThemeMode(val value: Int) {
    SYSTEM(0), LIGHT(1), DARK(2);

    companion object {
        fun from(value: Int) = values().firstOrNull { it.value == value } ?: SYSTEM
    }
}

class ThemePreferenceManager(private val context: Context) {
    val themeMode: Flow<ThemeMode> = context.dataStore.data.map {
        ThemeMode.from(it[THEME_MODE_KEY] ?: 0)
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[THEME_MODE_KEY] = mode.value
        }
    }
}
