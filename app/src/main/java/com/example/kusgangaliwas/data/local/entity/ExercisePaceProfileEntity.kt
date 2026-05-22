package com.example.kusgangaliwas.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Named pace profile for an exercise.
 *
 * Pace profiles are optional gentle timing presets used later by focused
 * exercise logging and hands-free gym nudges.
 *
 * An exercise may have multiple named profiles, such as:
 * - Default
 * - Heavy strength
 * - Hypertrophy
 * - Warmup
 * - Full cardio
 *
 * At most one profile should be treated as the default for an exercise by
 * repository/use-case logic. Room does not enforce that rule here because
 * SQLite partial unique indexes are better handled in migrations if needed.
 */
@Entity(
    tableName = "exercise_pace_profile",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["exerciseId"]),
        Index(value = ["exerciseId", "name"], unique = true),
    ],
)
data class ExercisePaceProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val exerciseId: Long,
    val name: String,
    val isDefault: Boolean = false,
    val isEnabled: Boolean = true,
    val prepLeadSeconds: Int = 0,
    val expectedWorkSeconds: Int = 0,
    val expectedRestSeconds: Int = 0,
    val nextSetWarningSeconds: Int = 0,
    val idleReminderIntervalSeconds: Int = 0,
    val idleReminderEnabled: Boolean = false,
    val etiquetteReminderEnabled: Boolean = false,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)