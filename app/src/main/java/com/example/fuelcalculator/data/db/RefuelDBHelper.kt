package com.example.fuelcalculator.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.icu.text.SimpleDateFormat
import com.example.fuelcalculator.data.model.RefuelRecord
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RefuelDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "Gasify Database"
        private const val DATABASE_VERSION = 3
        private const val TABLE_REFUEL_HISTORY = "refuel_history"

        // Column names
        private const val COLUMN_ENTRY_ID = "entry_id"
        private const val COLUMN_FUEL_ID = "fuel_id"
        private const val COLUMN_USER_ID = "user_id"
        private const val COLUMN_VEHICLE_ID = "vehicle_id"
        private const val COLUMN_DATE_RECORDED = "date_recorded"
        private const val COLUMN_LOCATION = "location"
        private const val COLUMN_PRICE = "price"
        private const val COLUMN_STATUS = "status"
        private const val COLUMN_LITERS = "liters"
        private const val COLUMN_ODOMETER = "odometer_reading"
    }

    init {
        // Force create table on initialization
        createRefuelTable(writableDatabase)
    }

    private fun createRefuelTable(db: SQLiteDatabase) {
        val createRefuelTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_REFUEL_HISTORY (
                $COLUMN_ENTRY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_FUEL_ID INTEGER NOT NULL,
                $COLUMN_USER_ID TEXT NOT NULL,
                $COLUMN_VEHICLE_ID INTEGER NOT NULL,
                $COLUMN_DATE_RECORDED TEXT NOT NULL,
                $COLUMN_LOCATION TEXT NOT NULL,
                $COLUMN_PRICE REAL NOT NULL,
                $COLUMN_STATUS TEXT NOT NULL,
                $COLUMN_LITERS REAL NOT NULL,
                $COLUMN_ODOMETER INTEGER NOT NULL
            )
        """.trimIndent()

        db.execSQL(createRefuelTable)
    }

    override fun onCreate(db: SQLiteDatabase) {
        createRefuelTable(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_REFUEL_HISTORY")
        onCreate(db)
    }

    fun addRefuelEntry(
        fuelId: Int,
        userId: String,
        vehicleId: Int,
        location: String,
        price: Double,
        status: String,
        liters: Double,
        odometerReading: Int
    ): Long {
        createRefuelTable(writableDatabase)

        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FUEL_ID, fuelId)
            put(COLUMN_USER_ID, userId)
            put(COLUMN_VEHICLE_ID, vehicleId)
            put(COLUMN_DATE_RECORDED, SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
            put(COLUMN_LOCATION, location)
            put(COLUMN_PRICE, price)
            put(COLUMN_STATUS, status)
            put(COLUMN_LITERS, liters)
            put(COLUMN_ODOMETER, odometerReading)
        }
        return db.insert(TABLE_REFUEL_HISTORY, null, values)
    }

    fun getRefuelHistory(userId: String): List<RefuelRecord> {
        val history = mutableListOf<RefuelRecord>()
        val db = this.readableDatabase

        val cursor = db.query(
            TABLE_REFUEL_HISTORY,
            null,
            "$COLUMN_USER_ID = ?",
            arrayOf(userId),
            null,
            null,
            "$COLUMN_DATE_RECORDED DESC"
        )

        with(cursor) {
            while (moveToNext()) {
                val record = RefuelRecord(
                    entryId = getInt(getColumnIndexOrThrow(COLUMN_ENTRY_ID)),
                    fuelId = getInt(getColumnIndexOrThrow(COLUMN_FUEL_ID)),
                    userId = getString(getColumnIndexOrThrow(COLUMN_USER_ID)),
                    vehicleId = getInt(getColumnIndexOrThrow(COLUMN_VEHICLE_ID)),
                    dateRecorded = getString(getColumnIndexOrThrow(COLUMN_DATE_RECORDED)),
                    location = getString(getColumnIndexOrThrow(COLUMN_LOCATION)),
                    price = getDouble(getColumnIndexOrThrow(COLUMN_PRICE)),
                    status = getString(getColumnIndexOrThrow(COLUMN_STATUS)),
                    liters = getDouble(getColumnIndexOrThrow(COLUMN_LITERS)),
                    odometerReading = getInt(getColumnIndexOrThrow(COLUMN_ODOMETER))
                )
                history.add(record)
            }
        }
        cursor.close()
        return history
    }

    fun getCurrentMonthTotal(userId: String): Double {
        val db = this.readableDatabase

        // Get current month's start and end dates
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        // Set to first day of current month
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startDate = dateFormat.format(calendar.time)

        // Set to last day of current month
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endDate = dateFormat.format(calendar.time)

        val query = """
            SELECT SUM($COLUMN_PRICE) as total 
            FROM $TABLE_REFUEL_HISTORY 
            WHERE $COLUMN_USER_ID = ? 
            AND $COLUMN_DATE_RECORDED BETWEEN ? AND ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId, startDate, endDate))

        var total = 0.0
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0)
        }

        cursor.close()
        return total
    }


}