package com.aimp.compose

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aimp.compose.playback.PlayerViewModel
import com.aimp.compose.ui.AimpTheme
import com.aimp.compose.ui.EqualizerScreen
import com.aimp.compose.ui.LibraryScreen
import com.aimp.compose.ui.PlayerScreen
import com.aimp.compose.ui.SettingsScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val requestPermission = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }

        val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE
        requestPermission.launch(audioPermission)

        setContent {
            val navController = rememberNavController()
            val playerVm: PlayerViewModel = hiltViewModel()
            val uiState by playerVm.state.collectAsState()
            AimpTheme(darkTheme = uiState.darkTheme) {
                NavHost(navController = navController, startDestination = "player") {
                    composable("player") { PlayerScreen(navController) }
                    composable("library") { LibraryScreen(navController) }
                    composable("equalizer") { EqualizerScreen() }
                    composable("settings") { SettingsScreen() }
                }
            }
        }
    }
}
