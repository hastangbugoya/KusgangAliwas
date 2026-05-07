package com.example.kusgangaliwas.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Anchors a training cycle to a calendar date.
 *
 * This enables month-view projection and "force restart cycle here" behavior.
 *
 * Example:
 * - cycleId = Main Cycle
 * - anchorDateEpochDay = May 6, 2026
 * - anchorStepId = Split A step
 *
 * Meaning:
 * "Starting on May 6, project this cycle as if this date is this step."
 *
 * Later anchors can override earlier ones from their date onward.
 */
@Entity(
    tableName = "cycle_calendar_anchor",
    foreignKeys = [
        ForeignKey(
            entity = TrainingCycleEntity::class,
            parentColumns = ["id"],
            childColumns = ["cycleId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TrainingCycleStepEntity::class,
            parentColumns = ["id"],
            childColumns = ["anchorStepId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["cycleId"]),
        Index(value = ["anchorStepId"]),
        Index(value = ["cycleId", "anchorDateEpochDay"], unique = true),
    ],
)
data class CycleCalendarAnchorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val cycleId: Long,

    /**
     * LocalDate.toEpochDay().
     *
     * Stored this way to avoid timezone problems for calendar planning.
     */
    val anchorDateEpochDay: Long,

    /**
     * The cycle step that should appear on anchorDateEpochDay.
     */
    val anchorStepId: Long,

    val notes: String? = null,

    val createdAtEpochMillis: Long,
)