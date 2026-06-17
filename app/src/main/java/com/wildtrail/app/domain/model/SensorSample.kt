package com.wildtrail.app.domain.model

import kotlin.math.sqrt

/**
 * A single tri-axial sensor reading, decoupled from the Android framework's
 * [android.hardware.SensorEvent] (which is `final`, has no public constructor,
 * and is therefore impossible to fake in a unit test).
 *
 * Mapping to this small value type at the data-layer boundary keeps the domain
 * layer (the [com.wildtrail.app.domain.usecase.DetectFallUseCase]) and its
 * tests platform-agnostic.
 *
 *  - For the **accelerometer** the axes are in m/s²; [magnitude] divided by
 *    `g` (≈9.81) gives the force in *g*.
 *  - For the **gyroscope** the axes are in rad/s; [magnitude] is the
 *    instantaneous angular speed.
 *
 * [timestampNs] is the sensor event time in nanoseconds (the same clock as
 * [android.hardware.SensorEvent.timestamp]); the fall-detection windows are
 * measured against it so the algorithm is fully deterministic from its input.
 */
data class SensorSample(
    val x: Float,
    val y: Float,
    val z: Float,
    val timestampNs: Long,
) {
    /** Euclidean magnitude of the three axes. */
    val magnitude: Float get() = sqrt(x * x + y * y + z * z)
}
