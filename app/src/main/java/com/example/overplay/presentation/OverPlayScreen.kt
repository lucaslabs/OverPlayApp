package com.example.overplay.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.C.VOLUME_FLAG_SHOW_UI
import androidx.media3.ui.PlayerView
import com.example.overplay.presentation.OverPlayViewModel.UiEvent
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OverPlayScreen(
    viewModel: OverPlayViewModel = hiltViewModel()
) {
    val uiEvent by viewModel.uiEvent.collectAsState()

    val exoPlayer = remember { viewModel.getExoplayer() }

    when (uiEvent) {
        UiEvent.ShakeEvent -> exoPlayer.pause()

        UiEvent.IncreaseDeviceVolume -> exoPlayer.increaseDeviceVolume(VOLUME_FLAG_SHOW_UI)
        UiEvent.DecreaseDeviceVolume -> exoPlayer.decreaseDeviceVolume(VOLUME_FLAG_SHOW_UI)

        UiEvent.SeekForward -> if (exoPlayer.isPlaying) exoPlayer.seekForward()
        UiEvent.SeekBackward -> if (exoPlayer.isPlaying) exoPlayer.seekBack()

        UiEvent.LocationChanged -> if (exoPlayer.isPlaying) {
            exoPlayer.apply {
                seekTo(0)
                playWhenReady = true
            }
        }

        else -> {}
    }

//    val locationPermissions = rememberMultiplePermissionsState(
//        permissions = listOf(
//            android.Manifest.permission.ACCESS_COARSE_LOCATION,
//            android.Manifest.permission.ACCESS_FINE_LOCATION
//        )
//    )
//
//    LaunchedEffect(true) {
//        locationPermissions.launchMultiplePermissionRequest()
//    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    player = exoPlayer
                }
            }
        )

        if (viewModel.showDebugEvent()) {
            Text(
                modifier = Modifier.background(Color.Red),
                text = "Event ${uiEvent.toString()}",
            )
        }
    }
}