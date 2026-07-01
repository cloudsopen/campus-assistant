package com.example.campusassistant.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ErrandTaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTask(task: ErrandTask): Long

    @androidx.room.Update
    fun updateTask(task: ErrandTask)

    @Query("SELECT * FROM errand_tasks ORDER BY publishTime DESC")
    fun getAllTasks(): List<ErrandTask>

    @Query("SELECT * FROM errand_tasks WHERE status = :status ORDER BY publishTime DESC")
    fun getTasksByStatus(status: Int): List<ErrandTask>

    @Query("SELECT * FROM errand_tasks WHERE category = :category AND status = 0 ORDER BY publishTime DESC")
    fun getPendingTasksByCategory(category: String): List<ErrandTask>

    @Query("SELECT * FROM errand_tasks WHERE id = :taskId")
    fun getTaskById(taskId: Long): ErrandTask?

    @androidx.room.Delete
    fun deleteTask(task: ErrandTask)
}