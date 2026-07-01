package com.example.campusassistant.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "errand_tasks")
data class ErrandTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val location: String? = null,    // 起止地点
    val reward: Double,
    val deadline: String? = null,   // 截止时间（字符串形式方便显示）
    val category: String = "快递",   // 分类：快递/外卖/代买/其他
    val status: Int = 0,            // 0: 待接单, 1: 已接单, 2: 已完成
    val publisher: String? = "匿名用户",
    val acceptor: String? = null,   // 接单人
    val publishTime: Long = System.currentTimeMillis()
)