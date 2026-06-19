package com.wildtrail.app.domain.model

/**
 * The set of trail criteria the Explore "filter menu" collects. Kept as a
 * plain value type so the same shape flows through the whole UDF loop: the
 * ViewModel holds a *draft* (the live slider/chip values) and an *applied*
 * copy (committed by "Apply"), and the repository turns an applied [HikeFilter]
 * into a Room query.
 *
 * The defaults describe "no filtering": the full distance/elevation ranges and
 * no difficulty restriction. [DISTANCE_MAX_KM] / [ELEVATION_MAX_M] are the
 * slider bounds and double as the "unbounded" sentinel for [isActive].
 *
 * @property difficulties selected difficulty levels (1..5); empty means "any".
 * @property surfaceTypes selected surface/terrain types; empty means "any".
 */
data class HikeFilter(
    val minKm: Float = 0f,
    val maxKm: Float = DISTANCE_MAX_KM,
    val minElevation: Int = 0,
    val maxElevation: Int = ELEVATION_MAX_M,
    val difficulties: Set<Int> = emptySet(),
    val surfaceTypes: Set<SurfaceType> = emptySet(),
) {
    /**
     * Whether this filter actually narrows anything — used by the ViewModel to
     * decide between showing the filtered results and the default featured feed
     * (and to keep an at-rest filter from running a needless query).
     */
    fun isActive(): Boolean =
        minKm > 0f ||
            maxKm < DISTANCE_MAX_KM ||
            minElevation > 0 ||
            maxElevation < ELEVATION_MAX_M ||
            difficulties.isNotEmpty() ||
            surfaceTypes.isNotEmpty()

    companion object {
        const val DISTANCE_MAX_KM = 50f
        const val ELEVATION_MAX_M = 2000

        /** All selectable difficulty levels, matching the 1..5 rating scale. */
        val DIFFICULTY_LEVELS = 1..5
    }
}
