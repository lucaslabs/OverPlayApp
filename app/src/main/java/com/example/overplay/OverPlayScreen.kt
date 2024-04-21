package com.example.overplay

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.ui.PlayerView

@Composable
fun OverPlayScreen(
    viewModel: OverPlayViewModel = hiltViewModel()
) {

    // TODO Use effect (launch or dispose)?
    val exoPlayer = remember {
        viewModel.getExoplayer().apply {
            setMediaItem(MediaItem.fromUri("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4 "))
            prepare()
            playWhenReady = true
        }
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