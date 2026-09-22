package com.tirhal.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tirhal.ai.data.local.entity.TravelerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TravelerDao {
    @Query("SELECT * FROM travelers ORDER BY createdAt DESC")
    fun getAllTravelers(): Flow<List<TravelerEntity>>

    @Query("SELECT * FROM travelers WHERE id = :id")
    suspend fun getTravelerById(id: Long): TravelerEntity?

    @Query("SELECT * FROM travelers WHERE clientId = :clientId ORDER BY createdAt DESC")
    fun getTravelersByClientId(clientId: Long): Flow<List<TravelerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTraveler(traveler: TravelerEntity): Long

    @Update
    suspend fun updateTraveler(traveler: TravelerEntity)

    @Delete
    suspend fun deleteTraveler(traveler: TravelerEntity)
}
