package com.example.campusassistant.aid
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_table")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // 快递 / 外卖 / 代买
    val title: String, // 需求描述
    val location: String, // 起止地点
    val timeLimit: String, // 截止时间
    val money: String // 报酬
)