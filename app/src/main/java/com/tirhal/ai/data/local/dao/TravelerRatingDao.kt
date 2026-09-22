package com.tirhal.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tirhal.ai.data.local.entity.TravelerRatingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TravelerRatingDao {
    @Query("SELECT * FROM traveler_ratings ORDER BY createdAt DESC")
    fun getAllRatings(): Flow<List<TravelerRatingEntity>>

    @Query("SELECT * FROM traveler_ratings WHERE travelerId = :travelerId")
    fun getRatingsByTravelerId(travelerId: Long): Flow<List<TravelerRatingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRating(rating: TravelerRatingEntity): Long

    @Update
    suspend fun updateRating(rating: TravelerRatingEntity)

    @Delete
    suspend fun deleteRating(rating: TravelerRatingEntity)
}
