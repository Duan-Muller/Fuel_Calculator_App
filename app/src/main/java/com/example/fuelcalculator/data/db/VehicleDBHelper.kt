package com.example.fuelcalculator.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import com.example.fuelcalculator.data.model.Vehicle

class VehicleDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "Gasify Database"
        private const val DATABASE_VERSION = 3
        private const val TABLE_VEHICLES = "vehicles"

        // Column names
        private const val COLUMN_VEHICLE_ID = "vehicle_id"
        private const val COLUMN_USER_ID = "user_id"
        private const val COLUMN_FUEL_TYPE = "fuel_type"
        private const val COLUMN_VEHICLE_TYPE = "vehicle_type"
        private const val COLUMN_BRAND = "brand"
        private const val COLUMN_MODEL = "model"
        private const val COLUMN_ENGINE_SIZE = "engine_size"
        private const val COLUMN_TRANSMISSION = "transmission"
        private const val COLUMN_YEAR = "year"
        private const val COLUMN_FUEL_EFFICIENCY = "fuel_efficiency"
        private const val COLUMN_MILEAGE = "mileage"
        private const val COLUMN_REGION = "region"
        private const val COLUMN_IS_ACTIVE = "is_active"
    }

    //Create vehicles table
    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_VEHICLES (
                $COLUMN_VEHICLE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID TEXT NOT NULL,
                $COLUMN_FUEL_TYPE TEXT NOT NULL,
                $COLUMN_VEHICLE_TYPE TEXT NOT NULL,
                $COLUMN_BRAND TEXT NOT NULL,
                $COLUMN_MODEL TEXT NOT NULL,
                $COLUMN_ENGINE_SIZE TEXT NOT NULL,
                $COLUMN_TRANSMISSION TEXT NOT NULL,
                $COLUMN_YEAR INTEGER NOT NULL,
                $COLUMN_FUEL_EFFICIENCY REAL NOT NULL,
                $COLUMN_MILEAGE INTEGER NOT NULL,
                $COLUMN_REGION TEXT NOT NULL,
                $COLUMN_IS_ACTIVE INTEGER DEFAULT 0
            )
        """.trimIndent()

        db.execSQL(createTable)
    }

    //Method to set active vehicle
    fun setActiveVehicle(vehicleId: Int, userId: String) {
        val db = this.writableDatabase

        // First, deactivate all vehicles for this user
        val deactivateValues = ContentValues().apply {
            put(COLUMN_IS_ACTIVE, 0)
        }
        db.update(TABLE_VEHICLES, deactivateValues, "$COLUMN_USER_ID = ?", arrayOf(userId))

        // Then activate the selected vehicle
        val activateValues = ContentValues().apply {
            put(COLUMN_IS_ACTIVE, 1)
        }
        db.update(TABLE_VEHICLES, activateValues,
            "$COLUMN_VEHICLE_ID = ? AND $COLUMN_USER_ID = ?",
            arrayOf(vehicleId.toString(), userId))
    }

    fun getVehiclesForUser(userId: String): List<Vehicle> {
        val vehicles = mutableListOf<Vehicle>()
        val db = this.readableDatabase

        try {
            // First verify table exists
            val tableCheck = "SELECT name FROM sqlite_master WHERE type='table' AND name='$TABLE_VEHICLES'"
            val checkCursor = db.rawQuery(tableCheck, null)
            val tableExists = checkCursor.count > 0
            checkCursor.close()

            if (!tableExists) {
                // Recreate table if it doesn't exist
                onCreate(db)
                return emptyList()
            }

            val selection = "$COLUMN_USER_ID = ?"
            val selectionArgs = arrayOf(userId)

            val cursor = db.query(
                TABLE_VEHICLES,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
            )

            with(cursor) {
                while (moveToNext()) {
                    vehicles.add(
                        Vehicle(
                            vehicleId = getInt(getColumnIndexOrThrow(COLUMN_VEHICLE_ID)),
                            userId = getString(getColumnIndexOrThrow(COLUMN_USER_ID)),
                            fuelType = getString(getColumnIndexOrThrow(COLUMN_FUEL_TYPE)),
                            vehicleType = getString(getColumnIndexOrThrow(COLUMN_VEHICLE_TYPE)),
                            brand = getString(getColumnIndexOrThrow(COLUMN_BRAND)),
                            model = getString(getColumnIndexOrThrow(COLUMN_MODEL)),
                            engineSize = getString(getColumnIndexOrThrow(COLUMN_ENGINE_SIZE)),
                            transmission = getString(getColumnIndexOrThrow(COLUMN_TRANSMISSION)),
                            year = getInt(getColumnIndexOrThrow(COLUMN_YEAR)),
                            fuelEfficiency = getDouble(getColumnIndexOrThrow(COLUMN_FUEL_EFFICIENCY)),
                            mileage = getInt(getColumnIndexOrThrow(COLUMN_MILEAGE)),
                            region = getString(getColumnIndexOrThrow(COLUMN_REGION)),
                            isActive = getInt(getColumnIndexOrThrow(COLUMN_IS_ACTIVE)) == 1
                        )
                    )
                }
            }
            cursor.close()
            return vehicles
        } catch (e: Exception) {
            // Log error and return empty list instead of crashing
            e.printStackTrace()
            return emptyList()
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        //Check if table exists first
        val tableCheck = "SELECT name FROM sqlite_master WHERE type='table' AND name='$TABLE_VEHICLES'"
        val cursor = db.rawQuery(tableCheck, null)
        val tableExists = cursor.count > 0
        cursor.close()

        if (!tableExists) {
            onCreate(db)
        }
    }

    fun addVehicle(vehicle: Vehicle): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_ID, vehicle.userId)
            put(COLUMN_FUEL_TYPE, vehicle.fuelType)
            put(COLUMN_VEHICLE_TYPE, vehicle.vehicleType)
            put(COLUMN_BRAND, vehicle.brand)
            put(COLUMN_MODEL, vehicle.model)
            put(COLUMN_ENGINE_SIZE, vehicle.engineSize)
            put(COLUMN_TRANSMISSION, vehicle.transmission)
            put(COLUMN_YEAR, vehicle.year)
            put(COLUMN_FUEL_EFFICIENCY, vehicle.fuelEfficiency)
            put(COLUMN_MILEAGE, vehicle.mileage)
            put(COLUMN_REGION, vehicle.region)
            put(COLUMN_IS_ACTIVE, if (vehicle.isActive) 1 else 0)
        }
        return db.insert(TABLE_VEHICLES, null, values)
    }

    fun getVehicleById(vehicleId: Int): Vehicle? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_VEHICLES,
            null,
            "$COLUMN_VEHICLE_ID = ?",
            arrayOf(vehicleId.toString()),
            null,
            null,
            null
        )

        return if (cursor.moveToFirst()) {
            val vehicle = Vehicle(
                vehicleId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_VEHICLE_ID)),
                userId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                fuelType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FUEL_TYPE)),
                vehicleType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VEHICLE_TYPE)),
                brand = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BRAND)),
                model = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MODEL)),
                engineSize = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENGINE_SIZE)),
                transmission = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSMISSION)),
                year = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_YEAR)),
                fuelEfficiency = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_FUEL_EFFICIENCY)),
                mileage = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MILEAGE)),
                region = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REGION)),
                isActive = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ACTIVE)) == 1
            )
            cursor.close()
            vehicle
        } else {
            cursor.close()
            null
        }
    }

    fun getActiveVehicle(userId: String): Vehicle? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_VEHICLES,
            null,
            "$COLUMN_USER_ID = ? AND $COLUMN_IS_ACTIVE = ?",
            arrayOf(userId, "1"),
            null,
            null,
            null
        )

        return if (cursor.moveToFirst()) {
            val vehicle = Vehicle(
                vehicleId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_VEHICLE_ID)),
                userId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                fuelType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FUEL_TYPE)),
                vehicleType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VEHICLE_TYPE)),
                brand = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BRAND)),
                model = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MODEL)),
                engineSize = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENGINE_SIZE)),
                transmission = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSMISSION)),
                year = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_YEAR)),
                fuelEfficiency = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_FUEL_EFFICIENCY)),
                mileage = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MILEAGE)),
                region = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REGION)),
                isActive = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ACTIVE)) == 1
            )
            cursor.close()
            vehicle
        } else {
            cursor.close()
            null
        }
    }

    fun updateVehicleMileage(vehicleId: Int, newMileage: Int) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_MILEAGE, newMileage)
        }
        db.update(
            TABLE_VEHICLES,
            values,
            "$COLUMN_VEHICLE_ID = ?",
            arrayOf(vehicleId.toString())
        )
    }

    fun removeVehicle(vehicleId: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_VEHICLES, "$COLUMN_VEHICLE_ID = ?", arrayOf(vehicleId.toString()))
    }

}