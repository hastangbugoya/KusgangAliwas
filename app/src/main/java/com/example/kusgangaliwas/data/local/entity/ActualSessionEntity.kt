package com.example.kusgangaliwas.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents an actual performed workout session.
 *
 * This is intentionally separate from PlannedSessionEntity to allow:
 * - scheduled date ≠ performed date
 * - logging a planned session on a different day
 * - logging sessions without any plan (impromptu workouts)
 *
 * Cycle progression is intentionally day-based, not timestamp-based.
 * The cycle system should use performedDateEpochDay rather than session
 * timestamps when determining cycle completion/progress.
 */
@Entity(
    tableName = "actual_session",
    foreignKeys = [
        ForeignKey(
            entity = PlannedSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["plannedSessionId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = SplitTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["splitTemplateId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = TrainingCycleEntity::class,
            parentColumns = ["id"],
            childColumns = ["trainingCycleId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["plannedSessionId"]),
        Index(value = ["performedDateEpochDay"]),
        Index(value = ["splitTemplateId"]),
        Index(value = ["trainingCycleId"]),
        Index(value = ["trainingCycleStepId"]),
        Index(
            value = [
                "trainingCycleId",
                "performedDateEpochDay",
            ],
        ),
    ],
)
data class ActualSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /**
     * Optional link back to the planned session.
     *
     * Null if this was a fully impromptu session.
     */
    val plannedSessionId: Long? = null,

    /**
     * The actual date the workout happened.
     *
     * Cycle progression should use this field instead of timestamps.
     */
    val performedDateEpochDay: Long,

    /**
     * Optional split reference for quick display/filtering.
     */
    val splitTemplateId: Long? = null,

    /**
     * Optional cycle association.
     *
     * Null if this session was not started from a cycle.
     */
    val trainingCycleId: Long? = null,

    /**
     * Optional cycle step association.
     *
     * Stores the exact cycle step used when the session started.
     */
    val trainingCycleStepId: Long? = null,

    /**
     * Snapshot of the cycle ordering when the session was created.
     *
     * This preserves historical context even if the cycle is reordered later.
     */
    val trainingCycleStepOrderSnapshot: Int? = null,

    val title: String,

    /**
     * Suggested values:
     * - "inProgress"
     * - "completed"
     * - "abandoned"
     */
    val status: String = "inProgress",

    /**
     * Optional real-world timestamps for audit/history/UI display.
     *
     * Cycle progression logic should NOT depend on these values.
     */
    val startedAtEpochMillis: Long? = null,
    val completedAtEpochMillis: Long? = null,

    val notes: String? = null,

    /**
     * Optional user reflection score.
     *
     * Suggested range:
     * - 1 to 5 stars
     */
    val rating: Int? = null,

    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)