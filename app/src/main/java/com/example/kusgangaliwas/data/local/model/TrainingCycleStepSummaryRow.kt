package com.example.kusgangaliwas.data.local.model

/**
 * Lightweight read-model projection for compact cycle step UI rows.
 *
 * This intentionally avoids bloating TrainingCycleStepEntity with
 * presentation-specific aggregation data.
 */
data class TrainingCycleStepSummaryRow(
    val stepId: Long,
    val cycleId: Long,
    val splitTemplateId: Long,
    val splitName: String,
    val stepOrder: Int,
    val warnBeforeMarkDone: Boolean,

    /**
     * Already formatted/sorted display text.
     *
     * Example:
     * "Chest, Shoulders, Triceps"
     */
    val muscleGroupsText: String,

    /**
     * Number of strength exercises in this split.
     */
    val strengthExerciseCount: Int,

    /**
     * Number of cardio exercises in this split.
     */
    val cardioExerciseCount: Int,
) {

    val totalExerciseCount: Int
        get() = strengthExerciseCount + cardioExerciseCount
}