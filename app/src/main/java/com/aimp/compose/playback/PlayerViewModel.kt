package com.aimp.compose.playback

import android.content.ComponentName
import android.content.Context
import android.media.audiofx.Equalizer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.aimp.compose.data.SettingsStore
import com.aimp.compose.data.Track
import com.aimp.compose.lyrics.LyricLine
import com.aimp.compose.lyrics.LyricsRepository
import com.aimp.compose.lyrics.LyricsResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

data class PlayerUiState(
    val track: Track? = null,
    val queue: List<Track> = emptyList(),
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val lyrics: List<LyricLine> = emptyList(),
    val lyricsSynced: Boolean = true,
    val lyricsState: LyricsUiState = LyricsUiState.Idle,
    val currentLine: Int = -1,
    val autoLyrics: Boolean = true,
    val darkTheme: Boolean = true,
    val lyricsMode: Int = 0,
    val speed: Float = 1f
)

sealed interface LyricsUiState {
    data object Idle : LyricsUiState
    data object Loading : LyricsUiState
    data object Instrumental : LyricsUiState
    data object NotFound : LyricsUiState
    data class Error(val message: String) : LyricsUiState
}

sealed interface PlayerIntent {
    data class Play(val track: Track, val queue: List<Track> = emptyList()) : PlayerIntent
    data object TogglePlay : PlayerIntent
    data object Next : PlayerIntent
    data object Prev : PlayerIntent
    data class Seek(val ms: Long) : PlayerIntent
    data object RefreshLyrics : PlayerIntent
    data class SetLyricsMode(val mode: Int) : PlayerIntent
    data class SetAutoLyrics(val enabled: Boolean) : PlayerIntent
    data class SetDarkTheme(val enabled: Boolean) : PlayerIntent
    data class SetSpeed(val speed: Float) : PlayerIntent
    data class SetBand(val index: Int, val level: Short) : PlayerIntent
}

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lyricsRepository: LyricsRepository,
    private val settingsStore: SettingsStore
) : ViewModel() {

    private val _state = MutableStateFlow(PlayerUiState())
    val state: StateFlow<PlayerUiState> = _state.asStateFlow()

    private var controller: MediaController? = null
    private var equalizer: Equalizer? = null

    init {
        viewModelScope.launch {
            controller = awaitController()
            controller?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _state.value = _state.value.copy(isPlaying = isPlaying)
                    if (isPlaying) attachEqualizer()
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    val index = controller?.currentMediaItemIndex ?: -1
                    val track = _state.value.queue.getOrNull(index)
                    if (track != null) onTrackChanged(track)
                }
            })
            startPositionPolling()
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(
                autoLyrics = settingsStore.autoLyrics.first(),
                darkTheme = settingsStore.darkTheme.first(),
                lyricsMode = settingsStore.lyricsMode.first()
            )
        }
    }

    fun handle(intent: PlayerIntent) {
        when (intent) {
            is PlayerIntent.Play -> play(intent.track, intent.queue)
            PlayerIntent.TogglePlay -> controller?.let {
                if (it.isPlaying) it.pause() else it.play()
            }
            PlayerIntent.Next -> controller?.seekToNextMediaItem()
            PlayerIntent.Prev -> controller?.seekToPreviousMediaItem()
            is PlayerIntent.Seek -> controller?.seekTo(intent.ms)
            PlayerIntent.RefreshLyrics -> _state.value.track?.let { loadLyrics(it, force = true) }
            is PlayerIntent.SetLyricsMode -> viewModelScope.launch {
                settingsStore.setLyricsMode(intent.mode)
                _state.value = _state.value.copy(lyricsMode = intent.mode)
            }
            is PlayerIntent.SetAutoLyrics -> viewModelScope.launch {
                settingsStore.setAutoLyrics(intent.enabled)
                _state.value = _state.value.copy(autoLyrics = intent.enabled)
            }
            is PlayerIntent.SetDarkTheme -> viewModelScope.launch {
                settingsStore.setDarkTheme(intent.enabled)
                _state.value = _state.value.copy(darkTheme = intent.enabled)
            }
            is PlayerIntent.SetSpeed -> {
                controller?.setPlaybackSpeed(intent.speed.coerceIn(0.25f, 3f))
                _state.value = _state.value.copy(speed = intent.speed)
            }
            is PlayerIntent.SetBand -> {
                attachEqualizer()
                equalizer?.setBandLevel(intent.index.toShort(), intent.level)
            }
        }
    }

    private fun play(track: Track, queue: List<Track>) {
        val list = queue.ifEmpty { listOf(track) }
        val items = list.map { it.toMediaItem() }
        controller?.setMediaItems(items)
        val index = list.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        controller?.seekToDefaultPosition(index)
        controller?.prepare()
        controller?.play()
        _state.value = _state.value.copy(track = track, queue = list, durationMs = track.durationMs)
        onTrackChanged(track)
    }

    private fun onTrackChanged(track: Track) {
        _state.value = _state.value.copy(
            track = track,
            durationMs = track.durationMs,
            lyrics = emptyList(),
            currentLine = -1,
            lyricsState = LyricsUiState.Idle
        )
        if (_state.value.autoLyrics) loadLyrics(track, force = false)
    }

    private fun loadLyrics(track: Track, force: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(lyricsState = LyricsUiState.Loading)
            when (val result = lyricsRepository.load(track, force)) {
                is LyricsResult.Success -> _state.value = _state.value.copy(
                    lyrics = result.lines,
                    lyricsSynced = result.synced,
                    lyricsState = LyricsUiState.Idle
                )
                LyricsResult.Instrumental -> _state.value = _state.value.copy(
                    lyrics = emptyList(),
                    lyricsState = LyricsUiState.Instrumental
                )
                LyricsResult.NotFound -> _state.value = _state.value.copy(
                    lyrics = emptyList(),
                    lyricsState = LyricsUiState.NotFound
                )
                is LyricsResult.Error -> _state.value = _state.value.copy(
                    lyrics = emptyList(),
                    lyricsState = LyricsUiState.Error(result.message)
                )
            }
        }
    }

    private fun startPositionPolling() {
        viewModelScope.launch {
            while (true) {
                val controller = controller ?: break
                val pos = controller.currentPosition
                val dur = controller.duration.takeIf { it > 0 } ?: _state.value.durationMs
                val lines = _state.value.lyrics
                var idx = -1
                if (_state.value.lyricsSynced && lines.isNotEmpty()) {
                    for (i in lines.indices) {
                        if (lines[i].timeMs <= pos) idx = i else break
                    }
                }
                _state.value = _state.value.copy(positionMs = pos, durationMs = dur, currentLine = idx)
                delay(250)
            }
        }
    }

    fun bands(): ShortArray {
        attachEqualizer()
        return equalizer?.let {
            ShortArray(it.numberOfBands.toInt()) { i -> it.getBandLevel(i.toShort()) }
        } ?: ShortArray(10)
    }

    private fun attachEqualizer() {
        if (equalizer == null) {
            try {
                equalizer = Equalizer(0, 0).apply { enabled = true }
            } catch (_: Throwable) {
                equalizer = null
            }
        }
    }

    private suspend fun awaitController(): MediaController? = suspendCancellableCoroutine { cont ->
        val future = MediaController.Builder(
            context,
            SessionToken(context, ComponentName(context, MediaSessionService::class.java))
        ).buildAsync()
        future.addListener(
            { if (cont.isActive) cont.resume(runCatching { future.get() }.getOrNull()) },
            androidx.core.content.ContextCompat.getMainExecutor(context)
        )
    }

    private fun Track.toMediaItem(): MediaItem = MediaItem.Builder()
        .setUri(uri)
        .setMediaId(id.toString())
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .build()
        )
        .build()

    override fun onCleared() {
        equalizer?.release()
        controller?.release()
        super.onCleared()
    }
}
