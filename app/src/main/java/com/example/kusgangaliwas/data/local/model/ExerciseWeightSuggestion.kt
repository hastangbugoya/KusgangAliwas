package com.example.kusgangaliwas.data.local.model

/**
 * Lightweight read model for suggesting a starting weight for an exercise.
 *
 * This is not persisted directly. It is produced from actual session history.
 *
 * V1 rule:
 * - Find the latest previous session containing the exercise.
 * - Suggest the maximum logged weight from that session.
 * - Reps come from the same set that produced the max weight.
 *
 * Example:
 * 100 x 10
 * 120 x 8
 * 110 x 12
 *
 * Suggested:
 * 120 x 8
 *
 * Weight unit is intentionally not stored here yet because actual set logs
 * currently store weight as a raw number without a per-set unit column.
 *
 * Later this can evolve to include:
 * - previous 3 sessions
 * - user-selectable suggestion source
 * - anomaly handling
 * - estimated working weight / estimated 1RM
 * - explicit weight unit support
 */
data class ExerciseWeightSuggestion(
    val exerciseId: Long,
    val exerciseName: String?,
    val sourceActualSessionId: Long,
    val sourcePerformedDateEpochDay: Long,
    val suggestedWeight: Double,
    val suggestedReps: Int?,
)