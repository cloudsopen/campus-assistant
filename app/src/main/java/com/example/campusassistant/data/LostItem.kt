package com.example.campusassistant.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lost_items")
data class LostItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val userId: Long,

    val publisher: String?,

    val title: String,

    val location: String,

    val category: String,

    val lost_time:String,
    
    val description: String,

    val contact_information:String,

    val imagePaths: List<String>? = null
)