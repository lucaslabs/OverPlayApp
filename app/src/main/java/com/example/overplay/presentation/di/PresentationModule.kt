package com.example.overplay.presentation.di

import android.content.Context
import android.hardware.SensorManager
import androidx.media3.exoplayer.ExoPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PresentationModule {

    @Provides
    @Singleton
    fun provideExoPlayer(@ApplicationContext context: Context) =
        ExoPlayer.Builder(context).build()

    @Provides
    @Singleton
    fun provideSensorManager(@ApplicationContext context: Context) =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
}