package com.example.kusgangaliwas.domain.repository

import com.example.kusgangaliwas.data.local.entity.ExerciseMotivationalGoalAssignmentEntity
import com.example.kusgangaliwas.data.local.entity.ExerciseMotivationalGoalEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for optional motivational exercise goals.
 *
 * Motivational goals are aspirational overlays for exercises and planning
 * contexts. They can help the user remember what they hope to reach over time,
 * but they are not planned loads, pass/fail rules, or strict success metrics.
 *
 * Runtime workout suggestions should continue to come from previous
 * performance logs. Goals are only contextual targets the user may choose to
 * carry into exercises, splits, schedules, cycles, and future programs.
 */
interface ExerciseMotivationalGoalRepository {
    fun observeActiveGoalsForExercise(
        exerciseId: Long,
    ): Flow<List<ExerciseMotivationalGoalEntity>>

    suspend fun getActiveGoalsForExercise(
        exerciseId: Long,
    ): List<ExerciseMotivationalGoalEntity>

    fun observeHiddenGoalsForExercise(
        exerciseId: Long,
    ): Flow<List<ExerciseMotivationalGoalEntity>>

    suspend fun getHiddenGoalsForExercise(
        exerciseId: Long,
    ): List<ExerciseMotivationalGoalEntity>

    fun observeActiveLongTermGoalsForExercise(
        exerciseId: Long,
    ): Flow<List<ExerciseMotivationalGoalEntity>>

    suspend fun getActiveLongTermGoalsForExercise(
        exerciseId: Long,
    ): List<ExerciseMotivationalGoalEntity>

    fun observeActiveGoalsForSplitTemplateExercise(
        splitTemplateExerciseId: Long,
    ): Flow<List<ExerciseMotivationalGoalEntity>>

    suspend fun getActiveGoalsForSplitTemplateExercise(
        splitTemplateExerciseId: Long,
    ): List<ExerciseMotivationalGoalEntity>

    suspend fun getGoalById(goalId: Long): ExerciseMotivationalGoalEntity?

    suspend fun getActiveAssignmentsForGoal(
        goalId: Long,
    ): List<ExerciseMotivationalGoalAssignmentEntity>

    suspend fun insertGoal(goal: ExerciseMotivationalGoalEntity): Long

    suspend fun updateGoal(goal: ExerciseMotivationalGoalEntity)

    suspend fun deleteGoal(goal: ExerciseMotivationalGoalEntity)

    suspend fun insertAssignment(
        assignment: ExerciseMotivationalGoalAssignmentEntity,
    ): Long

    suspend fun updateAssignment(
        assignment: ExerciseMotivationalGoalAssignmentEntity,
    )

    suspend fun deleteAssignment(
        assignment: ExerciseMotivationalGoalAssignmentEntity,
    )

    suspend fun deactivateGoal(
        goalId: Long,
        updatedAtEpochMillis: Long,
    )

    suspend fun restoreGoalAndAssignments(
        goalId: Long,
        updatedAtEpochMillis: Long,
    )

    suspend fun deactivateAssignment(
        assignmentId: Long,
        updatedAtEpochMillis: Long,
    )

    suspend fun deactivateAssignmentsForGoal(
        goalId: Long,
        updatedAtEpochMillis: Long,
    )

    suspend fun insertGoalWithLongTermAssignment(
        goal: ExerciseMotivationalGoalEntity,
        assignmentCreatedAtEpochMillis: Long,
    ): Long

    suspend fun assignGoalToSplitTemplateExerciseIfMissing(
        goalId: Long,
        splitTemplateExerciseId: Long,
        createdAtEpochMillis: Long,
    ): Long
}