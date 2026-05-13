package com.wildtrail.app.util

import kotlin.math.sqrt

/**
 * XP → level conversion.
 *
 * Curve: `xpForLevel(n) = 50 * n * (n - 1)`. This produces a gentle quadratic
 * progression that doesn't feel grindy for new users but slows down for
 * power users. Hand-tabulated:
 *
 *   Level 1 →    0 XP   (start)
 *   Level 2 →  100 XP
 *   Level 3 →  300 XP
 *   Level 4 →  600 XP
 *   Level 5 → 1000 XP
 *   Level 6 → 1500 XP
 *   Level 7 → 2100 XP
 *   Level 8 → 2800 XP
 *   Level 9 → 3600 XP
 *   Level 10→ 4500 XP
 *
 * The closed-form inverse `levelForXp` lets us figure out which level a
 * given XP total is in without iterating, by solving the quadratic
 * `50*n^2 - 50*n - xp = 0`.
 */
object LevelMath {

    /** XP threshold to *reach* the given level. Level 1 starts at 0 XP. */
    fun xpForLevel(level: Int): Int = 50 * level * (level - 1)

    /** The highest level you've reached given [xp]. Always ≥ 1. */
    fun levelForXp(xp: Int): Int {
        if (xp <= 0) return 1
        // Solve: 50 * n * (n - 1) ≤ xp  →  n ≤ (1 + sqrt(1 + 0.08 * xp)) / 2
        val n = ((1.0 + sqrt(1.0 + 0.08 * xp)) / 2.0).toInt()
        return n.coerceAtLeast(1)
    }

    /** XP earned within the current level (0 .. xpForNextLevel(level)). */
    fun xpInCurrentLevel(xp: Int): Int = xp - xpForLevel(levelForXp(xp))

    /** XP needed to advance from [level] to [level]+1. */
    fun xpForNextLevel(level: Int): Int = xpForLevel(level + 1) - xpForLevel(level)

    /** Progress 0f..1f within the current level — handy for a progress bar. */
    fun progressInCurrentLevel(xp: Int): Float {
        val level = levelForXp(xp)
        val span = xpForNextLevel(level).coerceAtLeast(1)
        return (xpInCurrentLevel(xp).toFloat() / span).coerceIn(0f, 1f)
    }
}
