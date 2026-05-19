package com.example.kusgangaliwas.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents one activation/run window for a training cycle.
 *
 * This is intentionally separate from CycleCalendarAnchorEntity:
 * - TrainingCycleActivationEntity answers "when was this cycle active?"
 * - CycleCalendarAnchorEntity answers "how should this cycle project onto dates?"
 *
 * A cycle can be activated, deactivated, and activated again many times.
 *
 * Active run:
 * - deactivatedDateEpochDay == null
 *
 * Inactive/completed historical run:
 * - deactivatedDateEpochDay != null
 */
@Entity(
    tableName = "training_cycle_activation",
    foreignKeys = [
        ForeignKey(
            entity = TrainingCycleEntity::class,
            parentColumns = ["id"],
            childColumns = ["cycleId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["cycleId"]),
        Index(value = ["cycleId", "activatedDateEpochDay"]),
        Index(value = ["cycleId", "deactivatedDateEpochDay"]),
    ],
)
data class TrainingCycleActivationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val cycleId: Long,

    /**
     * LocalDate.toEpochDay().
     *
     * Stored this way to avoid timezone problems for cycle run history.
     */
    val activatedDateEpochDay: Long,

    /**
     * Null means this activation/run is still active.
     */
    val deactivatedDateEpochDay: Long? = null,

    val notes: String? = null,

    val createdAtEpochMillis: Long,

    val updatedAtEpochMillis: Long,
)
