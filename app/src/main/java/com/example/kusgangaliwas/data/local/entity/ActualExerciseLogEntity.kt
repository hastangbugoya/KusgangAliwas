package com.example.kusgangaliwas.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents one performed (or noted) exercise within an actual session.
 *
 * This is the core "what actually happened" record.
 *
 * Supports:
 * - planned exercise performed
 * - substitution
 * - impromptu exercise
 * - loose/stray logging (IOU-style)
 */
@Entity(
    tableName = "actual_exercise_log",
    foreignKeys = [
        ForeignKey(
            entity = ActualSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["actualSessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = PlannedSessionExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["plannedSessionExerciseId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["actualSessionId"]),
        Index(value = ["plannedSessionExerciseId"]),
        Index(value = ["exerciseId"]),
        Index(value = ["logOrder"]),
    ],
)
data class ActualExerciseLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val actualSessionId: Long,

    /**
     * Optional reference to the planned item this came from.
     *
     * Null if:
     * - impromptu exercise
     * - loose/stray log
     */
    val plannedSessionExerciseId: Long? = null,

    /**
     * Exercise reference.
     *
     * Nullable to support loose logs like:
     * "did some cable work, details later"
     */
    val exerciseId: Long? = null,

    /**
     * Order in which the exercise was actually performed.
     */
    val logOrder: Int,

    /**
     * Suggested values:
     * - "plannedExercise"
     * - "substitution"
     * - "impromptu"
     * - "looseNote"
     */
    val logType: String,

    /**
     * Optional free-text name if exerciseId is null.
     */
    val freeTextName: String? = null,

    val notes: String? = null,

    val performedAtEpochMillis: Long? = null,
)