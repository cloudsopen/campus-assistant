package com.example.campusassistant.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface IdleItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItem(item: IdleItem): Long

    @Update
    fun updateItem(item: IdleItem)

    @Delete
    fun deleteItem(item: IdleItem)

    @Query("SELECT * FROM idle_items ORDER BY id DESC")
    fun getAllItems(): List<IdleItem>

    @Query("SELECT * FROM idle_items WHERE id = :itemId")
    fun getItemById(itemId: Long): IdleItem?

    @Query("SELECT * FROM idle_items WHERE category = :categoryName ORDER BY id DESC")
    fun getItemsByCategory(categoryName: String): List<IdleItem>

    @Query("SELECT COUNT(*) FROM idle_items")
    suspend fun getCount(): Int

    @Query("SELECT * FROM idle_items WHERE userId = :userId ORDER BY id DESC")
    suspend fun getItemsByUserId(userId: Long): List<IdleItem>
}