package com.example.kusgangaliwas.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents one step in a training cycle.
 *
 * A step can be:
 * - a split (linked to SplitTemplate)
 * - a rest step
 * - an open/optional step
 *
 * This allows flexible cycles like:
 * A → Rest → B → Rest → C → Rest → repeat
 */
@Entity(
    tableName = "training_cycle_step",
    foreignKeys = [
        ForeignKey(
            entity = TrainingCycleEntity::class,
            parentColumns = ["id"],
            childColumns = ["cycleId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = SplitTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["splitTemplateId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["cycleId"]),
        Index(value = ["splitTemplateId"]),
        // Ensure unique ordering per cycle
        Index(value = ["cycleId", "stepOrder"], unique = true),
    ],
)
data class TrainingCycleStepEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val cycleId: Long,

    /**
     * Order within the cycle (0-based or 1-based, your choice—just stay consistent).
     */
    val stepOrder: Int,

    /**
     * Type of step.
     *
     * Suggested values:
     * - "split"
     * - "rest"
     * - "open"
     */
    val stepType: String,

    /**
     * Only used when stepType == "split".
     */
    val splitTemplateId: Long? = null,

    /**
     * Optional label override (e.g. "Light Rest", "Optional Cardio").
     */
    val label: String? = null,

    /**
     * Optional color key for calendar display.
     * Keep as string for flexibility (e.g. "blue", "A", "cycle1_step2").
     */
    val colorKey: String? = null,

    val notes: String? = null,
)