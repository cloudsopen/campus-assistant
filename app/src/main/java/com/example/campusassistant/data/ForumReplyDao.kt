package com.example.campusassistant.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ForumReplyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReply(reply: ForumReply): Long

    @Query("SELECT * FROM forum_replies WHERE postId = :postId ORDER BY replyTime ASC")
    fun getRepliesByPostId(postId: Long): List<ForumReply>

    @Query("SELECT COALESCE(MAX(floorNumber), 0) FROM forum_replies WHERE postId = :postId")
    fun getMaxFloorNumber(postId: Long): Int

    @Query("SELECT COUNT(*) FROM forum_replies WHERE postId = :postId")
    fun getReplyCount(postId: Long): Int
}
