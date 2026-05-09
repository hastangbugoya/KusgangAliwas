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
    ],
    indices = [
        Index(value = ["plannedSessionId"]),
        Index(value = ["performedDateEpochDay"]),
        Index(value = ["splitTemplateId"]),
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
     */
    val performedDateEpochDay: Long,

    /**
     * Optional split reference for quick display/filtering.
     */
    val splitTemplateId: Long? = null,

    val title: String,

    /**
     * Suggested values:
     * - "inProgress"
     * - "completed"
     * - "abandoned"
     */
    val status: String = "inProgress",

    val startedAtEpochMillis: Long? = null,
    val completedAtEpochMillis: Long? = null,

    val notes: String? = null,
    val rating: Int? = null, // 1-5 stars

    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)