package com.example.kusgangaliwas.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents one ordered split inside a reusable training cycle.
 *
 * Cycles are intentionally day-agnostic. They do not schedule workouts by
 * calendar date and they do not enforce completion. They only provide the
 * ordered split queue the user cycles through.
 *
 * Runtime progress should be derived from cycle progress/session history, not
 * from this definition row.
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
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["cycleId"]),
        Index(value = ["splitTemplateId"]),
        Index(value = ["cycleId", "stepOrder"], unique = true),
        Index(value = ["cycleId", "splitTemplateId"], unique = true),
    ],
)
data class TrainingCycleStepEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val cycleId: Long,

    /**
     * Order within the cycle. Use 0-based ordering.
     */
    val stepOrder: Int,

    /**
     * The split template represented by this cycle step.
     */
    val splitTemplateId: Long,

    /**
     * When true, the UI should warn before allowing the user to mark this
     * split done without logging an actual workout.
     *
     * This is only gentle friction. It should not block the user.
     */
    val warnBeforeMarkDone: Boolean = false,

    val notes: String? = null,
)