package com.example.campusassistant.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ErrandTaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTask(task: ErrandTask): Long

    @Query("SELECT * FROM errand_tasks ORDER BY id DESC")
    fun getAllTasks(): List<ErrandTask>
}