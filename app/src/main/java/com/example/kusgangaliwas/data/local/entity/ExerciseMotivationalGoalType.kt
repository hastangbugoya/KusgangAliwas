package com.example.kusgangaliwas.data.local.entity

/**
 * Describes the kind of aspirational exercise target a user wants to keep in view.
 *
 * Motivational goals are contextual reminders only. They are not pass/fail metrics
 * and should not be used to mark a workout, split, cycle, or program as failed.
 */
enum class ExerciseMotivationalGoalType {
    WEIGHT_REPS,
    ESTIMATED_1RM,
    ACTUAL_1RM,
    CARDIO_DISTANCE,
    CARDIO_DURATION,
    CARDIO_DISTANCE_DURATION,
}