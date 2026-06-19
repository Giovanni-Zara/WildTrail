package com.wildtrail.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Request body for POST /summarize-reviews.
 *
 * The backend reads only the review text corpus, so we send a plain list of
 * strings — nothing else about the review (author, rating, photos) leaves the
 * device for the summary:
 *
 *   { "reviews": ["review 1", "review 2", ...] }
 */
data class SummarizeReviewsRequestDto(
    val reviews: List<String>,
)

/**
 * Response body returned by POST /summarize-reviews:
 *
 *   { "summary": "Hikers loved the views…", "review_count": 12 }
 */
data class SummarizeReviewsResponseDto(
    val summary: String,
    @SerializedName("review_count") val reviewCount: Int = 0,
)
