package com.example.campusassistant.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface LostItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItem(item: LostItem): Long

    @Update
    fun updateItem(item: LostItem)

    @Delete
    fun deleteItem(item: LostItem)

    @Query("SELECT * FROM lost_items ORDER BY id DESC")
    fun getAllItems(): List<LostItem>

    @Query("SELECT * FROM lost_items WHERE id = :itemId")
    fun getItemById(itemId: Long): LostItem?

    @Query("SELECT * FROM lost_items WHERE category = :categoryName ORDER BY id DESC")
    fun getItemsByCategory(categoryName: String): List<LostItem>
}