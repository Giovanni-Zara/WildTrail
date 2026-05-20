package com.wildtrail.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Top-level request body sent to POST /predict.
 *
 * The API expects exactly this JSON shape:
 * {
 *   "user": { "xp_points": 1500, "eta": 30, "past_hikes": 25, "avg_speed": 4.5 },
 *   "hike": { "lunghezza": 10.0, "elevation_gain": 400, "surface_type": "forest", "difficulty": 3 }
 * }
 */
data class PredictRequestDto(
    val user: UserFeaturesDto,
    val hike: HikeFeaturesDto,
)

data class UserFeaturesDto(
    @SerializedName("xp_points")  val xpPoints: Int,
    @SerializedName("eta")        val eta: Int,
    @SerializedName("past_hikes") val pastHikes: Int,
    @SerializedName("avg_speed")  val avgSpeed: Double,
)

data class HikeFeaturesDto(
    @SerializedName("lunghezza")       val lunghezza: Double,
    @SerializedName("elevation_gain")  val elevationGain: Int,
    @SerializedName("surface_type")    val surfaceType: String,
    @SerializedName("difficulty")      val difficulty: Int,
)
