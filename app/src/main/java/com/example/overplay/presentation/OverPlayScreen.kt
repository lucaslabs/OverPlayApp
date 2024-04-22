package com.example.overplay.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.ui.PlayerView
import com.example.overplay.presentation.OverPlayViewModel.UiEvent
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OverPlayScreen(
    viewModel: OverPlayViewModel = hiltViewModel()
) {
    val uiEvent by viewModel.uiEvent.collectAsState()

    val exoPlayer = remember { viewModel.getExoplayer() }

    val lifecycleOwner by rememberUpdatedState(LocalLifecycleOwner.current)

    // Handle lifecycle events
    DisposableEffect(lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    exoPlayer.apply {
                        prepare()
                        playWhenReady = true
                    }
                }

                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer.playWhenReady = false
                }

                Lifecycle.Event.ON_DESTROY -> {
                    exoPlayer.apply {
                        stop()
                        release()
                    }
                }

                else -> Unit
            }
        }

        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    // Handle shake, rotation, and location events
    if (exoPlayer.isPlaying) {
        when (val event = uiEvent) {
            UiEvent.ShakeEvent -> exoPlayer.pause()

            is UiEvent.IncreaseDeviceVolume -> exoPlayer.volume += event.delta
            is UiEvent.DecreaseDeviceVolume -> exoPlayer.volume -= event.delta

            UiEvent.SeekForward -> exoPlayer.seekForward()
            UiEvent.SeekBackward -> exoPlayer.seekBack()

            UiEvent.LocationChanged ->
                exoPlayer.apply {
                    seekTo(0)
                    playWhenReady = true
                }

            else -> Unit
        }
    }

    // Handle location permission request
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    LaunchedEffect(true) {
        locationPermissions.launchMultiplePermissionRequest()
    }

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