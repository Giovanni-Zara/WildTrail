package com.wildtrail.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Compact, locale-aware date string for a hike's end-time.
 *
 * Renders as "12 May 2026" — short enough for the hike card subtitle but
 * unambiguous everywhere in the world (numeric-only dates flip month/day
 * order between locales, which is why we render the month name).
 *
 * Epoch-millis of `0` is treated as "unknown" and renders the empty string
 * so the caller can drop the line entirely.
 */
fun formatHikeDate(epochMillis: Long): String {
    if (epochMillis <= 0L) return ""
    val df = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return df.format(Date(epochMillis))
}
