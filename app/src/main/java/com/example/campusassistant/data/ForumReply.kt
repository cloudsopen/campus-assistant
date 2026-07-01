package com.example.campusassistant.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "forum_replies")
data class ForumReply(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val postId: Long,           // 所属帖子 ID
    val content: String,        // 评论内容
    val author: String?,        // 评论者（可匿名）
    val floorNumber: Int,       // 楼层号（1, 2, 3...）
    val replyTime: Long = System.currentTimeMillis()
)
