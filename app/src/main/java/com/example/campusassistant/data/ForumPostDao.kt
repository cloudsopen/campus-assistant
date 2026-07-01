package com.example.campusassistant.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ForumPostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPost(post: ForumPost): Long

    @Query("SELECT * FROM forum_posts ORDER BY publishTime DESC")
    fun getAllPosts(): List<ForumPost>

    @Query("SELECT * FROM forum_posts WHERE category = :categoryName ORDER BY publishTime DESC")
    fun getPostsByCategory(categoryName: String): List<ForumPost>
}
