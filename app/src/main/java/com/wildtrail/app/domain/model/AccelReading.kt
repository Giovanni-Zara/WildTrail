package com.wildtrail.app.domain.model

/**
 * A processed accelerometer reading that carries **both** the raw magnitude and
 * the low-pass-filtered magnitude for the same instant.
 *
 * Why both? A fall *impact* is a sharp, brief spike — exactly the high-frequency
 * content a low-pass filter is designed to remove — so testing the impact on
 * the smoothed signal would attenuate (and often miss) real falls. The
 * *stillness* check, on the other hand, benefits from smoothing because it
 * should ignore jitter. So [SensorRepository][com.wildtrail.app.data.repository.SensorRepository]
 * hands the domain layer both views of each sample and
 * [DetectFallUseCase][com.wildtrail.app.domain.usecase.DetectFallUseCase]
 * picks the right one per detection phase: **raw** for impact, **smoothed**
 * for stillness.
 *
 * Magnitudes are in m/s² (divide by ~9.81 for g).
 */
data class AccelReading(
    val rawMagnitude: Float,
    val smoothedMagnitude: Float,
    val timestampNs: Long,
)
