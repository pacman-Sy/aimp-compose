package com.aimp.compose.lyrics

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.aimp.compose.data.CachedLyrics
import com.aimp.compose.data.LyricsDao
import com.aimp.compose.data.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

sealed interface LyricsResult {
    data class Success(val lines: List<LyricLine>, val synced: Boolean, val fromCache: Boolean) : LyricsResult
    data object Instrumental : LyricsResult
    data object NotFound : LyricsResult
    data class Error(val message: String) : LyricsResult
}

class LyricsRepository @javax.inject.Inject constructor(
    private val api: LrcLibApi,
    private val dao: LyricsDao,
    @ApplicationContext private val context: Context
) {
    private val fetchedThisSession = ConcurrentHashMap<Long, Boolean>()

    suspend fun load(track: Track, force: Boolean = false): LyricsResult = withContext(Dispatchers.IO) {
        val cached = dao.get(track.id)
        if (cached != null && !force) {
            return@withContext fromCache(cached)
        }
        if (!isOnline()) {
            return@withContext if (cached != null) fromCache(cached)
            else LyricsResult.Error("Offline — no cached lyrics")
        }
        if (!force && fetchedThisSession.containsKey(track.id)) {
            return@withContext if (cached != null) fromCache(cached)
            else LyricsResult.NotFound
        }
        try {
            fetchedThisSession[track.id] = true
            val response = api.getLyrics(
                trackName = track.title,
                artistName = track.artist,
                albumName = track.album.takeIf { it.isNotBlank() },
                duration = (track.durationMs / 1000).toInt().takeIf { it > 0 }
            )
            if (!response.isSuccessful) {
                if (response.code() == 404) {
                    return@withContext LyricsResult.NotFound
                }
                throw HttpException(response)
            }
            val body = response.body() ?: return@withContext LyricsResult.NotFound
            if (body.instrumental) {
                dao.put(CachedLyrics(track.id, null, null, true))
                return@withContext LyricsResult.Instrumental
            }
            if (body.syncedLyrics.isNullOrBlank() && body.plainLyrics.isNullOrBlank()) {
                return@withContext LyricsResult.NotFound
            }
            dao.put(CachedLyrics(track.id, body.syncedLyrics, body.plainLyrics, false))
            val synced = !body.syncedLyrics.isNullOrBlank()
            val lines = if (synced) LrcParser.parseSynced(body.syncedLyrics)
            else LrcParser.parsePlain(body.plainLyrics)
            LyricsResult.Success(lines, synced, false)
        } catch (e: IOException) {
            if (cached != null) fromCache(cached)
            else LyricsResult.Error("Network timeout")
        } catch (e: HttpException) {
            if (e.code() == 404) LyricsResult.NotFound
            else LyricsResult.Error("Server error ${e.code()}")
        } catch (e: Exception) {
            LyricsResult.Error(e.message ?: "Unknown error")
        }
    }

    private fun fromCache(cached: CachedLyrics): LyricsResult {
        if (cached.instrumental) return LyricsResult.Instrumental
        val synced = !cached.syncedLyrics.isNullOrBlank()
        val lines = if (synced) LrcParser.parseSynced(cached.syncedLyrics)
        else LrcParser.parsePlain(cached.plainLyrics)
        return LyricsResult.Success(lines, synced, true)
    }

    private fun isOnline(): Boolean {
        val cm = context.getSystemService(ConnectivityManager::class.java) ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
