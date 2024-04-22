package com.example.overplay.presentation

import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.media3.exoplayer.ExoPlayer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.sqrt

@SuppressLint("MissingPermission")
@HiltViewModel
class OverPlayViewModel @Inject constructor(
    private val exoPlayer: ExoPlayer,
    private val sensorManager: SensorManager,
    fusedLocationProviderClient: FusedLocationProviderClient
) : ViewModel(), SensorEventListener {

    companion object {
        private const val MIN_TIME_BETWEEN_EVENTS_MILLISECS = 1000L
        private const val SHAKE_THRESHOLD = 3.25f
        private const val X_AXIS_THRESHOLD = 0.5f
        private const val Z_AXIS_THRESHOLD = 0.5f
        private const val VOLUME_DELTA = 0.1f
        private const val LOCATION_INTERVAL_UPDATE_SECS = 10L
        private const val LOCATION_THRESHOLD_METERS = 10f // meters
    }

    private var lastEventTime: Long = 0
    private var currentLocation: Location? = null
    private var lastLocation: Location? = null

    var uiEvent = MutableStateFlow<UiEvent?>(null)
        private set

    init {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        // For shake event
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)

        // For volume and seek video events
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL)

        // For location changed event
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            TimeUnit.SECONDS.toMillis(LOCATION_INTERVAL_UPDATE_SECS)
        ).build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                for (location in result.locations) {
                    if (lastLocation == null) lastLocation = location
                    currentLocation = location
                    currentLocation?.let { current ->
                        val distance: Float? = lastLocation?.distanceTo(current)
                        if (distance != null && distance > LOCATION_THRESHOLD_METERS) {
                            uiEvent.value = UiEvent.LocationChanged
                        }
                    }
                }
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun getExoplayer() = exoPlayer

    fun showDebugEvent() = true

    override fun onSensorChanged(event: SensorEvent?) {
        val currentTimeMillis = System.currentTimeMillis()

        when (event?.sensor?.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                if ((currentTimeMillis - lastEventTime) > MIN_TIME_BETWEEN_EVENTS_MILLISECS) {
                    if (isShaking(
                            xAxis = event.values[0],
                            yAxis = event.values[1],
                            zAxis = event.values[2]
                        )
                    ) {
                        lastEventTime = currentTimeMillis

                        uiEvent.value = UiEvent.ShakeEvent
                    }
                }
            }

            Sensor.TYPE_GYROSCOPE -> {
                if ((currentTimeMillis - lastEventTime) > MIN_TIME_BETWEEN_EVENTS_MILLISECS) {
                    lastEventTime = currentTimeMillis

                    val xAxis = event.values[0]
                    if (xAxis > X_AXIS_THRESHOLD) {
                        uiEvent.value = UiEvent.IncreaseDeviceVolume()
                        return
                    }

                    if (xAxis < -X_AXIS_THRESHOLD) {
                        uiEvent.value = UiEvent.DecreaseDeviceVolume()
                        return
                    }

                    val zAxis = event.values[2]
                    if (zAxis > Z_AXIS_THRESHOLD) {
                        uiEvent.value = UiEvent.SeekBackward
                        return
                    }

                    if (zAxis < -Z_AXIS_THRESHOLD) {
                        uiEvent.value = UiEvent.SeekForward
                        return
                    }
                }
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
        data class IncreaseDeviceVolume(val delta: Float = VOLUME_DELTA) : UiEvent()
        data class DecreaseDeviceVolume(val delta: Float = VOLUME_DELTA) : UiEvent()
        data object SeekForward : UiEvent()
        data object SeekBackward : UiEvent()
        data object LocationChanged : UiEvent()
    }
}