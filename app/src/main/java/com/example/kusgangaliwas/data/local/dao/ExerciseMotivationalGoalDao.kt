package com.example.kusgangaliwas.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.kusgangaliwas.data.local.entity.ExerciseMotivationalGoalAssignmentEntity
import com.example.kusgangaliwas.data.local.entity.ExerciseMotivationalGoalAssignmentScopeType
import com.example.kusgangaliwas.data.local.entity.ExerciseMotivationalGoalEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for optional motivational exercise goals.
 *
 * Motivational goals are aspirational overlays. They can be shown on exercise,
 * split, schedule, cycle, and future program screens without becoming planned
 * workout loads or pass/fail criteria.
 */
@Dao
interface ExerciseMotivationalGoalDao {
    @Query(
        """
        SELECT *
        FROM exercise_motivational_goal
        WHERE exerciseId = :exerciseId
            AND isActive = 1
        ORDER BY updatedAtEpochMillis DESC, id DESC
        """
    )
    fun observeActiveGoalsForExercise(
        exerciseId: Long,
    ): Flow<List<ExerciseMotivationalGoalEntity>>

    @Query(
        """
        SELECT *
        FROM exercise_motivational_goal
        WHERE exerciseId = :exerciseId
            AND isActive = 1
        ORDER BY updatedAtEpochMillis DESC, id DESC
        """
    )
    suspend fun getActiveGoalsForExercise(
        exerciseId: Long,
    ): List<ExerciseMotivationalGoalEntity>

    @Query(
        """
        SELECT *
        FROM exercise_motivational_goal
        WHERE exerciseId = :exerciseId
            AND isActive = 0
        ORDER BY updatedAtEpochMillis DESC, id DESC
        """
    )
    fun observeHiddenGoalsForExercise(
        exerciseId: Long,
    ): Flow<List<ExerciseMotivationalGoalEntity>>

    @Query(
        """
        SELECT *
        FROM exercise_motivational_goal
        WHERE exerciseId = :exerciseId
            AND isActive = 0
        ORDER BY updatedAtEpochMillis DESC, id DESC
        """
    )
    suspend fun getHiddenGoalsForExercise(
        exerciseId: Long,
    ): List<ExerciseMotivationalGoalEntity>

    @Query(
        """
        SELECT goal.*
        FROM exercise_motivational_goal AS goal
        INNER JOIN exercise_motivational_goal_assignment AS assignment
            ON assignment.goalId = goal.id
        WHERE goal.exerciseId = :exerciseId
            AND goal.isActive = 1
            AND assignment.isActive = 1
            AND assignment.scopeType = 'LONG_TERM'
        ORDER BY goal.updatedAtEpochMillis DESC, goal.id DESC
        """
    )
    fun observeActiveLongTermGoalsForExercise(
        exerciseId: Long,
    ): Flow<List<ExerciseMotivationalGoalEntity>>

    @Query(
        """
        SELECT goal.*
        FROM exercise_motivational_goal AS goal
        INNER JOIN exercise_motivational_goal_assignment AS assignment
            ON assignment.goalId = goal.id
        WHERE goal.exerciseId = :exerciseId
            AND goal.isActive = 1
            AND assignment.isActive = 1
            AND assignment.scopeType = 'LONG_TERM'
        ORDER BY goal.updatedAtEpochMillis DESC, goal.id DESC
        """
    )
    suspend fun getActiveLongTermGoalsForExercise(
        exerciseId: Long,
    ): List<ExerciseMotivationalGoalEntity>

    @Query(
        """
        SELECT goal.*
        FROM exercise_motivational_goal AS goal
        INNER JOIN exercise_motivational_goal_assignment AS assignment
            ON assignment.goalId = goal.id
        WHERE assignment.splitTemplateExerciseId = :splitTemplateExerciseId
            AND assignment.scopeType = 'SPLIT_TEMPLATE_EXERCISE'
            AND assignment.isActive = 1
            AND goal.isActive = 1
        ORDER BY goal.updatedAtEpochMillis DESC, goal.id DESC
        """
    )
    fun observeActiveGoalsForSplitTemplateExercise(
        splitTemplateExerciseId: Long,
    ): Flow<List<ExerciseMotivationalGoalEntity>>

    @Query(
        """
        SELECT goal.*
        FROM exercise_motivational_goal AS goal
        INNER JOIN exercise_motivational_goal_assignment AS assignment
            ON assignment.goalId = goal.id
        WHERE assignment.splitTemplateExerciseId = :splitTemplateExerciseId
            AND assignment.scopeType = 'SPLIT_TEMPLATE_EXERCISE'
            AND assignment.isActive = 1
            AND goal.isActive = 1
        ORDER BY goal.updatedAtEpochMillis DESC, goal.id DESC
        """
    )
    suspend fun getActiveGoalsForSplitTemplateExercise(
        splitTemplateExerciseId: Long,
    ): List<ExerciseMotivationalGoalEntity>

    @Query(
        """
        SELECT *
        FROM exercise_motivational_goal
        WHERE id = :goalId
        LIMIT 1
        """
    )
    suspend fun getGoalById(goalId: Long): ExerciseMotivationalGoalEntity?

    @Query(
        """
        SELECT *
        FROM exercise_motivational_goal_assignment
        WHERE goalId = :goalId
            AND isActive = 1
        ORDER BY updatedAtEpochMillis DESC, id DESC
        """
    )
    suspend fun getActiveAssignmentsForGoal(
        goalId: Long,
    ): List<ExerciseMotivationalGoalAssignmentEntity>

    @Query(
        """
        SELECT *
        FROM exercise_motivational_goal_assignment
        WHERE goalId = :goalId
            AND scopeType = 'LONG_TERM'
            AND isActive = 1
        ORDER BY updatedAtEpochMillis DESC, id DESC
        LIMIT 1
        """
    )
    suspend fun getActiveLongTermAssignmentForGoal(
        goalId: Long,
    ): ExerciseMotivationalGoalAssignmentEntity?

    @Query(
        """
        SELECT *
        FROM exercise_motivational_goal_assignment
        WHERE goalId = :goalId
            AND scopeType = 'SPLIT_TEMPLATE_EXERCISE'
            AND splitTemplateExerciseId = :splitTemplateExerciseId
            AND isActive = 1
        ORDER BY updatedAtEpochMillis DESC, id DESC
        LIMIT 1
        """
    )
    suspend fun getActiveSplitTemplateExerciseAssignment(
        goalId: Long,
        splitTemplateExerciseId: Long,
    ): ExerciseMotivationalGoalAssignmentEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertGoal(goal: ExerciseMotivationalGoalEntity): Long

    @Update
    suspend fun updateGoal(goal: ExerciseMotivationalGoalEntity)

    @Delete
    suspend fun deleteGoal(goal: ExerciseMotivationalGoalEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAssignment(
        assignment: ExerciseMotivationalGoalAssignmentEntity,
    ): Long

    @Update
    suspend fun updateAssignment(
        assignment: ExerciseMotivationalGoalAssignmentEntity,
    )

    @Delete
    suspend fun deleteAssignment(
        assignment: ExerciseMotivationalGoalAssignmentEntity,
    )

    @Query(
        """
        UPDATE exercise_motivational_goal
        SET isActive = 0,
            updatedAtEpochMillis = :updatedAtEpochMillis
        WHERE id = :goalId
        """
    )
    suspend fun deactivateGoal(
        goalId: Long,
        updatedAtEpochMillis: Long,
    )

    @Query(
        """
        UPDATE exercise_motivational_goal
        SET isActive = 1,
            updatedAtEpochMillis = :updatedAtEpochMillis
        WHERE id = :goalId
        """
    )
    suspend fun activateGoal(
        goalId: Long,
        updatedAtEpochMillis: Long,
    )

    @Query(
        """
        UPDATE exercise_motivational_goal_assignment
        SET isActive = 0,
            updatedAtEpochMillis = :updatedAtEpochMillis
        WHERE id = :assignmentId
        """
    )
    suspend fun deactivateAssignment(
        assignmentId: Long,
        updatedAtEpochMillis: Long,
    )

    @Query(
        """
        UPDATE exercise_motivational_goal_assignment
        SET isActive = 0,
            updatedAtEpochMillis = :updatedAtEpochMillis
        WHERE goalId = :goalId
        """
    )
    suspend fun deactivateAssignmentsForGoal(
        goalId: Long,
        updatedAtEpochMillis: Long,
    )

    @Query(
        """
        UPDATE exercise_motivational_goal_assignment
        SET isActive = 1,
            updatedAtEpochMillis = :updatedAtEpochMillis
        WHERE goalId = :goalId
        """
    )
    suspend fun activateAssignmentsForGoal(
        goalId: Long,
        updatedAtEpochMillis: Long,
    )

    /**
     * Restores a hidden goal and any old assignments that may have been hidden
     * before restore support existed.
     *
     * Goals remain motivational context only. Restoring a goal should not affect
     * workout completion, split completion, or any pass/fail status.
     */
    @Transaction
    suspend fun restoreGoalAndAssignments(
        goalId: Long,
        updatedAtEpochMillis: Long,
    ) {
        activateGoal(
            goalId = goalId,
            updatedAtEpochMillis = updatedAtEpochMillis,
        )
        activateAssignmentsForGoal(
            goalId = goalId,
            updatedAtEpochMillis = updatedAtEpochMillis,
        )
    }

    /**
     * Creates a goal and immediately gives it a long-term assignment.
     *
     * This is the default exercise-detail behavior for v1: a user creates a
     * gentle exercise goal, and the goal becomes visible on that exercise
     * without implying that every split must use it.
     */
    @Transaction
    suspend fun insertGoalWithLongTermAssignment(
        goal: ExerciseMotivationalGoalEntity,
        assignmentCreatedAtEpochMillis: Long,
    ): Long {
        val goalId = insertGoal(goal)
        insertAssignment(
            ExerciseMotivationalGoalAssignmentEntity(
                goalId = goalId,
                scopeType = ExerciseMotivationalGoalAssignmentScopeType.LONG_TERM,
                createdAtEpochMillis = assignmentCreatedAtEpochMillis,
                updatedAtEpochMillis = assignmentCreatedAtEpochMillis,
            )
        )
        return goalId
    }

    /**
     * Assigns an existing goal to one split exercise if an active assignment does
     * not already exist.
     *
     * This supports a simple import/carry-forward action from exercise goals
     * into split roadmap details while avoiding duplicate active rows.
     */
    @Transaction
    suspend fun assignGoalToSplitTemplateExerciseIfMissing(
        goalId: Long,
        splitTemplateExerciseId: Long,
        createdAtEpochMillis: Long,
    ): Long {
        val existingAssignment = getActiveSplitTemplateExerciseAssignment(
            goalId = goalId,
            splitTemplateExerciseId = splitTemplateExerciseId,
        )

        if (existingAssignment != null) {
            return existingAssignment.id
        }

        return insertAssignment(
            ExerciseMotivationalGoalAssignmentEntity(
                goalId = goalId,
                scopeType = ExerciseMotivationalGoalAssignmentScopeType.SPLIT_TEMPLATE_EXERCISE,
                splitTemplateExerciseId = splitTemplateExerciseId,
                createdAtEpochMillis = createdAtEpochMillis,
                updatedAtEpochMillis = createdAtEpochMillis,
            )
        )
    }
}