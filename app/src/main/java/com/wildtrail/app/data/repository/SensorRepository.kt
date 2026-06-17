package com.wildtrail.app.data.repository

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.wildtrail.app.domain.model.AccelReading
import com.wildtrail.app.domain.model.SensorSample
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map

/**
 * Data-layer source for the device motion sensors used by fall detection.
 *
 * Mirrors the [com.wildtrail.app.util.LocationTracker] pattern: each public
 * function returns a **cold** [Flow] that registers a [SensorEventListener]
 * on first collection and unregisters it via [awaitClose] when the collector
 * goes away — so the sensors are only powered while a trek is recording.
 *
 * Both streams are requested at [SensorManager.SENSOR_DELAY_GAME] (~20 ms /
 * 50 Hz), fast enough to catch a sub-second impact-and-rotation fall
 * signature. The high rate relies on the `HIGH_SAMPLING_RATE_SENSORS`
 * permission declared in the manifest.
 *
 * The class is `open` (and its emitters overridable) so unit tests can
 * substitute deterministic synthetic flows — the same fake-by-subclassing
 * approach the project uses for [WeatherRepository].
 */
open class SensorRepository(private val sensorManager: SensorManager?) {

    /**
     * Accelerometer stream where each item carries **both** the raw and the
     * [LowPassFilter]-smoothed magnitude (see [AccelReading] for why). A fresh
     * filter is created per subscription because it is stateful.
     */
    open fun accelerometerSamples(): Flow<AccelReading> {
        val filter = LowPassFilter()
        return sensorSamples(Sensor.TYPE_ACCELEROMETER).map { raw ->
            AccelReading(
                rawMagnitude = raw.magnitude,
                smoothedMagnitude = filter.apply(raw).magnitude,
                timestampNs = raw.timestampNs,
            )
        }
    }

    /** Raw gyroscope stream (angular velocity, rad/s). No filtering. */
    open fun gyroscopeSamples(): Flow<SensorSample> =
        sensorSamples(Sensor.TYPE_GYROSCOPE)

    private fun sensorSamples(sensorType: Int): Flow<SensorSample> = callbackFlow {
        val manager = sensorManager
        val sensor = manager?.getDefaultSensor(sensorType)
        if (manager == null || sensor == null) {
            // Sensor unavailable on this hardware (or no SensorManager in a
            // test/headless context): complete empty so collectors simply
            // never get a reading instead of crashing.
            close()
            return@callbackFlow
        }
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                trySend(
                    SensorSample(
                        x = event.values[0],
                        y = event.values[1],
                        z = event.values[2],
                        timestampNs = event.timestamp,
                    ),
                )
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
        awaitClose { manager.unregisterListener(listener) }
    }
}
