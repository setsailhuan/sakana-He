package com.sakana.he.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.Calendar

class WaterRepository(private val db: WaterDatabase) {

    private val _trigger = MutableSharedFlow<Unit>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    init {
        _trigger.tryEmit(Unit)
    }

    fun getRecordsForDay(year: Int, month: Int, day: Int): Flow<List<WaterRecord>> =
        _trigger.flatMapLatest {
            flow {
                val (start, end) = dayRange(year, month, day)
                emit(db.getRecordsForRange(start, end))
            }
        }.flowOn(Dispatchers.IO)

    fun getTodayRecords(): Flow<List<WaterRecord>> = _trigger.flatMapLatest {
        flow {
            val cal = Calendar.getInstance()
            val (start, end) = dayRange(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            emit(db.getRecordsForRange(start, end))
        }
    }.flowOn(Dispatchers.IO)

    fun getAllRecords(): Flow<List<WaterRecord>> = _trigger.flatMapLatest {
        flow { emit(db.getAllRecords()) }
    }.flowOn(Dispatchers.IO)

    suspend fun addRecord(amount: Int, timestamp: Long = System.currentTimeMillis()) {
        withContext(Dispatchers.IO) { db.insert(amount, timestamp) }
        _trigger.emit(Unit)
    }

    suspend fun updateRecord(id: Long, amount: Int, timestamp: Long) {
        withContext(Dispatchers.IO) { db.update(id, amount, timestamp) }
        _trigger.emit(Unit)
    }

    suspend fun deleteRecord(id: Long) {
        withContext(Dispatchers.IO) { db.delete(id) }
        _trigger.emit(Unit)
    }

    suspend fun importRecords(records: List<WaterRecord>) {
        withContext(Dispatchers.IO) { db.insertAll(records) }
        _trigger.emit(Unit)
    }

    suspend fun exportAllRecords(): List<WaterRecord> =
        withContext(Dispatchers.IO) { db.getAllRecords() }

    private fun dayRange(year: Int, month: Int, day: Int): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(year, month, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val end = Calendar.getInstance().apply {
            set(year, month, day, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
        return start to end
    }
}
