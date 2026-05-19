package com.example.kusgangaliwas.data.local.model

/**
 * Lightweight read-model projection for split picker/list rows.
 *
 * This intentionally keeps split template entities clean while giving UI
 * enough summary data to show useful context before a user selects a split.
 */
data class SplitTemplateSummaryRow(
    val splitTemplateId: Long,
    val splitName: String,

    /**
     * Already formatted/sorted display text.
     *
     * Example:
     * "Chest, Shoulders, Triceps"
     */
    val muscleGroupsText: String,

    val strengthExerciseCount: Int,
    val cardioExerciseCount: Int,
) {

    val totalExerciseCount: Int
        get() = strengthExerciseCount + cardioExerciseCount
}