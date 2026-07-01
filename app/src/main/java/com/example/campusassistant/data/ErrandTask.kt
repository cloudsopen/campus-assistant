package com.example.campusassistant.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "errand_tasks")
data class ErrandTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val publisher: String?,
    val title: String,
    val description: String,
    val reward: Double,
    val deadline: Long?,
    val publishTime: Long = System.currentTimeMillis()
)