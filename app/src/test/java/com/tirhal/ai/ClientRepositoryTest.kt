package com.tirhal.ai

import com.tirhal.ai.data.local.dao.ClientDao
import com.tirhal.ai.data.local.entity.ClientEntity
import com.tirhal.ai.data.repository.ClientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class FakeClientDao : ClientDao {
    private val clientsList = mutableListOf<ClientEntity>()
    private var currentId = 0L

    override fun getAllClients(): Flow<List<ClientEntity>> {
        return flowOf(clientsList.sortedByDescending { it.createdAt })
    }

    override suspend fun getClientById(id: Long): ClientEntity? {
        return clientsList.find { it.id == id }
    }

    override fun searchClients(query: String): Flow<List<ClientEntity>> {
        val filtered = clientsList.filter {
            it.fullName.contains(query, ignoreCase = true) ||
            it.phoneNumber.contains(query, ignoreCase = true) ||
            (it.whatsappNumber?.contains(query, ignoreCase = true) == true)
        }.sortedByDescending { it.createdAt }
        return flowOf(filtered)
    }

    override suspend fun insertClient(client: ClientEntity): Long {
        val newId = ++currentId
        val inserted = client.copy(id = newId)
        clientsList.add(inserted)
        return newId
    }

    override suspend fun updateClient(client: ClientEntity) {
        val index = clientsList.indexOfFirst { it.id == client.id }
        if (index != -1) {
            clientsList[index] = client
        }
    }

    override suspend fun deleteClient(client: ClientEntity) {
        clientsList.removeAll { it.id == client.id }
    }
}

class ClientRepositoryTest {
    private lateinit var fakeDao: FakeClientDao
    private lateinit var repository: ClientRepository

    @Before
    fun setUp() {
        fakeDao = FakeClientDao()
        repository = ClientRepository(fakeDao)
    }

    @Test
    fun testAddAndGetClient() = runBlocking {
        val client = ClientEntity(
            fullName = "محمد أحمد",
            phoneNumber = "0912345678",
            whatsappNumber = "0912345678",
            clientType = "عائلة",
            preferredDestination = "القاهرة"
        )

        val id = repository.saveClient(client)
        val retrieved = repository.getClientById(id)

        assertNotNull(retrieved)
        assertEquals("محمد أحمد", retrieved?.fullName)
        assertEquals("0912345678", retrieved?.phoneNumber)
        assertEquals("عائلة", retrieved?.clientType)
        assertEquals("القاهرة", retrieved?.preferredDestination)
    }

    @Test
    fun testUpdateClient() = runBlocking {
        val client = ClientEntity(
            fullName = "عمر خالد",
            phoneNumber = "0999888777"
        )
        val id = repository.saveClient(client)

        val existing = repository.getClientById(id)!!
        val updated = existing.copy(fullName = "عمر خالد التوم", preferredDestination = "جدة")
        repository.saveClient(updated)

        val result = repository.getClientById(id)
        assertEquals("عمر خالد التوم", result?.fullName)
        assertEquals("جدة", result?.preferredDestination)
    }

    @Test
    fun testSearchClients() = runBlocking {
        repository.saveClient(ClientEntity(fullName = "أحمد مصطفى", phoneNumber = "0111111111"))
        repository.saveClient(ClientEntity(fullName = "سارة حسن", phoneNumber = "0222222222"))

        val searchByName = repository.searchClients("أحمد").first()
        assertEquals(1, searchByName.size)
        assertEquals("أحمد مصطفى", searchByName[0].fullName)

        val searchByPhone = repository.searchClients("0222").first()
        assertEquals(1, searchByPhone.size)
        assertEquals("سارة حسن", searchByPhone[0].fullName)
    }

    @Test
    fun testDeleteClient() = runBlocking {
        val client = ClientEntity(fullName = "طارق الماحي", phoneNumber = "0333333333")
        val id = repository.saveClient(client)

        val saved = repository.getClientById(id)!!
        repository.deleteClient(saved)

        val deleted = repository.getClientById(id)
        assertNull(deleted)
    }
}
