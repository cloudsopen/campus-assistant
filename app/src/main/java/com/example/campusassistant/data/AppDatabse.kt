package com.example.campusassistant.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [LostItem::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class) // 启用我们刚才写的转换器
abstract class AppDatabase : RoomDatabase() {

    abstract fun lostItemDao(): LostItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "campus_assistant_db" // 数据库文件名
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}