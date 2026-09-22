package com.tirhal.ai.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tickets",
    foreignKeys = [
        ForeignKey(
            entity = BookingEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookingId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TravelerEntity::class,
            parentColumns = ["id"],
            childColumns = ["travelerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("bookingId"),
        Index("travelerId")
    ]
)
data class TicketEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ticketNumber: String,
    val bookingId: Long,
    val travelerId: Long,
    val seatNumber: String? = null,
    val price: Double,
    val issueDate: String,
    val status: String, // Active, Used, Refunded, Cancelled
    val qrCodeData: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
