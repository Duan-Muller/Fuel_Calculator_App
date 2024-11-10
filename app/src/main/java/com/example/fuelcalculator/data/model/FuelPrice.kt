package com.example.fuelcalculator.data.model

data class FuelPrice(
    val fuelId: Int,
    val month: String,
    val coastalPrice: Double,
    val inlandPrice: Double
)
