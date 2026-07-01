package com.example.campusassistant.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "campus_messages")
data class CampusMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ownerUserId: Long,
    val type: String,
    val title: String,
    val content: String,
    val relatedTitle: String? = null,
    val actorName: String? = null,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
