package com.sakana.he.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sakana.he.HeApplication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = (app as HeApplication).userPreferences

    val darkMode = prefs.darkMode.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val themeColor = prefs.themeColor.stateIn(viewModelScope, SharingStarted.Eagerly, "#00BCD4")
    val dailyGoal = prefs.dailyGoal.stateIn(viewModelScope, SharingStarted.Eagerly, 2000)

    fun setDarkMode(dark: Boolean) = viewModelScope.launch { prefs.setDarkMode(dark) }
    fun setThemeColor(color: String) = viewModelScope.launch { prefs.setThemeColor(color) }
    fun setDailyGoal(goal: Int) = viewModelScope.launch { prefs.setDailyGoal(goal) }
}
