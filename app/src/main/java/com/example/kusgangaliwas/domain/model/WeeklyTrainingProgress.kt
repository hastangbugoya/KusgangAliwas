package com.example.kusgangaliwas.domain.model

data class WeeklyTrainingProgress(
    val weekStartEpochDay: Long,
    val days: List<WeeklyTrainingDayProgress>,
)

data class WeeklyTrainingDayProgress(
    val epochDay: Long,

    /**
     * Sum of:
     * weight × reps
     */
    val strengthVolume: Double,

    /**
     * Sum of logged cardio distance.
     */
    val cardioDistance: Double,

    /**
     * Number of completed sets.
     */
    val completedSets: Int,

    /**
     * Number of cardio logs with actual entered data.
     */
    val completedCardioEntries: Int,
)
