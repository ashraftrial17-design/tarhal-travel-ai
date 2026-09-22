package com.tirhal.ai.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "traveler_ratings",
    foreignKeys = [
        ForeignKey(
            entity = TravelerEntity::class,
            parentColumns = ["id"],
            childColumns = ["travelerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("travelerId"),
        Index("tripId")
    ]
)
data class TravelerRatingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val travelerId: Long,
    val tripId: Long? = null,
    val ratingStars: Int, // 1 to 5
    val feedback: String? = null,
    val ratingDate: String,
    val createdAt: Long = System.currentTimeMillis()
)
