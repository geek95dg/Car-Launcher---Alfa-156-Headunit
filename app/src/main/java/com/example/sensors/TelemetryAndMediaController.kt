package com.example.sensors

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioManager
import android.os.Bundle
import android.os.SystemClock
import android.view.KeyEvent
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class TelemetryAndMediaController(context: Context) {
    private val appContext = context.applicationContext
    private val locationManager =
        appContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    private val sensorManager =
        appContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val audioManager =
        appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private var locationListener: LocationListener? = null
    private var sensorListener: SensorEventListener? = null

    @SuppressLint("MissingPermission")
    fun startRealGpsTracking(
        onLocationUpdate: (speedKmh: Int, bearingDeg: Float?, altitudeM: Int) -> Unit
    ): Boolean {
        val lm = locationManager ?: return false
        stopRealGpsTracking()

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val speedMetersPerSec = if (location.hasSpeed()) location.speed else 0f
                val speedKmh = (speedMetersPerSec * 3.6f).roundToInt().coerceAtLeast(0)
                val bearing = if (location.hasBearing()) location.bearing else null
                val altitude = if (location.hasAltitude()) location.altitude.roundToInt() else 210
                onLocationUpdate(speedKmh, bearing, altitude)
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
            override fun onProviderEnabled(provider: String) = Unit
            override fun onProviderDisabled(provider: String) = Unit
        }

        return runCatching {
            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER
            )
            var registered = false
            for (provider in providers) {
                if (lm.isProviderEnabled(provider)) {
                    lm.requestLocationUpdates(provider, 1000L, 0.5f, listener)
                    registered = true
                }
            }
            locationListener = listener
            registered
        }.getOrDefault(false)
    }

    fun stopRealGpsTracking() {
        val lm = locationManager ?: return
        locationListener?.let {
            runCatching { lm.removeUpdates(it) }
        }
        locationListener = null
    }

    fun startSensorTracking(
        onSensorUpdate: (lateralG: Float, longitudinalG: Float, azimuthDeg: Float?) -> Unit
    ) {
        val sm = sensorManager ?: return
        stopSensorTracking()

        val accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)
        var hasGravity = false
        var hasMagnetic = false

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        System.arraycopy(event.values, 0, gravity, 0, 3)
                        hasGravity = true
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                        hasMagnetic = true
                    }
                }

                val latG = (gravity[0] / 9.81f).coerceIn(-1.5f, 1.5f)
                val lonG = (gravity[1] / 9.81f).coerceIn(-1.5f, 1.5f)

                var azimuth: Float? = null
                if (hasGravity && hasMagnetic) {
                    val r = FloatArray(9)
                    val i = FloatArray(9)
                    if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(r, orientation)
                        val deg = ((Math.toDegrees(orientation[0].toDouble()).toFloat() + 360f) % 360f)
                        azimuth = deg
                    }
                }
                onSensorUpdate(latG, lonG, azimuth)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        accelerometer?.let {
            sm.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.let {
            sm.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }
        sensorListener = listener
    }

    fun stopSensorTracking() {
        val sm = sensorManager ?: return
        sensorListener?.let { sm.unregisterListener(it) }
        sensorListener = null
    }

    fun getVolumePercent(): Int {
        val am = audioManager ?: return 65
        val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val current = am.getStreamVolume(AudioManager.STREAM_MUSIC)
        return ((current.toFloat() / max.toFloat()) * 100f).roundToInt().coerceIn(0, 100)
    }

    fun adjustVolumeByDeltaPercent(deltaPercent: Int): Int {
        val am = audioManager ?: return 65
        val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val current = am.getStreamVolume(AudioManager.STREAM_MUSIC)
        val currentPct = ((current.toFloat() / max.toFloat()) * 100f).roundToInt()
        val targetPct = (currentPct + deltaPercent).coerceIn(0, 100)
        val targetIndex = ((targetPct / 100f) * max).roundToInt().coerceIn(0, max)
        runCatching {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, targetIndex, 0)
        }
        return targetPct
    }

    fun dispatchMediaKey(keyCode: Int) {
        val am = audioManager ?: return
        val eventTime = SystemClock.uptimeMillis()
        runCatching {
            val downEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0)
            val upEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, keyCode, 0)
            am.dispatchMediaKeyEvent(downEvent)
            am.dispatchMediaKeyEvent(upEvent)
        }
    }

    companion object {
        fun cardinalFromDegrees(degrees: Float): String {
            val normalized = ((degrees % 360f) + 360f) % 360f
            val directions = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
            val index = ((normalized + 22.5f) / 45f).toInt() % 8
            return directions[index]
        }

        fun computeDemoTelemetryStep(
            tick: Long,
            throttleMultiplier: Float
        ): DemoTelemetryFrame {
            val t = tick * 0.09f
            val baseWave = (sin(t) * 0.5f + 0.5f)
            val secondaryWave = (sin(t * 0.37f + 1.2f) * 0.5f + 0.5f)
            val rawSpeed = (38f + (baseWave * 95f + secondaryWave * 42f) * throttleMultiplier)
                .roundToInt()
                .coerceIn(0, 260)

            val gearNum = when {
                rawSpeed < 18 -> 1
                rawSpeed < 42 -> 2
                rawSpeed < 68 -> 3
                rawSpeed < 96 -> 4
                rawSpeed < 128 -> 5
                rawSpeed < 162 -> 6
                rawSpeed < 198 -> 7
                else -> 8
            }
            val gearFloor = listOf(0, 0, 18, 42, 68, 96, 128, 162, 198)[gearNum]
            val gearCeiling = listOf(18, 18, 42, 68, 96, 128, 162, 198, 265)[gearNum]
            val gearRatio = ((rawSpeed - gearFloor).toFloat() / (gearCeiling - gearFloor).coerceAtLeast(1))
                .coerceIn(0f, 1f)

            val rpm = (1650 + (gearRatio * 4850f * throttleMultiplier).roundToInt()).coerceIn(900, 7200)
            val turboBar = ((rpm - 1500).coerceAtLeast(0) / 5500f * 1.55f * throttleMultiplier)
                .coerceIn(0f, 1.8f)

            val latG = (sin(t * 0.65f) * 0.58f * throttleMultiplier).coerceIn(-1.2f, 1.2f)
            val lonG = (cos(t * 0.8f) * 0.44f * throttleMultiplier).coerceIn(-1.2f, 1.2f)
            val bearing = ((45f + tick * 0.7f) % 360f)

            return DemoTelemetryFrame(
                speedKmh = rawSpeed,
                rpm = rpm,
                gear = "D$gearNum",
                turboBar = (turboBar * 100f).roundToInt() / 100f,
                lateralG = (latG * 100f).roundToInt() / 100f,
                longitudinalG = (lonG * 100f).roundToInt() / 100f,
                bearingDeg = bearing,
                cardinal = cardinalFromDegrees(bearing),
                streetStatus = when ((tick / 60) % 3) {
                    0L -> "Autostrada A2 • Odcinek Sportowy"
                    1L -> "Droga ekspresowa S8 • Płynny ruch"
                    else -> "Przełęcz Stelvio • Sekcja Dynamiczna"
                }
            )
        }
    }
}

data class DemoTelemetryFrame(
    val speedKmh: Int,
    val rpm: Int,
    val gear: String,
    val turboBar: Float,
    val lateralG: Float,
    val longitudinalG: Float,
    val bearingDeg: Float,
    val cardinal: String,
    val streetStatus: String
)
