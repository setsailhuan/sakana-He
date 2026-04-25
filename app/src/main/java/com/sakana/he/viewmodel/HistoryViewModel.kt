package com.sakana.he.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sakana.he.HeApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class SelectedDay(val year: Int, val month: Int, val day: Int)

class HistoryViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as HeApplication).repository

    private val now = Calendar.getInstance()
    private val _displayMonth = MutableStateFlow(
        Pair(now.get(Calendar.YEAR), now.get(Calendar.MONTH))
    )
    val displayMonth: StateFlow<Pair<Int, Int>> = _displayMonth.asStateFlow()

    private val _selectedDay = MutableStateFlow(
        SelectedDay(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))
    )
    val selectedDay: StateFlow<SelectedDay> = _selectedDay.asStateFlow()

    val selectedDayRecords = _selectedDay.flatMapLatest { d ->
        repo.getRecordsForDay(d.year, d.month, d.day)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allRecords = repo.getAllRecords()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val totalDays: StateFlow<Int> = repo.getAllRecords()
        .map { records ->
            records.map { r ->
                val c = Calendar.getInstance().apply { timeInMillis = r.timestamp }
                Triple(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
            }.toSet().size
        }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val totalVolume: StateFlow<Int> = repo.getAllRecords()
        .map { records -> records.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun selectDay(year: Int, month: Int, day: Int) {
        _selectedDay.value = SelectedDay(year, month, day)
    }

    fun previousMonth() {
        val (y, m) = _displayMonth.value
        _displayMonth.value = if (m == 0) Pair(y - 1, 11) else Pair(y, m - 1)
    }

    fun nextMonth() {
        val (y, m) = _displayMonth.value
        _displayMonth.value = if (m == 11) Pair(y + 1, 0) else Pair(y, m + 1)
    }

    fun deleteRecord(id: Long) = viewModelScope.launch { repo.deleteRecord(id) }

    fun updateRecord(id: Long, amount: Int, timestamp: Long) = viewModelScope.launch {
        repo.updateRecord(id, amount, timestamp)
    }

    // Takes the collected allRecords list as parameter so the caller controls reactivity
    fun getDayTotalForMonth(
        records: List<com.sakana.he.data.WaterRecord>,
        dailyGoal: Int
    ): Map<Int, Float> {
        if (dailyGoal == 0) return emptyMap()
        val (year, month) = _displayMonth.value
        val dayMap = mutableMapOf<Int, Int>()
        records.forEach { r ->
            val c = Calendar.getInstance().apply { timeInMillis = r.timestamp }
            if (c.get(Calendar.YEAR) == year && c.get(Calendar.MONTH) == month) {
                val d = c.get(Calendar.DAY_OF_MONTH)
                dayMap[d] = (dayMap[d] ?: 0) + r.amount
            }
        }
        return dayMap.mapValues { (_, total) -> total.toFloat() / dailyGoal }
    }

    fun addRecord(amount: Int, hour: Int, minute: Int) = viewModelScope.launch {
        val d = _selectedDay.value
        val timestamp = Calendar.getInstance().apply {
            set(d.year, d.month, d.day, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        repo.addRecord(amount, timestamp)
    }
}
