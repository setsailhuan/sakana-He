package com.sakana.he.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class WaterDatabase(context: Context) :
    SQLiteOpenHelper(context, "water.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE records (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                amount INTEGER NOT NULL,
                timestamp INTEGER NOT NULL
            )"""
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS records")
        onCreate(db)
    }

    fun insert(amount: Int, timestamp: Long): Long {
        val cv = ContentValues().apply {
            put("amount", amount)
            put("timestamp", timestamp)
        }
        return writableDatabase.insert("records", null, cv)
    }

    fun update(id: Long, amount: Int, timestamp: Long) {
        val cv = ContentValues().apply {
            put("amount", amount)
            put("timestamp", timestamp)
        }
        writableDatabase.update("records", cv, "id=?", arrayOf(id.toString()))
    }

    fun delete(id: Long) {
        writableDatabase.delete("records", "id=?", arrayOf(id.toString()))
    }

    fun getRecordsForRange(startMs: Long, endMs: Long): List<WaterRecord> {
        val cursor = readableDatabase.query(
            "records", null,
            "timestamp >= ? AND timestamp < ?",
            arrayOf(startMs.toString(), endMs.toString()),
            null, null, "timestamp ASC"
        )
        return buildList {
            cursor.use {
                while (it.moveToNext()) {
                    add(WaterRecord(it.getLong(0), it.getInt(1), it.getLong(2)))
                }
            }
        }
    }

    fun getAllRecords(): List<WaterRecord> {
        val cursor = readableDatabase.query(
            "records", null, null, null, null, null, "timestamp ASC"
        )
        return buildList {
            cursor.use {
                while (it.moveToNext()) {
                    add(WaterRecord(it.getLong(0), it.getInt(1), it.getLong(2)))
                }
            }
        }
    }

    fun insertAll(records: List<WaterRecord>) {
        writableDatabase.beginTransaction()
        try {
            records.forEach { r ->
                val cv = ContentValues().apply {
                    put("amount", r.amount)
                    put("timestamp", r.timestamp)
                }
                writableDatabase.insert("records", null, cv)
            }
            writableDatabase.setTransactionSuccessful()
        } finally {
            writableDatabase.endTransaction()
        }
    }
}
