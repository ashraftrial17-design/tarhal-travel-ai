package com.tirhal.ai.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "follow_up_actions",
    foreignKeys = [
        ForeignKey(
            entity = TravelerEntity::class,
            parentColumns = ["id"],
            childColumns = ["travelerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BookingEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookingId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("travelerId"),
        Index("bookingId")
    ]
)
data class FollowUpActionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val travelerId: Long,
    val bookingId: Long? = null,
    val actionType: String, // PreTrip, InTransit, PostTrip, DocumentCheck
    val description: String,
    val status: String, // Pending, InProgress, Completed, Deferred
    val dueDate: String,
    val completedAt: Long? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
