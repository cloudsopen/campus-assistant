package com.example.campusassistant.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface IdleItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItem(item: IdleItem): Long

    @Query("SELECT * FROM idle_items ORDER BY id DESC")
    fun getAllItems(): List<IdleItem>

    @Query("SELECT * FROM idle_items WHERE category = :categoryName ORDER BY id DESC")
    fun getItemsByCategory(categoryName: String): List<IdleItem>
}