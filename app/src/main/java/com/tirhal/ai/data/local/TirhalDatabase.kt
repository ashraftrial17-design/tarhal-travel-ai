package com.tirhal.ai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tirhal.ai.data.local.dao.BookingDao
import com.tirhal.ai.data.local.dao.ClientDao
import com.tirhal.ai.data.local.dao.FollowUpActionDao
import com.tirhal.ai.data.local.dao.GuarantorDao
import com.tirhal.ai.data.local.dao.ReminderDao
import com.tirhal.ai.data.local.dao.TicketDao
import com.tirhal.ai.data.local.dao.TravelerDao
import com.tirhal.ai.data.local.dao.TravelerRatingDao
import com.tirhal.ai.data.local.dao.TripDao
import com.tirhal.ai.data.local.entity.BookingEntity
import com.tirhal.ai.data.local.entity.ClientEntity
import com.tirhal.ai.data.local.entity.FollowUpActionEntity
import com.tirhal.ai.data.local.entity.GuarantorEntity
import com.tirhal.ai.data.local.entity.ReminderEntity
import com.tirhal.ai.data.local.entity.TicketEntity
import com.tirhal.ai.data.local.entity.TravelerEntity
import com.tirhal.ai.data.local.entity.TravelerRatingEntity
import com.tirhal.ai.data.local.entity.TripEntity

@Database(
    entities = [
        ClientEntity::class,
        TravelerEntity::class,
        TripEntity::class,
        BookingEntity::class,
        TicketEntity::class,
        FollowUpActionEntity::class,
        ReminderEntity::class,
        TravelerRatingEntity::class,
        GuarantorEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class TirhalDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao
    abstract fun travelerDao(): TravelerDao
    abstract fun tripDao(): TripDao
    abstract fun bookingDao(): BookingDao
    abstract fun ticketDao(): TicketDao
    abstract fun followUpActionDao(): FollowUpActionDao
    abstract fun reminderDao(): ReminderDao
    abstract fun travelerRatingDao(): TravelerRatingDao
    abstract fun guarantorDao(): GuarantorDao
}
