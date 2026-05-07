package com.example.kusgangaliwas.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents one set performed for an exercise within an actual session.
 *
 * This is intentionally simple for v1:
 * - supports weight + reps
 * - supports duration/distance for cardio-style exercises
 * - no analytics yet
 *
 * Future features (not v1):
 * - volume calculation
 * - PR tracking
 * - charts
 */
@Entity(
    tableName = "actual_exercise_set_log",
    foreignKeys = [
        ForeignKey(
            entity = ActualExerciseLogEntity::class,
            parentColumns = ["id"],
            childColumns = ["actualExerciseLogId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["actualExerciseLogId"]),
        Index(value = ["setOrder"]),
    ],
)
data class ActualExerciseSetLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val actualExerciseLogId: Long,

    /**
     * Order of the set within the exercise.
     */
    val setOrder: Int,

    val weight: Double? = null,
    val reps: Int? = null,

    val durationSeconds: Int? = null,
    val distance: Double? = null,

    val notes: String? = null,
)