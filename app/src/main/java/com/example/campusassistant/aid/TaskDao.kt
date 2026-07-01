package com.example.campusassistant.aid

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    // 获取全部需求
    @Query("SELECT * FROM task_table")
    fun getAllTask(): Flow<List<Task>>

    // 根据类型筛选
    @Query("SELECT * FROM task_table WHERE type = :taskType")
    fun getTaskByType(taskType: String): Flow<List<Task>>

    // 根据id查询单条需求（编辑用）
    @Query("SELECT * FROM task_table WHERE id = :taskId")
    suspend fun getTaskById(taskId: Int): Task?

    // 新增需求
    @Insert
    suspend fun insertTask(task: Task)

    // 修改需求
    @Update
    suspend fun updateTask(task: Task)

    // 删除需求
    @Delete
    suspend fun deleteTask(task: Task)
}