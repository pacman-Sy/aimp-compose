package com.aimp.compose.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lyrics_cache")
data class CachedLyrics(
    @PrimaryKey val trackId: Long,
    val syncedLyrics: String?,
    val plainLyrics: String?,
    val instrumental: Boolean,
    val fetchedAt: Long = System.currentTimeMillis()
)
