package com.example.campusassistant.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CarpoolInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertInfo(info: CarpoolInfo): Long

    @Query("SELECT * FROM carpool_infos ORDER BY departureTime ASC")
    fun getAllInfo(): List<CarpoolInfo>
}