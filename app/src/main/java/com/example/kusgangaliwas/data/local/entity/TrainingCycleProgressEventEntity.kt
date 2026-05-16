package com.example.kusgangaliwas.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Records non-session cycle progress events.
 *
 * Actual workout completions are still represented by ActualSessionEntity.
 * This table is for cycle actions that should affect the current cycle round
 * without creating a workout log, such as marking a split done.
 *
 * Cycle progression is day-based, not timestamp-based.
 */
@Entity(
    tableName = "training_cycle_progress_event",
    foreignKeys = [
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
    ],
    indices = [
        Index(value = ["trainingCycleId"]),
        Index(value = ["trainingCycleStepId"]),
        Index(
            value = [
                "trainingCycleId",
                "eventDateEpochDay",
            ],
        ),
    ],
)
data class TrainingCycleProgressEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val trainingCycleId: Long,

    val trainingCycleStepId: Long,

    /**
     * Suggested values:
     * - "markedDone"
     */
    val eventType: String,

    /**
     * Date the user chose to apply this progress event.
     */
    val eventDateEpochDay: Long,

    val notes: String? = null,

    val createdAtEpochMillis: Long,
)
