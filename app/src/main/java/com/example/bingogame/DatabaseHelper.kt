package com.example.bingogame

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * DatabaseHelper manages the SQLite database for the Bingo game.
 * Stores: username, round number, 5 bingo numbers, and system time.
 */
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "BingoGame.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        // Create the database table
        val createTable = "CREATE TABLE bingo_records (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT NOT NULL, " +
                "round_number INTEGER NOT NULL, " +
                "bingo_numbers TEXT NOT NULL, " +
                "system_time TEXT NOT NULL)"
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS bingo_records")
        onCreate(db)
    }

    // Insert a new game record
    fun insertRecord(username: String, round: Int, numbers: String, time: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("username", username)
            put("round_number", round)
            put("bingo_numbers", numbers)
            put("system_time", time)
        }
        return db.insert("bingo_records", null, values)
    }
}