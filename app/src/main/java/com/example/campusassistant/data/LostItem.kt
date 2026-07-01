package com.example.campusassistant.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lost_items")
data class LostItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val publisher: String?,

    val location: String,

    val category: String,

    val losttime:String,
    
    val description: String,

    val imagePaths: List<String>? = null
)