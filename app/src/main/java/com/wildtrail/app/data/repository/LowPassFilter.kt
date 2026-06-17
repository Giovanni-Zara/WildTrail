package com.wildtrail.app.data.repository

import com.wildtrail.app.domain.model.SensorSample

/**
 * Per-axis exponential-moving-average (EMA) low-pass filter.
 *
 * Raw accelerometer data is noisy: hand tremor, footfalls and sensor jitter
 * all add high-frequency content that would make a naive "magnitude > 2.5g"
 * impact test fire constantly. Smoothing the stream first lets the
 * [com.wildtrail.app.domain.usecase.DetectFallUseCase] reason about the real
 * motion envelope instead of the noise.
 *
 * The recurrence is:
 *
 * ```
 * out[n] = out[n-1] + alpha * (in[n] - out[n-1])
 *        = alpha * in[n] + (1 - alpha) * out[n-1]
 * ```
 *
 * [alpha] ∈ (0, 1]: smaller → smoother but more lag; larger → more responsive
 * but noisier. The first sample seeds the filter so there is no start-up ramp.
 *
 * The instance is **stateful** (it remembers the previous output), so each
 * sensor collection must use its own filter — [SensorRepository] creates a
 * fresh one per `accelerometerSamples()` subscription.
 */
class LowPassFilter(private val alpha: Float = DEFAULT_ALPHA) {

    private var hasPrevious = false
    private var x = 0f
    private var y = 0f
    private var z = 0f

    fun apply(sample: SensorSample): SensorSample {
        if (!hasPrevious) {
            x = sample.x
            y = sample.y
            z = sample.z
            hasPrevious = true
        } else {
            x += alpha * (sample.x - x)
            y += alpha * (sample.y - y)
            z += alpha * (sample.z - z)
        }
        return sample.copy(x = x, y = y, z = z)
    }

    companion object {
        /** Gentle smoothing — keeps fast impacts while killing jitter. */
        const val DEFAULT_ALPHA = 0.2f
    }
}
