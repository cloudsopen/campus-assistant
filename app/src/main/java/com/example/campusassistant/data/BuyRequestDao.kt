package com.example.campusassistant.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BuyRequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRequest(request: BuyRequest): Long

    @Query("SELECT * FROM buy_requests ORDER BY id DESC")
    fun getAllRequests(): List<BuyRequest>
}