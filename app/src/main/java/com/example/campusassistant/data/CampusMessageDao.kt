package com.example.campusassistant.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CampusMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessage(message: CampusMessage): Long

    @Query("SELECT * FROM campus_messages WHERE ownerUserId = :ownerUserId ORDER BY createdAt DESC")
    fun getMessagesByOwner(ownerUserId: Long): List<CampusMessage>

    @Query("SELECT COUNT(*) FROM campus_messages WHERE ownerUserId = :ownerUserId AND isRead = 0")
    fun countUnread(ownerUserId: Long): Int

    @Query("UPDATE campus_messages SET isRead = 1 WHERE id = :messageId")
    fun markRead(messageId: Long)

    @Query("UPDATE campus_messages SET isRead = 1 WHERE ownerUserId = :ownerUserId")
    fun markAllRead(ownerUserId: Long)

    @Query("DELETE FROM campus_messages WHERE id = :messageId")
    fun deleteMessage(messageId: Long)
}
