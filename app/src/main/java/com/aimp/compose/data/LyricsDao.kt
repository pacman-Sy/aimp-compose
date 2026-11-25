package com.aimp.compose.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LyricsDao {
    @Query("SELECT * FROM lyrics_cache WHERE trackId = :trackId")
    suspend fun get(trackId: Long): CachedLyrics?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(lyrics: CachedLyrics)
}
