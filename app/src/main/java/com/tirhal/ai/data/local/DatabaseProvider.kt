package com.tirhal.ai.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tirhal.ai.data.local.entity.BookingEntity
import com.tirhal.ai.data.local.entity.ClientEntity
import com.tirhal.ai.data.local.entity.FollowUpActionEntity
import com.tirhal.ai.data.local.entity.TravelerRatingEntity
import com.tirhal.ai.data.local.entity.TripEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object DatabaseProvider {
    @Volatile
    private var INSTANCE: TirhalDatabase? = null

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE clients ADD COLUMN whatsappNumber TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE clients ADD COLUMN preferredDestinations TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE clients ADD COLUMN lastTripDate TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE clients ADD COLUMN expectedNextTravelDate TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE clients ADD COLUMN travelCycleMonths INTEGER NOT NULL DEFAULT 6")
            db.execSQL("ALTER TABLE clients ADD COLUMN satisfactionRating INTEGER NOT NULL DEFAULT 5")
        }
    }

    fun getDatabase(context: Context): TirhalDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                TirhalDatabase::class.java,
                "tirhal_database"
            )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
            INSTANCE = instance
            instance
        }
    }
}
