package com.example.kusgangaliwas.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Connects a motivational exercise goal to a display/planning context.
 *
 * Assignments make goals portable:
 * - a long-term exercise goal can be shown on the exercise detail screen
 * - the same goal can be carried into a split exercise
 * - a split goal can later be carried into a training cycle or schedule horizon
 *
 * These assignments are motivational context only. They should not be treated
 * as planned workout loads, completion criteria, or failure conditions.
 */
@Entity(
    tableName = "exercise_motivational_goal_assignment",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseMotivationalGoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = SplitTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["splitTemplateId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = SplitTemplateExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["splitTemplateExerciseId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TrainingCycleEntity::class,
            parentColumns = ["id"],
            childColumns = ["trainingCycleId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TrainingCycleStepEntity::class,
            parentColumns = ["id"],
            childColumns = ["trainingCycleStepId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = SplitScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["splitScheduleId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ProgramEntity::class,
            parentColumns = ["id"],
            childColumns = ["programId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ExerciseMotivationalGoalAssignmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["sourceAssignmentId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["goalId"]),
        Index(value = ["scopeType"]),
        Index(value = ["splitTemplateId"]),
        Index(value = ["splitTemplateExerciseId"]),
        Index(value = ["trainingCycleId"]),
        Index(value = ["trainingCycleStepId"]),
        Index(value = ["splitScheduleId"]),
        Index(value = ["programId"]),
        Index(value = ["sourceAssignmentId"]),
        Index(value = ["goalId", "scopeType"]),
        Index(value = ["scopeType", "isActive"]),
    ],
)
data class ExerciseMotivationalGoalAssignmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val goalId: Long,

    val scopeType: ExerciseMotivationalGoalAssignmentScopeType,

    val splitTemplateId: Long? = null,

    val splitTemplateExerciseId: Long? = null,

    val trainingCycleId: Long? = null,

    val trainingCycleStepId: Long? = null,

    val splitScheduleId: Long? = null,

    val programId: Long? = null,

    val startEpochDay: Long? = null,

    val targetEpochDay: Long? = null,

    val horizonWeeks: Int? = null,

    val sourceAssignmentId: Long? = null,

    val isActive: Boolean = true,

    val createdAtEpochMillis: Long,

    val updatedAtEpochMillis: Long,
)