package com.example.kusgangaliwas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents an optional high-level grouping of split schedules.
 *
 * A Program is intended for more structured training phases such as:
 * - "16-week Bulk"
 * - "12-week Cut"
 *
 * IMPORTANT DESIGN NOTES:
 * - Program is OPTIONAL. Casual users should not be forced to create one.
 * - Split schedules can exist independently without a Program.
 * - This entity mainly provides grouping, labeling, and optional defaults.
 *
 * RELATIONSHIPS:
 * - One Program can have many SplitScheduleEntity entries (via programId).
 *
 * FIELD NOTES:
 * - startEpochDay:
 *     Optional anchor for when the program is intended to begin.
 *     Not strictly required for scheduling logic.
 *
 * - horizonWeeks:
 *     Suggested default horizon for schedules created under this program.
 *     Actual scheduling should rely on SplitScheduleEntity.horizonWeeks.
 *
 * - isActive:
 *     Allows soft enabling/disabling of a program without deleting it.
 *
 * - createdAtEpochMillis / updatedAtEpochMillis:
 *     Standard audit fields.
 */
@Entity(tableName = "program")
data class ProgramEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val name: String,

    val notes: String = "",

    val startEpochDay: Long? = null,

    val horizonWeeks: Int,

    val isActive: Boolean = true,

    val createdAtEpochMillis: Long,

    val updatedAtEpochMillis: Long,
)