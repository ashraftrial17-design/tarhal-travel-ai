package com.tirhal.ai

import com.tirhal.ai.data.local.entity.ClientEntity
import com.tirhal.ai.ui.screens.calculateNextFollowUpDate
import com.tirhal.ai.ui.screens.generateWhatsAppTemplate
import com.tirhal.ai.ui.screens.getSuggestedNextAction
import com.tirhal.ai.ui.screens.isClientDueForFollowUp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClientModuleUnitTest {

    @Test
    fun testClientEntityDefaults() {
        val client = ClientEntity(
            fullName = "محمد عبدالله",
            phoneNumber = "+966500000000"
        )
        assertEquals("فرد", client.clientType)
        assertEquals("نشط", client.status)
        assertEquals(6, client.travelCycleMonths)
        assertEquals(5, client.satisfactionRating)
    }

    @Test
    fun testCalculateNextFollowUpDate() {
        val lastTrip = "2025-01-01"
        val nextDate = calculateNextFollowUpDate(lastTripDate = lastTrip, expectedNextDate = null, cycleMonths = 3)
        assertEquals("2025-04-01", nextDate)
    }

    @Test
    fun testIsClientDueForFollowUp() {
        val pastClient = ClientEntity(
            fullName = "علي أحمد",
            phoneNumber = "+966511111111",
            lastTripDate = "2024-01-01",
            travelCycleMonths = 3
        )
        assertTrue(isClientDueForFollowUp(pastClient))
    }

    @Test
    fun testSuggestedActionAndWhatsAppTemplate() {
        val client = ClientEntity(
            fullName = "سارة محمود",
            phoneNumber = "+966522222222",
            clientType = "عائلة",
            preferredDestinations = "دبي، مكة",
            status = "منقطع"
        )
        val action = getSuggestedNextAction(client)
        assertTrue(action.contains("عرض خصم خاص"))

        val template = generateWhatsAppTemplate(client)
        assertTrue(template.contains("سارة محمود"))
        assertTrue(template.contains("دبي"))
    }
}
