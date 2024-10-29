package com.example.fuelcalculator.data.model

import java.util.Date

data class RefuelRecord(
    val id: Long = 0,
    val userId: String,
    val date: Date,
    val liters: Double,
    val pricePerLiter: Double,
    val totalAmount: Double,
    val kilometers: Double? = null,
    val location: String? = null
)   {

    fun getFormattedDate(): String {
        return date.toString()
    }

    fun getTotalCost(): Double {
        return liters * pricePerLiter
    }
}
