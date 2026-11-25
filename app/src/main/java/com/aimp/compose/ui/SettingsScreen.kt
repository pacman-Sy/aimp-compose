package com.aimp.compose.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aimp.compose.playback.PlayerIntent
import com.aimp.compose.playback.PlayerViewModel

@Composable
fun SettingsScreen(vm: PlayerViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Settings", color = MaterialTheme.colorScheme.primary)
        SettingToggle("Auto lyrics (LRCLib)", state.autoLyrics) {
            vm.handle(PlayerIntent.SetAutoLyrics(it))
        }
        SettingToggle("Dark theme", state.darkTheme) {
            vm.handle(PlayerIntent.SetDarkTheme(it))
        }
        SettingToggle("Synced lyrics mode", state.lyricsMode == 0) {
            vm.handle(PlayerIntent.SetLyricsMode(if (it) 0 else 1))
        }
    }
}

@Composable
private fun SettingToggle(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onBackground)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
