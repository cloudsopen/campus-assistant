package com.example.campusassistant.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "carpool_infos")
data class CarpoolInfo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val publisher: String?,
    val departure: String,
    val destination: String,
    val departureTime: Long,
    val seats: Int,
    val description: String?,
    val publishTime: Long = System.currentTimeMillis()
)