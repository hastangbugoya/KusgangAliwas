package com.example.kusgangaliwas.data.local.model

/**
 * Suggested cardio values derived from the user's most recent matching cardio log.
 *
 * This is intentionally a suggestion, not a prescription:
 * - planned sessions stay lightweight
 * - actual logs remain editable
 * - the user can accept, change, or ignore the values
 */
data class CardioSuggestion(
    val exerciseId: Long,
    val exerciseName: String? = null,
    val sourceActualSessionId: Long,
    val sourcePerformedDateEpochDay: Long,
    val distance: Double?,
    val distanceUnit: String?,
    val durationSeconds: Long?,
    val averageInclinePercent: Double?,
    val averageResistance: Double?,
)