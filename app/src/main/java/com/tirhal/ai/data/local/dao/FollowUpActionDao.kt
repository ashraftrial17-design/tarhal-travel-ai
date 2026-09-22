package com.tirhal.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tirhal.ai.data.local.entity.FollowUpActionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowUpActionDao {
    @Query("SELECT * FROM follow_up_actions ORDER BY dueDate ASC")
    fun getAllFollowUpActions(): Flow<List<FollowUpActionEntity>>

    @Query("SELECT * FROM follow_up_actions WHERE travelerId = :travelerId")
    fun getActionsByTravelerId(travelerId: Long): Flow<List<FollowUpActionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAction(action: FollowUpActionEntity): Long

    @Update
    suspend fun updateAction(action: FollowUpActionEntity)

    @Delete
    suspend fun deleteAction(action: FollowUpActionEntity)
}
