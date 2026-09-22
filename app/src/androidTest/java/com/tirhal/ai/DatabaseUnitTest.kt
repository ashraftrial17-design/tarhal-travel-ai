package com.tirhal.ai

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tirhal.ai.data.local.TirhalDatabase
import com.tirhal.ai.data.local.entity.ClientEntity
import com.tirhal.ai.data.local.entity.TravelerEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseUnitTest {

    private lateinit var db: TirhalDatabase

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TirhalDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testInsertAndGetClient() = runBlocking {
        val client = ClientEntity(
            fullName = "أحمد محمد",
            phoneNumber = "+967770000000",
            email = "ahmed@example.com"
        )
        val id = db.clientDao().insertClient(client)
        val fetchedClient = db.clientDao().getClientById(id)
        assertNotNull(fetchedClient)
        assertEquals("أحمد محمد", fetchedClient?.fullName)
    }

    @Test
    fun testInsertAndGetTraveler() = runBlocking {
        val traveler = TravelerEntity(
            fullName = "سارة علي",
            passportNumber = "A12345678",
            nationality = "اليمن"
        )
        val id = db.travelerDao().insertTraveler(traveler)
        val travelers = db.travelerDao().getAllTravelers().first()
        assertEquals(1, travelers.size)
        assertEquals("سارة علي", travelers[0].fullName)
    }
}
