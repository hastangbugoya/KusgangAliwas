package com.example.kusgangaliwas.data.repository

import com.example.kusgangaliwas.data.local.dao.ExerciseMotivationalGoalDao
import com.example.kusgangaliwas.data.local.entity.ExerciseMotivationalGoalAssignmentEntity
import com.example.kusgangaliwas.data.local.entity.ExerciseMotivationalGoalEntity
import com.example.kusgangaliwas.domain.repository.ExerciseMotivationalGoalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room-backed repository for optional motivational exercise goals.
 *
 * This implementation keeps motivational goals as contextual overlays only.
 * Runtime workout suggestions should still come from previous performance logs,
 * not from these aspirational goal records.
 */
@Singleton
class ExerciseMotivationalGoalRepositoryImpl @Inject constructor(
    private val exerciseMotivationalGoalDao: ExerciseMotivationalGoalDao,
) : ExerciseMotivationalGoalRepository {

    override fun observeActiveGoalsForExercise(
        exerciseId: Long,
    ): Flow<List<ExerciseMotivationalGoalEntity>> {
        return exerciseMotivationalGoalDao.observeActiveGoalsForExercise(exerciseId)
    }

    override suspend fun getActiveGoalsForExercise(
        exerciseId: Long,
    ): List<ExerciseMotivationalGoalEntity> {
        return exerciseMotivationalGoalDao.getActiveGoalsForExercise(exerciseId)
    }

    override fun observeHiddenGoalsForExercise(
        exerciseId: Long,
    ): Flow<List<ExerciseMotivationalGoalEntity>> {
        return exerciseMotivationalGoalDao.observeHiddenGoalsForExercise(exerciseId)
    }

    override suspend fun getHiddenGoalsForExercise(
        exerciseId: Long,
    ): List<ExerciseMotivationalGoalEntity> {
        return exerciseMotivationalGoalDao.getHiddenGoalsForExercise(exerciseId)
    }

    override fun observeActiveLongTermGoalsForExercise(
        exerciseId: Long,
    ): Flow<List<ExerciseMotivationalGoalEntity>> {
        return exerciseMotivationalGoalDao.observeActiveLongTermGoalsForExercise(exerciseId)
    }

    override suspend fun getActiveLongTermGoalsForExercise(
        exerciseId: Long,
    ): List<ExerciseMotivationalGoalEntity> {
        return exerciseMotivationalGoalDao.getActiveLongTermGoalsForExercise(exerciseId)
    }

    override fun observeActiveGoalsForSplitTemplateExercise(
        splitTemplateExerciseId: Long,
    ): Flow<List<ExerciseMotivationalGoalEntity>> {
        return exerciseMotivationalGoalDao.observeActiveGoalsForSplitTemplateExercise(
            splitTemplateExerciseId = splitTemplateExerciseId,
        )
    }

    override suspend fun getActiveGoalsForSplitTemplateExercise(
        splitTemplateExerciseId: Long,
    ): List<ExerciseMotivationalGoalEntity> {
        return exerciseMotivationalGoalDao.getActiveGoalsForSplitTemplateExercise(
            splitTemplateExerciseId = splitTemplateExerciseId,
        )
    }

    override suspend fun getGoalById(
        goalId: Long,
    ): ExerciseMotivationalGoalEntity? {
        return exerciseMotivationalGoalDao.getGoalById(goalId)
    }

    override suspend fun getActiveAssignmentsForGoal(
        goalId: Long,
    ): List<ExerciseMotivationalGoalAssignmentEntity> {
        return exerciseMotivationalGoalDao.getActiveAssignmentsForGoal(goalId)
    }

    override suspend fun insertGoal(
        goal: ExerciseMotivationalGoalEntity,
    ): Long {
        return exerciseMotivationalGoalDao.insertGoal(goal)
    }

    override suspend fun updateGoal(
        goal: ExerciseMotivationalGoalEntity,
    ) {
        exerciseMotivationalGoalDao.updateGoal(goal)
    }

    override suspend fun deleteGoal(
        goal: ExerciseMotivationalGoalEntity,
    ) {
        exerciseMotivationalGoalDao.deleteGoal(goal)
    }

    override suspend fun insertAssignment(
        assignment: ExerciseMotivationalGoalAssignmentEntity,
    ): Long {
        return exerciseMotivationalGoalDao.insertAssignment(assignment)
    }

    override suspend fun updateAssignment(
        assignment: ExerciseMotivationalGoalAssignmentEntity,
    ) {
        exerciseMotivationalGoalDao.updateAssignment(assignment)
    }

    override suspend fun deleteAssignment(
        assignment: ExerciseMotivationalGoalAssignmentEntity,
    ) {
        exerciseMotivationalGoalDao.deleteAssignment(assignment)
    }

    override suspend fun deactivateGoal(
        goalId: Long,
        updatedAtEpochMillis: Long,
    ) {
        exerciseMotivationalGoalDao.deactivateGoal(
            goalId = goalId,
            updatedAtEpochMillis = updatedAtEpochMillis,
        )
    }

    override suspend fun restoreGoalAndAssignments(
        goalId: Long,
        updatedAtEpochMillis: Long,
    ) {
        exerciseMotivationalGoalDao.restoreGoalAndAssignments(
            goalId = goalId,
            updatedAtEpochMillis = updatedAtEpochMillis,
        )
    }

    override suspend fun deactivateAssignment(
        assignmentId: Long,
        updatedAtEpochMillis: Long,
    ) {
        exerciseMotivationalGoalDao.deactivateAssignment(
            assignmentId = assignmentId,
            updatedAtEpochMillis = updatedAtEpochMillis,
        )
    }

    override suspend fun deactivateAssignmentsForGoal(
        goalId: Long,
        updatedAtEpochMillis: Long,
    ) {
        exerciseMotivationalGoalDao.deactivateAssignmentsForGoal(
            goalId = goalId,
            updatedAtEpochMillis = updatedAtEpochMillis,
        )
    }

    override suspend fun insertGoalWithLongTermAssignment(
        goal: ExerciseMotivationalGoalEntity,
        assignmentCreatedAtEpochMillis: Long,
    ): Long {
        return exerciseMotivationalGoalDao.insertGoalWithLongTermAssignment(
            goal = goal,
            assignmentCreatedAtEpochMillis = assignmentCreatedAtEpochMillis,
        )
    }

    override suspend fun assignGoalToSplitTemplateExerciseIfMissing(
        goalId: Long,
        splitTemplateExerciseId: Long,
        createdAtEpochMillis: Long,
    ): Long {
        return exerciseMotivationalGoalDao.assignGoalToSplitTemplateExerciseIfMissing(
            goalId = goalId,
            splitTemplateExerciseId = splitTemplateExerciseId,
            createdAtEpochMillis = createdAtEpochMillis,
        )
    }
}