package com.example.overplay.presentation

import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.media3.exoplayer.ExoPlayer
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import kotlin.math.sqrt

@SuppressLint("MissingPermission")
@HiltViewModel
class OverPlayViewModel @Inject constructor(
    private val exoPlayer: ExoPlayer,
    private val sensorManager: SensorManager,
    private val fusedLocationProviderClient: FusedLocationProviderClient
) : ViewModel(), SensorEventListener {

    companion object {
        private const val SHAKE_THRESHOLD = 3.25f
        private const val MIN_TIME_BETWEEN_SHAKES_MILLISECS = 1000
        private const val LOCATION_INTERVAL_UPDATE = 10000 // 10 secs
    }

    private var lastShakeTime: Long = 0
    private var currentLocation: Location? = null
    private var lastLocation: Location? = null

    var uiEvent = MutableStateFlow<UiEvent?>(null)
        private set

    init {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        // For shake event
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)

//        // For volume and seek video events
//        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL)
//
//        // For location changed event
//        val locationRequest = LocationRequest.Builder(
//            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
//            TimeUnit.SECONDS.toMillis(10)
//        ).build()
//
//        val locationCallback = object : LocationCallback() {
//            override fun onLocationResult(result: LocationResult) {
//                super.onLocationResult(result)
//                for (location in result.locations) {
//                    if (lastLocation == null) lastLocation = location
//                    currentLocation = location
//                    currentLocation?.let { current ->
//                        val distance: Float? = lastLocation?.distanceTo(current)
//                        if (distance != null && distance > 10.0) {
//                            uiEvent.value = UiEvent.LocationChanged
//                        }
//                    }
//                }
//            }
//        }
//
//        fusedLocationProviderClient.requestLocationUpdates(
//            locationRequest,
//            locationCallback,
//            Looper.getMainLooper()
//        )
    }

    fun getExoplayer() = exoPlayer

    fun showDebugEvent() = true

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val currentTimeMillis = System.currentTimeMillis()
                if ((currentTimeMillis - lastShakeTime) > MIN_TIME_BETWEEN_SHAKES_MILLISECS) {
                    if (isShaking(
                            xAxis = event.values[0],
                            yAxis = event.values[1],
                            zAxis = event.values[2]
                        )
                    ) {
                        lastShakeTime = currentTimeMillis
                        uiEvent.value = UiEvent.ShakeEvent
                    }
                }
            }

            Sensor.TYPE_GYROSCOPE -> {
                val xAxis = event.values[0]
                if (xAxis > 1.0f) uiEvent.value = UiEvent.IncreaseDeviceVolume
                if (xAxis < 1.0f) uiEvent.value = UiEvent.DecreaseDeviceVolume

                val zAxis = event.values[2]
                if (zAxis < 1.0f) uiEvent.value = UiEvent.SeekForward
                if (zAxis > 1.0f) uiEvent.value = UiEvent.SeekBackward
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun isShaking(xAxis: Float, yAxis: Float, zAxis: Float): Boolean {
        val acceleration =
            sqrt(xAxis * xAxis + yAxis * yAxis + zAxis * zAxis) - SensorManager.GRAVITY_EARTH
        return acceleration > SHAKE_THRESHOLD
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
        data object LocationChanged : UiEvent()
    }
}