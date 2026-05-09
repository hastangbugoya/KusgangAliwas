package com.example.kusgangaliwas.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Stores a reusable exercise/movement in the local exercise library.
 *
 * Exercises are intentionally separate from splits and sessions:
 * - Splits reference exercises as planned roadmap items.
 * - Actual session logs reference exercises when the user performs them.
 * - Muscle groups are attached through a many-to-many table.
 * - Substitutions/equivalences reference exercises separately.
 *
 * V1 keeps this simple and user-owned. More detailed metadata can be added later
 * without making the exercise itself depend on a specific plan or split.
 */
@Entity(
    tableName = "exercise",
    indices = [
        Index(value = ["name"], unique = true),
    ],
)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val name: String,

    val notes: String? = null,

    val referenceUrl: String? = null,

    val isActive: Boolean = true,

    val createdAtEpochMillis: Long,

    val updatedAtEpochMillis: Long,
)