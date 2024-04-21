package com.example.overplay.presentation

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import kotlin.math.sqrt

@HiltViewModel
class OverPlayViewModel @Inject constructor(
    private val exoPlayer: ExoPlayer,
    private val sensorManager: SensorManager
) : ViewModel(), SensorEventListener {

    init {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private var acceleration = 10f
    private var currentAcceleration = SensorManager.GRAVITY_EARTH
    private var lastAcceleration = SensorManager.GRAVITY_EARTH

    var uiEvent = MutableStateFlow<UiEvent?>(null)
        private set

    companion object {
        private const val ACCELERATION_FACTOR = 0.9f
        private const val ACCELERATION_THRESHOLD = 12
    }

    fun getExoplayer() = exoPlayer

    fun getVideoUrl() =
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4 "

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_ACCELEROMETER -> if (isShakeEvent(
                    xAxis = event.values[0],
                    yAxis = event.values[1],
                    zAxis = event.values[2]
                )
            ) {
                uiEvent.value = UiEvent.ShakeEvent
            }

            Sensor.TYPE_GYROSCOPE -> {
                val xAxis = event.values[0]
                if (xAxis > 0) uiEvent.value = UiEvent.IncreaseDeviceVolume
                if (xAxis < 0) uiEvent.value = UiEvent.DecreaseDeviceVolume

                val zAxis = event.values[2]
                if (zAxis < 0) uiEvent.value = UiEvent.SeekForward
                if (zAxis > 0) uiEvent.value = UiEvent.SeekBackward
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun isShakeEvent(xAxis: Float, yAxis: Float, zAxis: Float): Boolean {
        lastAcceleration = currentAcceleration
        currentAcceleration = sqrt(xAxis * xAxis + yAxis * yAxis + zAxis * zAxis)
        val delta = currentAcceleration - lastAcceleration
        acceleration = acceleration * ACCELERATION_FACTOR + delta
        return acceleration > ACCELERATION_THRESHOLD
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
    }

    sealed class UiEvent {
        data object ShakeEvent : UiEvent()
        data object IncreaseDeviceVolume : UiEvent()
        data object DecreaseDeviceVolume : UiEvent()
        data object SeekForward : UiEvent()
        data object SeekBackward : UiEvent()
    }
}