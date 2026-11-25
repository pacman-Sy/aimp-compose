package com.aimp.compose.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.aimp.compose.R
import com.aimp.compose.lyrics.LyricLine
import com.aimp.compose.playback.LyricsUiState
import com.aimp.compose.playback.PlayerIntent
import com.aimp.compose.playback.PlayerViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(navController: NavController? = null, vm: PlayerViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AIMP", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = { navController?.navigate("settings") }) {
                        Icon(Icons.Filled.Settings, null, tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                NavigationBarItem(
                    selected = true,
                    onClick = { navController?.navigate("player") },
                    icon = { Icon(painterResource(R.drawable.aimp_play), null) },
                    label = { Text("Player") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController?.navigate("library") },
                    icon = { Icon(Icons.Filled.LibraryMusic, null) },
                    label = { Text("Library") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController?.navigate("equalizer") },
                    icon = { Icon(Icons.Filled.Equalizer, null) },
                    label = { Text("DSP") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController?.navigate("settings") },
                    icon = { Icon(Icons.Filled.Settings, null) },
                    label = { Text("Settings") }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = state.track?.title ?: "No track",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = state.track?.let { "${it.artist} — ${it.album}" } ?: "",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                maxLines = 1
            )

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .size(260.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .pointerInput(Unit) {
                        detectTapGestures(onDoubleTap = { offset ->
                            if (offset.x < size.width / 2)
                                vm.handle(PlayerIntent.Seek(state.positionMs - 10_000))
                            else
                                vm.handle(PlayerIntent.Seek(state.positionMs + 10_000))
                        })
                    }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { _, drag ->
                            if (drag > 80) vm.handle(PlayerIntent.Prev)
                            else if (drag < -80) vm.handle(PlayerIntent.Next)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.aimp_art),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { if (state.durationMs > 0) state.positionMs.toFloat() / state.durationMs else 0f },
                modifier = Modifier.fillMaxWidth().height(3.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatTime(state.positionMs), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatTime(state.durationMs), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { vm.handle(PlayerIntent.Prev) }) {
                    Icon(painterResource(R.drawable.aimp_prev), null, tint = MaterialTheme.colorScheme.onBackground)
                }
                IconButton(onClick = { vm.handle(PlayerIntent.TogglePlay) }) {
                    Icon(
                        painter = painterResource(if (state.isPlaying) R.drawable.aimp_pause else R.drawable.aimp_play),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
                IconButton(onClick = { vm.handle(PlayerIntent.Next) }) {
                    Icon(painterResource(R.drawable.aimp_next), null, tint = MaterialTheme.colorScheme.onBackground)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Speed", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Slider(
                    value = state.speed,
                    onValueChange = { vm.handle(PlayerIntent.SetSpeed(it)) },
                    valueRange = 0.25f..3f,
                    modifier = Modifier.width(120.dp)
                )
            }

            Spacer(Modifier.height(6.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surface)
            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Lyrics", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Row {
                    TextButtonMini("SYNCED", state.lyricsMode == 0) { vm.handle(PlayerIntent.SetLyricsMode(0)) }
                    TextButtonMini("PLAIN", state.lyricsMode == 1) { vm.handle(PlayerIntent.SetLyricsMode(1)) }
                    TextButtonMini("REFRESH", false) { vm.handle(PlayerIntent.RefreshLyrics) }
                }
            }

            LyricsPanel(vm, state.lyrics, state.currentLine, state.lyricsSynced, state.lyricsState, state.lyricsMode)
        }
    }
}

@Composable
private fun TextButtonMini(label: String, active: Boolean, onClick: () -> Unit) {
    androidx.compose.material3.TextButton(onClick = onClick) {
        Text(label, color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LyricsPanel(
    vm: PlayerViewModel,
    lines: List<LyricLine>,
    currentLine: Int,
    synced: Boolean,
    lyricsState: LyricsUiState,
    mode: Int
) {
    val listState = rememberLazyListState()
    LaunchedEffect(currentLine) {
        if (synced && currentLine >= 0) listState.animateScrollToItem(currentLine)
    }

    when {
        lyricsState is LyricsUiState.Loading ->
            Text("Loading lyrics…", color = MaterialTheme.colorScheme.onSurfaceVariant)
        lyricsState is LyricsUiState.NotFound ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Lyrics not found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                androidx.compose.material3.TextButton(onClick = { vm.handle(PlayerIntent.RefreshLyrics) }) {
                    Text("Retry", color = MaterialTheme.colorScheme.primary)
                }
            }
        lyricsState is LyricsUiState.Instrumental ->
            Text("No lyrics available (Instrumental)", color = MaterialTheme.colorScheme.onSurfaceVariant)
        lyricsState is LyricsUiState.Error ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(lyricsState.message, color = MaterialTheme.colorScheme.error)
                androidx.compose.material3.TextButton(onClick = { vm.handle(PlayerIntent.RefreshLyrics) }) {
                    Text("Retry", color = MaterialTheme.colorScheme.primary)
                }
            }
        lines.isEmpty() -> Text("No lyrics loaded", color = MaterialTheme.colorScheme.onSurfaceVariant)
        mode == 1 || !synced -> Text(
            lines.joinToString("\n") { it.text },
            color = MaterialTheme.colorScheme.onBackground
        )
        else -> LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp)
        ) {
            itemsIndexed(lines) { index, line ->
                Text(
                    text = line.text,
                    color = if (index == currentLine) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (index == currentLine) FontWeight.Bold else FontWeight.Normal,
                    fontSize = if (index == currentLine) 17.sp else 14.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val total = ms / 1000
    return "%02d:%02d".format(total / 60, total % 60)
}
