package com.example.kusgangaliwas.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents one performed cardio block within an actual session.
 *
 * Examples:
 * - treadmill warm-up
 * - bike interval block
 * - cooldown walk
 *
 * Kept separate from strength sets because cardio is usually logged by
 * distance, duration, incline, resistance, or notes rather than reps × weight.
 */
@Entity(
    tableName = "actual_cardio_log",
    foreignKeys = [
        ForeignKey(
            entity = ActualSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["actualSessionId"],
            onDelete = ForeignKey.CASCADE,
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
        Index(value = ["exerciseId"]),
        Index(value = ["logOrder"]),
    ],
)
data class ActualCardioLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val actualSessionId: Long,

    /**
     * Optional exercise reference, such as treadmill, bike, rower, elliptical.
     *
     * Nullable so users can still log free-text cardio.
     */
    val exerciseId: Long? = null,

    /**
     * Order in which this cardio block was performed inside the session.
     *
     * This shares ordering semantics with ActualExerciseLogEntity.logOrder,
     * allowing cardio and strength to be merged into one session list.
     */
    val logOrder: Int,

    /**
     * Suggested values:
     * - "warmup"
     * - "steadyState"
     * - "intervals"
     * - "cooldown"
     * - "freeText"
     */
    val logType: String = "steadyState",

    val freeTextName: String? = null,

    val distance: Double? = null,

    /**
     * Suggested values:
     * - "mi"
     * - "km"
     * - "m"
     */
    val distanceUnit: String? = null,

    val durationSeconds: Long? = null,

    val averageInclinePercent: Double? = null,

    val averageResistance: Double? = null,

    val notes: String? = null,

    val performedAtEpochMillis: Long? = null,

    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)