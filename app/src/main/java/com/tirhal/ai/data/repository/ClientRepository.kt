package com.tirhal.ai.data.repository

import com.tirhal.ai.data.local.dao.ClientDao
import com.tirhal.ai.data.local.entity.ClientEntity
import kotlinx.coroutines.flow.Flow

class ClientRepository(private val clientDao: ClientDao) {
    fun getAllClients(): Flow<List<ClientEntity>> = clientDao.getAllClients()

    fun searchClients(query: String): Flow<List<ClientEntity>> {
        return if (query.isBlank()) {
            clientDao.getAllClients()
        } else {
            clientDao.searchClients(query)
        }
    }

    suspend fun getClientById(id: Long): ClientEntity? = clientDao.getClientById(id)

    suspend fun saveClient(client: ClientEntity): Long {
        return if (client.id == 0L) {
            clientDao.insertClient(client)
        } else {
            clientDao.updateClient(client)
            client.id
        }
    }

    suspend fun deleteClient(client: ClientEntity) = clientDao.deleteClient(client)
}
