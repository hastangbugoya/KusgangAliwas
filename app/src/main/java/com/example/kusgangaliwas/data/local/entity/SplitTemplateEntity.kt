package com.example.kusgangaliwas.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a reusable training split or session template.
 *
 * Examples:
 * - Push
 * - Pull
 * - Legs
 * - Upper
 * - Lower
 * - Split A / B / C
 *
 * A split defines WHAT exercises are suggested, not WHEN they are performed.
 * Scheduling (calendar/cycle) is handled separately.
 */
@Entity(
    tableName = "split_template",
    indices = [
        Index(value = ["name"], unique = true),
    ],
)
data class SplitTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val name: String,

    val notes: String? = null,

    /**
     * Optional suggestion only.
     *
     * For users who prefer time-based spacing:
     * e.g. 2 = “consider resting ~2 days before repeating”
     *
     * Not enforced by the app.
     */
    val suggestedDaysBeforeRepeat: Int? = null,

    val isActive: Boolean = true,

    val createdAtEpochMillis: Long,

    val updatedAtEpochMillis: Long,
)