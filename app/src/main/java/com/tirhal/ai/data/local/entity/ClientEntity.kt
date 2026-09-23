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
    val clientType: String = "فرد", // فرد / عائلة / شركة / مؤسسة / مجموعة
    val companionCount: Int = 0,
    val preferredDestination: String? = null,
    val email: String? = null,
    val address: String? = null,
    val notes: String? = null,

    // بيانات السفر والخدمات
    val previousTrips: String? = null,
    val usedServices: String? = null,
    val travelPreferences: String? = null,
    val rating: Float = 0.0f,
    val contactLog: String? = null,

    // بيانات الرحلة الأساسية داخل الملف
    val ticketNumber: String? = null,
    val departureDateTime: String? = null,
    val departureRoute: String? = null,
    val returnDateTime: String? = null,
    val returnRoute: String? = null,
    val airlineCompany: String? = null,
    val flightNumber: String? = null,
    val returnStatus: String? = null,

    // بيانات الضامن / الوسيط
    val guarantorName: String? = null,
    val guarantorPhone: String? = null,
    val guarantorWhatsapp: String? = null,
    val guarantorNotes: String? = null,

    val createdAt: Long = System.currentTimeMillis()
)
