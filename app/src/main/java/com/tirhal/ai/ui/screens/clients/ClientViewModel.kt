package com.tirhal.ai.ui.screens.clients

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tirhal.ai.data.local.DatabaseProvider
import com.tirhal.ai.data.local.entity.ClientEntity
import com.tirhal.ai.data.repository.ClientRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ClientViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ClientRepository

    init {
        val database = DatabaseProvider.getDatabase(application)
        repository = ClientRepository(database.clientDao())
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val clients: StateFlow<List<ClientEntity>> = _searchQuery
        .flatMapLatest { query ->
            repository.searchClients(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    suspend fun getClientById(id: Long): ClientEntity? {
        return repository.getClientById(id)
    }

    fun saveClient(client: ClientEntity, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveClient(client)
            onSaved()
        }
    }

    fun deleteClient(client: ClientEntity, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteClient(client)
            onDeleted()
        }
    }
}
