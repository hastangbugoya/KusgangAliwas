package com.example.kusgangaliwas.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Stores a user-facing muscle/body-area label.
 *
 * These are intentionally layman-friendly labels such as:
 * - upper chest
 * - side shoulders
 * - triceps
 * - lats
 * - quads
 *
 * Muscle groups are separate from splits:
 * - Muscle groups describe what an exercise targets.
 * - Splits describe how the user organizes training.
 */
@Entity(
    tableName = "muscle_group",
    indices = [
        Index(value = ["name"], unique = true),
    ],
)
data class MuscleGroupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val name: String,

    val sortOrder: Int = 0,

    val isActive: Boolean = true,
)