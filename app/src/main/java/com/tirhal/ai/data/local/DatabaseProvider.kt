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

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE clients ADD COLUMN clientType TEXT NOT NULL DEFAULT 'فرد'")
            db.execSQL("ALTER TABLE clients ADD COLUMN status TEXT NOT NULL DEFAULT 'نشط'")
        }
    }

    fun getDatabase(context: Context): TirhalDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                TirhalDatabase::class.java,
                "tirhal_database"
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            seedSampleData(getDatabase(context))
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
            INSTANCE = instance
            instance
        }
    }

    private suspend fun seedSampleData(database: TirhalDatabase) {
        val clientDao = database.clientDao()
        val tripDao = database.tripDao()
        val bookingDao = database.bookingDao()
        val followUpDao = database.followUpActionDao()
        val ratingDao = database.travelerRatingDao()

        // Seed initial Clients
        val clientId1 = clientDao.insertClient(
            ClientEntity(
                fullName = "أحمد محمد العلي",
                phoneNumber = "+966501234567",
                whatsappNumber = "+966501234567",
                email = "ahmed.ali@example.com",
                address = "الرياض - حي الملز",
                clientType = "فرد",
                preferredDestinations = "جدة، دبي",
                lastTripDate = "2025-01-10",
                expectedNextTravelDate = "2025-04-10",
                travelCycleMonths = 3,
                satisfactionRating = 5,
                status = "نشط",
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
                clientType = "عائلة",
                preferredDestinations = "مكة المكرمة",
                lastTripDate = "2024-11-20",
                expectedNextTravelDate = "2025-05-20",
                travelCycleMonths = 6,
                satisfactionRating = 4,
                status = "يحتاج متابعة",
                notes = "تفضل الرحلات العائلية وتذاكر الطيران"
            )
        )
        val clientId3 = clientDao.insertClient(
            ClientEntity(
                fullName = "شركة الفرسان للسياحة والتنظيم",
                phoneNumber = "+966531122334",
                whatsappNumber = "+966531122334",
                email = "info@alforsan.example.com",
                address = "الدمام - حي الخزامى",
                clientType = "شركة",
                preferredDestinations = "دبي، القاهرة",
                lastTripDate = "2024-09-10",
                expectedNextTravelDate = "2025-03-10",
                travelCycleMonths = 6,
                satisfactionRating = 2,
                status = "منقطع",
                notes = "عميل منقطع، واجه ملاحظة في رحلته السابقة"
            )
        )
        val clientId4 = clientDao.insertClient(
            ClientEntity(
                fullName = "مؤسسة الأفق للسفر",
                phoneNumber = "+966540099887",
                whatsappNumber = "+966540099887",
                email = "contact@alofoq.example.com",
                address = "الرياض - العليا",
                clientType = "مؤسسة",
                preferredDestinations = "إسطنبول",
                lastTripDate = "2025-02-01",
                expectedNextTravelDate = "2025-03-25",
                travelCycleMonths = 2,
                satisfactionRating = 5,
                status = "محتمل",
                notes = "مجموعة حجز موسمية"
            )
        )

        // Seed Trips
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

        // Seed Bookings
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

        // Seed Follow-up Actions
        followUpDao.insertAction(
            FollowUpActionEntity(
                travelerId = clientId1,
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
                travelerId = clientId2,
                bookingId = bookingId2,
                actionType = "متابعة الدفع",
                description = "تحصيل المبلغ المتبقي (130 ريال)",
                status = "معلقة",
                dueDate = "2025-03-10",
                notes = "إرسال رابط السداد"
            )
        )

        // Seed Ratings
        ratingDao.insertRating(
            TravelerRatingEntity(
                travelerId = clientId3,
                tripId = tripId3,
                ratingStars = 2,
                feedback = "تأخير في مواعيد الانطلاق وعدم استجابة السائق",
                ratingDate = "2024-09-12"
            )
        )
    }
}
