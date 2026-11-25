package com.aimp.compose.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aimp.compose.playback.PlayerIntent
import com.aimp.compose.playback.PlayerViewModel

@Composable
fun EqualizerScreen(vm: PlayerViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    val levels = remember { mutableStateOf(vm.bands()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Equalizer", color = MaterialTheme.colorScheme.primary)
        val current = levels.value
        for (i in current.indices) {
            Text("Band ${i + 1}", color = MaterialTheme.colorScheme.onBackground)
            Slider(
                value = current[i].toFloat(),
                onValueChange = { value ->
                    val arr = levels.value.copyOf()
                    arr[i] = value.toInt().toShort()
                    levels.value = arr
                    vm.handle(PlayerIntent.SetBand(i, arr[i]))
                },
                valueRange = -1500f..1500f,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
