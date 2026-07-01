package com.example.campusassistant.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "forum_posts")
data class ForumPost(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,          // 帖子标题
    val content: String,        // 帖子正文
    val author: String?,        // 发布者（可匿名）
    val category: String,       // 分类：学习/生活/活动/求助/其他
    val publishTime: Long = System.currentTimeMillis()
)
