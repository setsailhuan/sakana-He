package com.sakana.he.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sakana.he.HeApplication
import com.sakana.he.data.WaterRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val heApp = app as HeApplication
    private val prefs = heApp.userPreferences
    private val repo = heApp.repository

    private val _exportStatus = MutableStateFlow<String?>(null)
    val exportStatus: StateFlow<String?> = _exportStatus.asStateFlow()

    fun exportToCsv(context: Context, uri: Uri) = viewModelScope.launch {
        try {
            val records = repo.exportAllRecords()
            withContext(Dispatchers.IO) {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    OutputStreamWriter(stream).use { writer ->
                        writer.write("id,amount_ml,timestamp_ms\n")
                        records.forEach { r ->
                            writer.write("${r.id},${r.amount},${r.timestamp}\n")
                        }
                    }
                }
            }
            _exportStatus.value = "导出成功：${records.size} 条记录"
        } catch (e: Exception) {
            _exportStatus.value = "导出失败：${e.message}"
        }
    }

    fun importFromCsv(context: Context, uri: Uri) = viewModelScope.launch {
        try {
            val records = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BufferedReader(InputStreamReader(stream)).readLines()
                        .drop(1)
                        .mapNotNull { line ->
                            val parts = line.split(",")
                            if (parts.size >= 3) {
                                val amount = parts[1].trim().toIntOrNull() ?: return@mapNotNull null
                                val timestamp = parts[2].trim().toLongOrNull() ?: return@mapNotNull null
                                WaterRecord(0, amount, timestamp)
                            } else null
                        }
                } ?: emptyList()
            }
            repo.importRecords(records)
            _exportStatus.value = "导入成功：${records.size} 条记录"
        } catch (e: Exception) {
            _exportStatus.value = "导入失败：${e.message}"
        }
    }

    fun clearStatus() { _exportStatus.value = null }
}
