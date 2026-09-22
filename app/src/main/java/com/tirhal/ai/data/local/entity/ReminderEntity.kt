package com.tirhal.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val reminderType: String, // Payment, FlightTime, DocumentExpiry, Custom
    val scheduledTime: Long,
    val isCompleted: Boolean = false,
    val targetType: String? = null, // e.g., Booking, Traveler
    val targetId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
