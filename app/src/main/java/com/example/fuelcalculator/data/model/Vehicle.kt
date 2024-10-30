package com.example.fuelcalculator.data.model

data class Vehicle(
    val vehicleId: Int = 0,
    val userId: String,  // Firebase Auth User ID
    val fuelType: String,
    val vehicleType: String,
    val brand: String,
    val model: String,
    val engineSize: String,
    val transmission: String,
    val year: Int,
    val fuelEfficiency: Double,
    val mileage: Int,
    val region: String,
    val isActive: Boolean = false
)
