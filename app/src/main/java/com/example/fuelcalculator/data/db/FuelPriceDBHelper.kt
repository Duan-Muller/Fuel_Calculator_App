package com.example.fuelcalculator.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.fuelcalculator.data.model.FuelPrice
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FuelPriceDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    init {
        // Force create table on initialization
        writableDatabase.apply {
            onCreate(this)
        }
    }

    companion object {
        private const val DATABASE_NAME = "Gasify Database"
        private const val DATABASE_VERSION = 3
        private const val TABLE_FUEL_PRICES = "fuel_prices"

        private const val COLUMN_FUEL_ID = "fuel_id"
        private const val COLUMN_MONTH = "month"
        private const val COLUMN_COASTAL_PRICE = "coastal_price"
        private const val COLUMN_INLAND_PRICE = "inland_price"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
        CREATE TABLE IF NOT EXISTS $TABLE_FUEL_PRICES (
            $COLUMN_FUEL_ID INTEGER NOT NULL,
            $COLUMN_MONTH TEXT NOT NULL,
            $COLUMN_COASTAL_PRICE REAL NOT NULL,
            $COLUMN_INLAND_PRICE REAL NOT NULL,
            PRIMARY KEY ($COLUMN_FUEL_ID, $COLUMN_MONTH)
        )
    """.trimIndent()

        db.execSQL(createTable)
        // Add initial data after creating table
        addInitialFuelPrices(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FUEL_PRICES")
        onCreate(db)
    }

    fun getCurrentFuelPrice(fuelType: String, region: String): Double? {
        val fuelId = when (fuelType) {
            "Petrol 93" -> 1
            "Petrol 95" -> 2
            "Diesel 50ppm" -> 3
            "Diesel 500ppm" -> 4
            else -> return null
        }

        val currentMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())

        val db = readableDatabase
        val cursor = db.query(
            TABLE_FUEL_PRICES,
            null,
            "$COLUMN_FUEL_ID = ? AND $COLUMN_MONTH = ?",
            arrayOf(fuelId.toString(), currentMonth),
            null,
            null,
            null
        )

        return if (cursor.moveToFirst()) {
            val price = if (region.equals("Coastal", true)) {
                cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_COASTAL_PRICE))
            } else {
                cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_INLAND_PRICE))
            }
            cursor.close()
            price
        } else {
            cursor.close()
            null
        }
    }

    private fun addInitialFuelPrices(db: SQLiteDatabase = writableDatabase) {
        // Get current month
        val currentMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())

        // Sample fuel prices
        val fuelPrices = listOf(
            FuelPrice(1, currentMonth, 20.19, 20.98),  // Petrol 93
            FuelPrice(2, currentMonth, 20.51, 21.30),  // Petrol 95
            FuelPrice(3, currentMonth, 18.01, 18.77),  // Diesel 50ppm
            FuelPrice(4, currentMonth, 17.87, 18.66)   // Diesel 500ppm
        )

        // Insert prices
        fuelPrices.forEach { price ->
            val values = ContentValues().apply {
                put(COLUMN_FUEL_ID, price.fuelId)
                put(COLUMN_MONTH, price.month)
                put(COLUMN_COASTAL_PRICE, price.coastalPrice)
                put(COLUMN_INLAND_PRICE, price.inlandPrice)
            }
            db.insertWithOnConflict(
                TABLE_FUEL_PRICES,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
            )
        }
    }

    fun checkIfTableHasData(): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_FUEL_PRICES", null)

        return if (cursor.moveToFirst()) {
            val count = cursor.getInt(0)
            cursor.close()
            count > 0
        } else {
            cursor.close()
            false
        }
    }

}