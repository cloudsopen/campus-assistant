package com.example.campusassistant.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "buy_requests")
data class BuyRequest(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val publisher: String?,
    val title: String,
    val description: String,
    val budget: Double,
    val publishTime: Long = System.currentTimeMillis()
)