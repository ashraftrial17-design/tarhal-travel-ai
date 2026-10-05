package com.tirhal.ai.data.local

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tirhal.ai.data.local.entity.BookingEntity
import com.tirhal.ai.data.local.entity.ClientEntity
import com.tirhal.ai.data.local.entity.FollowUpActionEntity
import com.tirhal.ai.data.local.entity.TravelerEntity
import com.tirhal.ai.data.local.entity.TravelerRatingEntity
import com.tirhal.ai.data.local.entity.TripEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object DatabaseProvider {
    private const val TAG = "DatabaseProvider"

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
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                seedSampleData(getDatabase(context))
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to seed sample database data: ${e.localizedMessage}", e)
                            }
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
            INSTANCE = instance
            instance
        }
    }

    suspend fun seedSampleData(database: TirhalDatabase) {
        val clientDao = database.clientDao()
        val travelerDao = database.travelerDao()
        val tripDao = database.tripDao()
        val bookingDao = database.bookingDao()
        val followUpDao = database.followUpActionDao()
        val ratingDao = database.travelerRatingDao()

        // Prevent duplicate seeding
        if (clientDao.getAllClients().first().isNotEmpty()) {
            return
        }

        database.withTransaction {
            // 1. Seed initial Clients
            val clientId1 = clientDao.insertClient(
                ClientEntity(
                    fullName = "أحمد محمد العلي",
                    phoneNumber = "+966501234567",
                    whatsappNumber = "+966501234567",
                    email = "ahmed.ali@example.com",
                    address = "الرياض - حي الملز",
                    preferredDestinations = "جدة، دبي",
                    lastTripDate = "2025-01-10",
                    expectedNextTravelDate = "2025-04-10",
                    travelCycleMonths = 3,
                    satisfactionRating = 5,
                    notes = "عميل مميز يسافر بشكل دوري"
                )
            )
            val clientId2 = clientDao.insertClient(
                ClientEntity(
                    fullName = "فاطمة إبراهيم الشمري",
                    phoneNumber = "+966559876543",
                    whatsappNumber = "+966559876543",
                    email = "fatimah.s@example.com",
                    address = "جدة - حي الشاطئ",
                    preferredDestinations = "مكة المكرمة",
                    lastTripDate = "2024-11-20",
                    expectedNextTravelDate = "2025-05-20",
                    travelCycleMonths = 6,
                    satisfactionRating = 4,
                    notes = "تفضل الرحلات العائلية وتذاكر الطيران"
                )
            )
            val clientId3 = clientDao.insertClient(
                ClientEntity(
                    fullName = "خالد بن عبدالله السعدي",
                    phoneNumber = "+966531122334",
                    whatsappNumber = "+966531122334",
                    email = "khaled.s@example.com",
                    address = "الدمام - حي الخزامى",
                    preferredDestinations = "دبي",
                    lastTripDate = "2024-09-10",
                    expectedNextTravelDate = "2025-03-10",
                    travelCycleMonths = 6,
                    satisfactionRating = 2,
                    notes = "عميل منقطع، واجه ملاحظة في رحلته السابقة"
                )
            )

            // 2. Seed corresponding Travelers linked to Clients
            val travelerId1 = travelerDao.insertTraveler(
                TravelerEntity(
                    clientId = clientId1,
                    fullName = "أحمد محمد العلي",
                    phoneNumber = "+966501234567",
                    nationality = "سعودي"
                )
            )
            val travelerId2 = travelerDao.insertTraveler(
                TravelerEntity(
                    clientId = clientId2,
                    fullName = "فاطمة إبراهيم الشمري",
                    phoneNumber = "+966559876543",
                    nationality = "سعودية"
                )
            )
            val travelerId3 = travelerDao.insertTraveler(
                TravelerEntity(
                    clientId = clientId3,
                    fullName = "خالد بن عبدالله السعدي",
                    phoneNumber = "+966531122334",
                    nationality = "سعودي"
                )
            )

            // 3. Seed Trips
            val tripId1 = tripDao.insertTrip(
                TripEntity(
                    tripCode = "TRP-101",
                    origin = "الرياض",
                    destination = "جدة",
                    departureDate = "2025-03-15",
                    departureTime = "08:00 AM",
                    transportationType = "طيران",
                    carrierCompany = "الخطوط السعودية",
                    price = 450.0,
                    status = "مجدولة",
                    notes = "رحلة مباشرة"
                )
            )
            val tripId2 = tripDao.insertTrip(
                TripEntity(
                    tripCode = "TRP-102",
                    origin = "الرياض",
                    destination = "مكة المكرمة",
                    departureDate = "2025-03-20",
                    departureTime = "06:00 AM",
                    transportationType = "حافلة فاخرة",
                    carrierCompany = "شركة سابتكو",
                    price = 180.0,
                    status = "مجدولة",
                    notes = "شاملة وجبة إفطار"
                )
            )
            val tripId3 = tripDao.insertTrip(
                TripEntity(
                    tripCode = "TRP-103",
                    origin = "جدة",
                    destination = "دبي",
                    departureDate = "2024-09-10",
                    departureTime = "10:30 PM",
                    transportationType = "طيران",
                    carrierCompany = "طيران أديل",
                    price = 650.0,
                    status = "مكتملة",
                    notes = "رحلة سابقة"
                )
            )

            // 4. Seed Bookings
            val bookingId1 = bookingDao.insertBooking(
                BookingEntity(
                    bookingReference = "BKG-2025-001",
                    clientId = clientId1,
                    tripId = tripId1,
                    bookingDate = "2025-03-01",
                    status = "مؤكد",
                    totalAmount = 450.0,
                    paidAmount = 450.0,
                    paymentStatus = "مدفوع بالكامل",
                    notes = "طلب مقعد بجوار النافذة"
                )
            )
            val bookingId2 = bookingDao.insertBooking(
                BookingEntity(
                    bookingReference = "BKG-2025-002",
                    clientId = clientId2,
                    tripId = tripId2,
                    bookingDate = "2025-03-02",
                    status = "قيد الانتظار",
                    totalAmount = 180.0,
                    paidAmount = 50.0,
                    paymentStatus = "مدفوع جزئيًا",
                    notes = "يرجى تأكيد الدفع المتبقي قبل السفر"
                )
            )
            bookingDao.insertBooking(
                BookingEntity(
                    bookingReference = "BKG-2024-088",
                    clientId = clientId3,
                    tripId = tripId3,
                    bookingDate = "2024-09-01",
                    status = "مكتمل",
                    totalAmount = 650.0,
                    paidAmount = 650.0,
                    paymentStatus = "مدفوع بالكامل",
                    notes = "رحلة سابقة للمتابعة"
                )
            )

            // 5. Seed Follow-up Actions (using valid travelerId references)
            followUpDao.insertAction(
                FollowUpActionEntity(
                    travelerId = travelerId1,
                    bookingId = bookingId1,
                    actionType = "قبل الرحلة",
                    description = "تأكيد موعد الإقلاع وإرسال تذكرة الإلكترونية",
                    status = "معلقة",
                    dueDate = "2025-03-14",
                    notes = "متابعة هاتفية"
                )
            )
            followUpDao.insertAction(
                FollowUpActionEntity(
                    travelerId = travelerId2,
                    bookingId = bookingId2,
                    actionType = "متابعة الدفع",
                    description = "تحصيل المبلغ المتبقي (130 ريال)",
                    status = "معلقة",
                    dueDate = "2025-03-10",
                    notes = "إرسال رابط السداد"
                )
            )

            // 6. Seed Ratings (using valid travelerId references)
            ratingDao.insertRating(
                TravelerRatingEntity(
                    travelerId = travelerId3,
                    tripId = tripId3,
                    ratingStars = 2,
                    feedback = "تأخير في مواعيد الانطلاق وعدم استجابة السائق",
                    ratingDate = "2024-09-12"
                )
            )
        }
    }
}
