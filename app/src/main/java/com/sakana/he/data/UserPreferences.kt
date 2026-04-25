package com.sakana.he.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferences(private val context: Context) {

    companion object {
        val DAILY_GOAL = intPreferencesKey("daily_goal")
        val THEME_COLOR = stringPreferencesKey("theme_color")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
    }

    val dailyGoal: Flow<Int> = context.dataStore.data.map { it[DAILY_GOAL] ?: 2000 }
    val themeColor: Flow<String> = context.dataStore.data.map { it[THEME_COLOR] ?: "#00BCD4" }
    val darkMode: Flow<Boolean> = context.dataStore.data.map { it[DARK_MODE] ?: false }

    suspend fun setDailyGoal(goal: Int) {
        context.dataStore.edit { it[DAILY_GOAL] = goal }
    }

    suspend fun setThemeColor(color: String) {
        context.dataStore.edit { it[THEME_COLOR] = color }
    }

    suspend fun setDarkMode(dark: Boolean) {
        context.dataStore.edit { it[DARK_MODE] = dark }
    }
}
