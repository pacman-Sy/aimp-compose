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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.aimp.compose.playback.PlayerIntent
import com.aimp.compose.playback.PlayerViewModel

@Composable
fun PlayerScreen(navController: NavController? = null, vm: PlayerViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = true,
                    onClick = { navController?.navigate("player") },
                    icon = { Icon(Icons.Filled.Home, null) },
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
                    label = { Text("Equalizer") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController?.navigate("settings") },
                    icon = { Icon(Icons.Filled.Settings, null) },
                    label = { Text("Settings") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = state.track?.title ?: "No track",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = state.track?.artist ?: "", color = MaterialTheme.colorScheme.onBackground)

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(240.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = { offset ->
                                if (offset.x < size.width / 2) vm.handle(PlayerIntent.Seek(state.positionMs - 10_000))
                                else vm.handle(PlayerIntent.Seek(state.positionMs + 10_000))
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { _, dragAmount ->
                            if (dragAmount > 80) vm.handle(PlayerIntent.Prev)
                            else if (dragAmount < -80) vm.handle(PlayerIntent.Next)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_stat_note),
                    contentDescription = null,
                    modifier = Modifier.size(96.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = if (state.durationMs > 0) state.positionMs.toFloat() / state.durationMs else 0f,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatTime(state.positionMs), fontSize = 11.sp)
                Text(formatTime(state.durationMs), fontSize = 11.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { vm.handle(PlayerIntent.Prev) }) { Text("|<<") }
                Button(onClick = { vm.handle(PlayerIntent.TogglePlay) }) {
                    Text(if (state.isPlaying) "Pause" else "Play")
                }
                Button(onClick = { vm.handle(PlayerIntent.Next) }) { Text(">>|") }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Speed", fontSize = 12.sp)
                Slider(
                    value = state.speed,
                    onValueChange = { vm.handle(PlayerIntent.SetSpeed(it)) },
                    valueRange = 0.25f..3f,
                    modifier = Modifier.width(160.dp)
                )
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "Lyrics",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Row {
                Button(onClick = { vm.handle(PlayerIntent.SetLyricsMode(0)) }) { Text("Synced") }
                Button(onClick = { vm.handle(PlayerIntent.SetLyricsMode(1)) }) { Text("Plain") }
                Button(onClick = { vm.handle(PlayerIntent.RefreshLyrics) }) { Text("Refresh") }
            }
            LyricsPanel(vm, state.lyrics, state.currentLine, state.lyricsSynced, state.lyricsState, state.lyricsMode)
        }
    }
}

@Composable
private fun LyricsPanel(
    vm: PlayerViewModel,
    lines: List<LyricLine>,
    currentLine: Int,
    synced: Boolean,
    lyricsState: com.aimp.compose.playback.LyricsUiState,
    mode: Int
) {
    val listState = rememberLazyListState()
    LaunchedEffect(currentLine) {
        if (synced && currentLine >= 0) listState.animateScrollToItem(currentLine)
    }

    when {
        lyricsState is com.aimp.compose.playback.LyricsUiState.Loading ->
            Text("Loading lyrics…", color = MaterialTheme.colorScheme.onBackground)
        lyricsState is com.aimp.compose.playback.LyricsUiState.NotFound ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Lyrics not found", color = MaterialTheme.colorScheme.onBackground)
                Button(onClick = { vm.handle(PlayerIntent.RefreshLyrics) }) { Text("Retry") }
            }
        lyricsState is com.aimp.compose.playback.LyricsUiState.Instrumental ->
            Text("No lyrics available (Instrumental)", color = MaterialTheme.colorScheme.onBackground)
        lyricsState is com.aimp.compose.playback.LyricsUiState.Error ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(lyricsState.message, color = Color.Red)
                Button(onClick = { vm.handle(PlayerIntent.RefreshLyrics) }) { Text("Retry") }
            }
        lines.isEmpty() -> Text("No lyrics loaded", color = MaterialTheme.colorScheme.onBackground)
        mode == 1 || !synced -> Text(
            lines.joinToString("\n") { it.text },
            color = MaterialTheme.colorScheme.onBackground
        )
        else -> LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp)
        ) {
            itemsIndexed(lines) { index, line ->
                Text(
                    text = line.text,
                    color = if (index == currentLine) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontWeight = if (index == currentLine) FontWeight.Bold else FontWeight.Normal,
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
