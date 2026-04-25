package com.sakana.he.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sakana.he.HeApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val heApp = app as HeApplication
    private val repo = heApp.repository
    private val prefs = heApp.userPreferences

    private val _selectedAmount = MutableStateFlow(150)
    val selectedAmount: StateFlow<Int> = _selectedAmount.asStateFlow()

    val todayRecords = repo.getTodayRecords()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val dailyGoal = prefs.dailyGoal
        .stateIn(viewModelScope, SharingStarted.Eagerly, 2000)

    val todayTotal: StateFlow<Int> = repo.getTodayRecords()
        .map { records -> records.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val dailyProgress: StateFlow<Float> = combine(
        repo.getTodayRecords().map { it.sumOf { r -> r.amount } },
        prefs.dailyGoal
    ) { total, goal ->
        if (goal == 0) 0f else total.toFloat() / goal
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0f)

    fun adjustAmount(delta: Int) {
        _selectedAmount.value = (_selectedAmount.value + delta).coerceIn(10, 300)
    }

    fun recordDrink() = viewModelScope.launch {
        repo.addRecord(_selectedAmount.value)
    }
}
