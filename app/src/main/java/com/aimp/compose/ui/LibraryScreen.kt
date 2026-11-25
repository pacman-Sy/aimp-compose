package com.aimp.compose.ui

import android.Manifest
import android.content.ContentUris
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.aimp.compose.data.Track
import com.aimp.compose.playback.PlayerIntent
import com.aimp.compose.playback.PlayerViewModel

@Composable
fun LibraryScreen(navController: NavController? = null, vm: PlayerViewModel = hiltViewModel()) {
    val context = LocalContext.current
    var tracks by remember { mutableStateOf<List<Track>>(emptyList()) }

    fun scan() {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION
        )
        val result = mutableListOf<Track>()
        context.contentResolver.query(uri, projection, "${MediaStore.Audio.Media.IS_MUSIC}=1", null, null)?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                result.add(Track(
                    id = id,
                    title = c.getString(titleCol) ?: "Unknown",
                    artist = c.getString(artistCol) ?: "Unknown",
                    album = c.getString(albumCol) ?: "",
                    durationMs = c.getLong(durCol),
                    uri = ContentUris.withAppendedId(uri, id)
                ))
            }
        }
        tracks = result
    }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) scan()
    }

    LaunchedEffect(Unit) {
        val perm = if (android.os.Build.VERSION.SDK_INT >= 33)
            Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE
        permission.launch(perm)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = { scan() }) { Text("Scan library") }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(tracks) { track ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { vm.handle(PlayerIntent.Play(track, tracks)) }
                        .padding(12.dp)
                ) {
                    Text(track.title, color = MaterialTheme.colorScheme.primary)
                    Text("${track.artist} — ${track.album}", color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
    }
}
