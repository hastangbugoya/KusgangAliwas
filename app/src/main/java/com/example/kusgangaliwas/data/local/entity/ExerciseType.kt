package com.example.kusgangaliwas.data.local.entity

/**
 * High-level category for an exercise.
 *
 * Used for:
 * - filtering exercise pickers
 * - session analytics
 * - cardio vs strength handling
 * - future charting/grouping
 */
enum class ExerciseType {
    STRENGTH,
    CARDIO,
    MOBILITY,
    OTHER,
}