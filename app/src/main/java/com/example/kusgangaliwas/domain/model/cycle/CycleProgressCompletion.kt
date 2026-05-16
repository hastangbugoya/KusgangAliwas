package com.example.kusgangaliwas.domain.model.cycle

/**
 * Unified representation of cycle progress.
 *
 * A completion can come from:
 * - an actual completed workout session
 * - a non-session progress event (mark done)
 *
 * Cycle progression is intentionally day-based.
 */
data class CycleProgressCompletion(
    val trainingCycleStepId: Long,
    val epochDay: Long,
    val sourceType: String,
    val sourceId: Long,
)
