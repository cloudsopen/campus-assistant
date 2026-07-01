package com.example.campusassistant.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface CarpoolInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertInfo(info: CarpoolInfo): Long

    @Update
    fun updateInfo(info: CarpoolInfo)

    @Delete
    fun deleteInfo(info: CarpoolInfo)

    @Query("SELECT * FROM carpool_infos ORDER BY departureTime ASC")
    fun getAllInfo(): List<CarpoolInfo>

    @Query("SELECT * FROM carpool_infos WHERE id = :infoId")
    fun getInfoById(infoId: Long): CarpoolInfo?
}