package com.example.campusassistant.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        LostItem::class,
        IdleItem::class,
        BuyRequest::class,
        CarpoolInfo::class,
        ErrandTask::class
    ],
    version = 2, // 升级版本号
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun lostItemDao(): LostItemDao
    abstract fun idleItemDao(): IdleItemDao
    abstract fun buyRequestDao(): BuyRequestDao
    abstract fun carpoolInfoDao(): CarpoolInfoDao
    abstract fun errandTaskDao(): ErrandTaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "campus_assistant_db"
                )
                .fallbackToDestructiveMigration() // 开发阶段直接摧毁重建，方便修改
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}