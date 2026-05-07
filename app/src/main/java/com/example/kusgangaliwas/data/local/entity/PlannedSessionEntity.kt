package com.example.kusgangaliwas.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a planned training session on the calendar.
 *
 * Important distinction:
 * - scheduledDateEpochDay = when the user intended/planned the session
 * - actual execution will be stored separately in ActualSessionEntity
 *
 * This allows:
 * - "I planned Split A for Monday"
 * - "I actually did Monday's Split A on Wednesday"
 *
 * A planned session may come from:
 * - manual calendar planning
 * - cycle projection/materialization
 * - user restart/replan action
 */
@Entity(
    tableName = "planned_session",
    foreignKeys = [
        ForeignKey(
            entity = SplitTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["splitTemplateId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = TrainingCycleEntity::class,
            parentColumns = ["id"],
            childColumns = ["cycleId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = TrainingCycleStepEntity::class,
            parentColumns = ["id"],
            childColumns = ["cycleStepId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["scheduledDateEpochDay"]),
        Index(value = ["splitTemplateId"]),
        Index(value = ["cycleId"]),
        Index(value = ["cycleStepId"]),
    ],
)
data class PlannedSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /**
     * LocalDate.toEpochDay().
     *
     * This is the date the session was planned for, not necessarily
     * the date it was performed.
     */
    val scheduledDateEpochDay: Long,

    val title: String,

    val splitTemplateId: Long? = null,

    val cycleId: Long? = null,

    val cycleStepId: Long? = null,

    /**
     * Suggested values:
     * - "manual"
     * - "cycleGenerated"
     * - "cycleRestart"
     */
    val sourceType: String,

    /**
     * Suggested values:
     * - "planned"
     * - "inProgress"
     * - "completed"
     * - "partiallyCompleted"
     * - "intentionallySkipped"
     * - "missed"
     * - "moved"
     */
    val status: String = "planned",

    val notes: String? = null,

    val createdAtEpochMillis: Long,

    val updatedAtEpochMillis: Long,
)