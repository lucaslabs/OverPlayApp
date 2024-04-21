package com.example.overplay.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.C.VOLUME_FLAG_SHOW_UI
import androidx.media3.ui.PlayerView
import com.example.overplay.presentation.OverPlayViewModel.UiEvent

@Composable
fun OverPlayScreen(
    viewModel: OverPlayViewModel = hiltViewModel()
) {
    val uiEvent by viewModel.uiEvent.collectAsState()

    // TODO Use effect (launch or dispose)?
    val exoPlayer = remember { viewModel.getExoplayer() }

    when (uiEvent) {
        UiEvent.ShakeEvent ->
            exoPlayer.pause()

        is UiEvent.IncreaseDeviceVolume -> exoPlayer.increaseDeviceVolume(VOLUME_FLAG_SHOW_UI)
        is UiEvent.DecreaseDeviceVolume -> exoPlayer.decreaseDeviceVolume(VOLUME_FLAG_SHOW_UI)

        is UiEvent.SeekForward -> if (exoPlayer.isPlaying) exoPlayer.seekForward()
        is UiEvent.SeekBackward -> if (exoPlayer.isPlaying) exoPlayer.seekBack()
        else -> {}
    }


    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            PlayerView(context).apply {
                player = exoPlayer
            }
        }
    )
}