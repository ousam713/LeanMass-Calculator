package com.leanmass.calculator.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.security.MessageDigest

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "leanmass.db"
        const val DATABASE_VERSION = 1

        // Table Users
        const val TABLE_USERS = "users"
        const val COL_USER_ID = "id"
        const val COL_USER_NAME = "name"
        const val COL_USER_EMAIL = "email"
        const val COL_USER_PASSWORD = "password"

        // Table History
        const val TABLE_HISTORY = "history"
        const val COL_HIST_ID = "id"
        const val COL_HIST_USER_ID = "user_id"
        const val COL_HIST_WEIGHT = "weight"
        const val COL_HIST_HEIGHT = "height"
        const val COL_HIST_GENDER = "gender"
        const val COL_HIST_LBM = "lbm"
        const val COL_HIST_DATE = "date"

        // Normes LBM
        const val LBM_MIN_MALE = 38.0
        const val LBM_MIN_FEMALE = 24.0
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_USERS (
                $COL_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_NAME TEXT NOT NULL,
                $COL_USER_EMAIL TEXT UNIQUE NOT NULL,
                $COL_USER_PASSWORD TEXT NOT NULL
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_HISTORY (
                $COL_HIST_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_HIST_USER_ID INTEGER NOT NULL,
                $COL_HIST_WEIGHT REAL NOT NULL,
                $COL_HIST_HEIGHT REAL NOT NULL,
                $COL_HIST_GENDER TEXT NOT NULL,
                $COL_HIST_LBM REAL NOT NULL,
                $COL_HIST_DATE TEXT NOT NULL,
                FOREIGN KEY($COL_HIST_USER_ID) REFERENCES $TABLE_USERS($COL_USER_ID)
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HISTORY")
        onCreate(db)
    }

    // Hash password MD5
    fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("MD5")
        val hashBytes = md.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    // ───── USER METHODS ─────

    fun registerUser(name: String, email: String, password: String): Boolean {
        return try {
            val db = writableDatabase
            val values = ContentValues().apply {
                put(COL_USER_NAME, name)
                put(COL_USER_EMAIL, email)
                put(COL_USER_PASSWORD, hashPassword(password))
            }
            db.insertOrThrow(TABLE_USERS, null, values)
            true
        } catch (e: Exception) {
            false // email déjà utilisé
        }
    }

    fun loginUser(email: String, password: String): Int {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COL_USER_ID),
            "$COL_USER_EMAIL = ? AND $COL_USER_PASSWORD = ?",
            arrayOf(email, hashPassword(password)),
            null, null, null
        )
        return if (cursor.moveToFirst()) {
            val id = cursor.getInt(0)
            cursor.close()
            id // retourne l'ID user
        } else {
            cursor.close()
            -1 // échec
        }
    }

    // ───── HISTORY METHODS ─────

    fun saveCalculation(userId: Int, weight: Double, height: Double, gender: String, lbm: Double) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_HIST_USER_ID, userId)
            put(COL_HIST_WEIGHT, weight)
            put(COL_HIST_HEIGHT, height)
            put(COL_HIST_GENDER, gender)
            put(COL_HIST_LBM, lbm)
            put(COL_HIST_DATE, java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date()))
        }
        db.insert(TABLE_HISTORY, null, values)
    }

    fun getHistory(userId: Int): List<HistoryItem> {
        val list = mutableListOf<HistoryItem>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_HISTORY, null,
            "$COL_HIST_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null,
            "$COL_HIST_DATE DESC"
        )
        while (cursor.moveToNext()) {
            list.add(
                HistoryItem(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_HIST_ID)),
                    weight = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_HIST_WEIGHT)),
                    height = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_HIST_HEIGHT)),
                    gender = cursor.getString(cursor.getColumnIndexOrThrow(COL_HIST_GENDER)),
                    lbm = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_HIST_LBM)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COL_HIST_DATE))
                )
            )
        }
        cursor.close()
        return list
    }

    fun deleteCalculation(id: Int) {
        writableDatabase.delete(TABLE_HISTORY, "$COL_HIST_ID = ?", arrayOf(id.toString()))
    }
}

data class HistoryItem(
    val id: Int,
    val weight: Double,
    val height: Double,
    val gender: String,
    val lbm: Double,
    val date: String
)
