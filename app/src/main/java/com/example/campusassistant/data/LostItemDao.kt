package com.example.campusassistant.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LostItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItem(item: LostItem): Long

    @Query("SELECT * FROM lost_items ORDER BY id DESC")
    fun getAllItems(): List<LostItem>

    @Query("SELECT * FROM lost_items WHERE category = :categoryName ORDER BY id DESC")
    fun getItemsByCategory(categoryName: String): List<LostItem>
}