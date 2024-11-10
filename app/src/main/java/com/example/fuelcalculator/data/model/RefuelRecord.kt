package com.example.fuelcalculator.data.model

data class RefuelRecord(
    val entryId: Int = 0,
    val fuelId: Int,
    val userId: String,
    val vehicleId: Int,
    val dateRecorded: String,
    val location: String,
    val price: Double,
    val status: String,
    val liters: Double,
    val odometerReading: Int
)