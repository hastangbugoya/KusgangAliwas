package com.example.kusgangaliwas.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a reusable training cycle.
 *
 * Example:
 * - Split A
 * - Rest
 * - Split B
 * - Rest
 * - Split C
 * - Rest
 * - repeat
 *
 * The cycle defines the suggested sequence, not strict enforcement.
 */
@Entity(
    tableName = "training_cycle",
    indices = [
        Index(value = ["name"], unique = true),
    ],
)
data class TrainingCycleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val name: String,

    val notes: String? = null,

    val isActive: Boolean = true,

    val createdAtEpochMillis: Long,

    val updatedAtEpochMillis: Long,
)