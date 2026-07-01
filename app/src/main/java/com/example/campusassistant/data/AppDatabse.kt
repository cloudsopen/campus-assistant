package com.example.campusassistant.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        User::class,
        LostItem::class,
        IdleItem::class,
        BuyRequest::class,
        CarpoolInfo::class,
        ErrandTask::class,
        ForumPost::class,
        ForumReply::class,
        CampusMessage::class
    ],
    version = 10,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun lostItemDao(): LostItemDao
    abstract fun idleItemDao(): IdleItemDao
    abstract fun buyRequestDao(): BuyRequestDao
    abstract fun carpoolInfoDao(): CarpoolInfoDao
    abstract fun errandTaskDao(): ErrandTaskDao
    abstract fun forumPostDao(): ForumPostDao
    abstract fun forumReplyDao(): ForumReplyDao
    abstract fun campusMessageDao(): CampusMessageDao

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
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
