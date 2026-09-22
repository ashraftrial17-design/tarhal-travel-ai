package com.tirhal.ai.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bookings",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = GuarantorEntity::class,
            parentColumns = ["id"],
            childColumns = ["guarantorId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("clientId"),
        Index("tripId"),
        Index("guarantorId")
    ]
)
data class BookingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookingReference: String,
    val clientId: Long,
    val tripId: Long,
    val guarantorId: Long? = null,
    val bookingDate: String,
    val status: String, // Pending, Confirmed, Cancelled
    val totalAmount: Double,
    val paidAmount: Double,
    val paymentStatus: String, // Unpaid, Partial, Paid
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
