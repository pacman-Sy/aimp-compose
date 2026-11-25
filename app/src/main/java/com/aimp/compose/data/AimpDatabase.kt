package com.aimp.compose.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CachedLyrics::class], version = 1, exportSchema = false)
abstract class AimpDatabase : RoomDatabase() {
    abstract fun lyricsDao(): LyricsDao

    companion object {
        @Volatile
        private var INSTANCE: AimpDatabase? = null

        fun get(context: Context): AimpDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AimpDatabase::class.java,
                "aimp.db"
            ).build().also { INSTANCE = it }
        }
    }
}
