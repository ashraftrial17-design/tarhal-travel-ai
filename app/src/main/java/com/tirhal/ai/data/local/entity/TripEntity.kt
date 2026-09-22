package com.tirhal.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tripCode: String,
    val origin: String,
    val destination: String,
    val departureDate: String,
    val departureTime: String? = null,
    val arrivalDate: String? = null,
    val transportationType: String, // e.g., Bus, Flight
    val carrierCompany: String? = null,
    val price: Double,
    val status: String, // e.g., Scheduled, InProgress, Completed, Cancelled
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
