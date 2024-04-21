package com.example.overplay

import androidx.lifecycle.ViewModel
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OverPlayViewModel @Inject constructor(
    private val exoPlayer: ExoPlayer
) : ViewModel() {

    fun getExoplayer() = exoPlayer
}