package com.example.kusgangaliwas.data.local.entity

/**
 * Describes where a motivational exercise goal is being shown or carried.
 *
 * Scope assignments let one goal stay portable across exercise details, split
 * roadmaps, split schedules, training cycles, and future program contexts
 * without turning the goal into a required planned load.
 */
enum class ExerciseMotivationalGoalAssignmentScopeType {
    LONG_TERM,
    SPLIT_TEMPLATE,
    SPLIT_TEMPLATE_EXERCISE,
    TRAINING_CYCLE,
    TRAINING_CYCLE_STEP,
    SPLIT_SCHEDULE,
    PROGRAM,
}