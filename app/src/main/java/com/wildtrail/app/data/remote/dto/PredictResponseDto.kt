package com.wildtrail.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Response body returned by POST /predict.
 *
 * The API returns: { "duration_min": 178.5, "unit": "minutes" }
 */
data class PredictResponseDto(
    @SerializedName("duration_min") val durationMin: Double,
    val unit: String,
)
