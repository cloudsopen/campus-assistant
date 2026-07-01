package com.example.campusassistant.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface BuyRequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRequest(request: BuyRequest): Long

    @Update
    fun updateRequest(request: BuyRequest)

    @Delete
    fun deleteRequest(request: BuyRequest)

    @Query("SELECT * FROM buy_requests ORDER BY id DESC")
    fun getAllRequests(): List<BuyRequest>

    @Query("SELECT * FROM buy_requests WHERE id = :requestId")
    fun getRequestById(requestId: Long): BuyRequest?

    @Query("SELECT COUNT(*) FROM buy_requests")
    suspend fun getCount(): Int

    @Query("SELECT * FROM buy_requests WHERE userId = :userId ORDER BY id DESC")
    suspend fun getRequestsByUserId(userId: Long): List<BuyRequest>
}