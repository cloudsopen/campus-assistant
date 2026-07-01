package com.example.campusassistant.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "idle_items")
data class IdleItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val publisher: String?,
    val title: String,
    val description: String,
    val price: Double,
    val category: String, // e.g. electronics, books
    val imagePaths: List<String>? = null,
    val publishTime: Long = System.currentTimeMillis()
)