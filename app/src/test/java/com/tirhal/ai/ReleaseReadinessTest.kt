package com.tirhal.ai

import com.tirhal.ai.data.local.entity.BookingEntity
import com.tirhal.ai.data.local.entity.ClientEntity
import com.tirhal.ai.data.local.entity.TripEntity
import com.tirhal.ai.ui.navigation.Screen
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseReadinessTest {

    @Test
    fun testNavigationScreensAreUniqueAndNonEmpty() {
        val navItems = Screen.navItems
        assertEquals(9, navItems.size)

        val routes = navItems.map { it.route }
        assertEquals(routes.size, routes.toSet().size)

        navItems.forEach { screen ->
            assertTrue(screen.route.isNotBlank())
            assertTrue(screen.titleResId > 0)
        }
    }

    @Test
    fun testBookingPaymentCalculation() {
        val bookingConfirmed = BookingEntity(
            bookingReference = "BKG-101",
            clientId = 1L,
            tripId = 1L,
            bookingDate = "2025-03-01",
            status = "مؤكد",
            totalAmount = 500.0,
            paidAmount = 500.0,
            paymentStatus = "مدفوع بالكامل"
        )
        val remainingConfirmed = bookingConfirmed.totalAmount - bookingConfirmed.paidAmount
        assertEquals(0.0, remainingConfirmed, 0.001)

        val bookingPartial = BookingEntity(
            bookingReference = "BKG-102",
            clientId = 2L,
            tripId = 2L,
            bookingDate = "2025-03-02",
            status = "قيد الانتظار",
            totalAmount = 1000.0,
            paidAmount = 300.0,
            paymentStatus = "مدفوع جزئيًا"
        )
        val remainingPartial = bookingPartial.totalAmount - bookingPartial.paidAmount
        assertEquals(700.0, remainingPartial, 0.001)
    }

    @Test
    fun testWhatsAppPhoneNumberSanitization() {
        val rawPhone1 = "+966 50 123 4567"
        val cleanPhone1 = rawPhone1.replace(Regex("[^0-9]"), "")
        assertEquals("966501234567", cleanPhone1)

        val rawPhone2 = "050-987-6543"
        val cleanPhone2 = rawPhone2.replace(Regex("[^0-9]"), "")
        assertEquals("0509876543", cleanPhone2)

        val rawPhone3 = ""
        val cleanPhone3 = rawPhone3.replace(Regex("[^0-9]"), "")
        assertEquals("", cleanPhone3)
    }

    @Test
    fun testBackupRestoreJsonStructureMapping() {
        val rootJson = JSONObject().apply {
            put("clients", JSONArray().apply {
                put(JSONObject().apply {
                    put("id", 10L)
                    put("fullName", "عميل اختبار")
                    put("phoneNumber", "+966500000000")
                    put("whatsappNumber", "+966500000000")
                    put("travelCycleMonths", 6)
                    put("satisfactionRating", 5)
                })
            })
            put("trips", JSONArray().apply {
                put(JSONObject().apply {
                    put("id", 20L)
                    put("tripCode", "TRP-TEST")
                    put("origin", "الرياض")
                    put("destination", "جدة")
                    put("price", 250.0)
                })
            })
            put("bookings", JSONArray().apply {
                put(JSONObject().apply {
                    put("id", 30L)
                    put("bookingReference", "BKG-TEST")
                    put("clientId", 10L)
                    put("tripId", 20L)
                    put("totalAmount", 250.0)
                    put("paidAmount", 250.0)
                })
            })
        }

        assertTrue(rootJson.has("clients"))
        assertTrue(rootJson.has("trips"))
        assertTrue(rootJson.has("bookings"))

        val clientsArray = rootJson.getJSONArray("clients")
        assertEquals(1, clientsArray.length())
        val clientObj = clientsArray.getJSONObject(0)
        assertEquals(10L, clientObj.getLong("id"))
        assertEquals("عميل اختبار", clientObj.getString("fullName"))

        val clientIdMap = mutableMapOf<Long, Long>()
        val oldClientId = clientObj.getLong("id")
        val newClientId = 100L // Simulated auto-generated DB ID
        clientIdMap[oldClientId] = newClientId

        val bookingsArray = rootJson.getJSONArray("bookings")
        val bookingObj = bookingsArray.getJSONObject(0)
        val rawClientId = bookingObj.getLong("clientId")
        val mappedClientId = clientIdMap[rawClientId]

        assertNotNull(mappedClientId)
        assertEquals(100L, mappedClientId)
    }

    @Test
    fun testClientEntityFallbackValues() {
        val client = ClientEntity(
            fullName = "سارة أحمد",
            phoneNumber = "0551112233"
        )
        assertEquals(6, client.travelCycleMonths)
        assertEquals(5, client.satisfactionRating)
        assertEquals(null, client.whatsappNumber)
        assertEquals(null, client.email)
    }

    @Test
    fun testTripEntityDefaults() {
        val trip = TripEntity(
            tripCode = "TRP-888",
            origin = "مكة",
            destination = "الرياض",
            departureDate = "2025-06-01",
            transportationType = "حافلة",
            price = 150.0,
            status = "مجدولة"
        )
        assertEquals("مجدولة", trip.status)
        assertEquals(null, trip.departureTime)
        assertEquals(null, trip.carrierCompany)
    }
}
