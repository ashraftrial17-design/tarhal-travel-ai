package com.tirhal.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tirhal.ai.data.local.entity.GuarantorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GuarantorDao {
    @Query("SELECT * FROM guarantors ORDER BY createdAt DESC")
    fun getAllGuarantors(): Flow<List<GuarantorEntity>>

    @Query("SELECT * FROM guarantors WHERE id = :id")
    suspend fun getGuarantorById(id: Long): GuarantorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuarantor(guarantor: GuarantorEntity): Long

    @Update
    suspend fun updateGuarantor(guarantor: GuarantorEntity)

    @Delete
    suspend fun deleteGuarantor(guarantor: GuarantorEntity)
}
