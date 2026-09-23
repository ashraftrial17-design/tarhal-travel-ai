package com.tirhal.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val phoneNumber: String,
    val whatsappNumber: String? = null,
    val email: String? = null,
    val address: String? = null,
    val preferredDestinations: String? = null,
    val lastTripDate: String? = null,
    val expectedNextTravelDate: String? = null,
    val travelCycleMonths: Int = 6,
    val satisfactionRating: Int = 5, // 1 to 5
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
