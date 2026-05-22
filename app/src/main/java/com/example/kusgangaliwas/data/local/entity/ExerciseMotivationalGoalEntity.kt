package com.example.kusgangaliwas.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Optional aspirational goal for an exercise.
 *
 * Motivational goals are user-facing context only:
 * - They can help the user remember what they hope to reach.
 * - They can be attached to splits, split exercises, schedules, cycles, or programs.
 * - They should not be interpreted as pass/fail workout requirements.
 * - They should not make the app show punitive language such as failed, missed,
 *   or behind.
 *
 * Runtime performance suggestions should still come from actual previous logs.
 * This table stores the user's target overlay, not the planned workout load.
 */
@Entity(
    tableName = "exercise_motivational_goal",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ExerciseMotivationalGoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["sourceGoalId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["exerciseId"]),
        Index(value = ["goalType"]),
        Index(value = ["sourceGoalId"]),
        Index(value = ["exerciseId", "isActive"]),
    ],
)
data class ExerciseMotivationalGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val exerciseId: Long,

    val goalType: ExerciseMotivationalGoalType,

    val targetWeight: Double? = null,

    val targetReps: Int? = null,

    val targetOneRepMax: Double? = null,

    val targetDistance: Double? = null,

    val targetDistanceUnit: String? = null,

    val targetDurationSeconds: Int? = null,

    val title: String,

    val notes: String? = null,

    val isActive: Boolean = true,

    val isMotivationalOnly: Boolean = true,

    val sourceGoalId: Long? = null,

    val createdAtEpochMillis: Long,

    val updatedAtEpochMillis: Long,
)
