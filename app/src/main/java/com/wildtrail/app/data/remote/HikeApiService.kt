package com.wildtrail.app.data.remote

import com.wildtrail.app.data.remote.dto.PredictRequestDto
import com.wildtrail.app.data.remote.dto.PredictResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit interface for the hike-prediction endpoint on our PythonAnywhere backend.
 *
 * Retrofit reads the @POST annotation and builds the actual HTTP request for us —
 * we never write socket or HTTP code manually.
 *
 * The base URL (same server as WeatherApiService) is set in AppContainer.
 */
interface HikeApiService {

    @POST("predict")
    suspend fun predict(@Body request: PredictRequestDto): PredictResponseDto
}
