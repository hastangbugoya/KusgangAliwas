package com.example.kusgangaliwas.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a scheduling rule for a SplitTemplate.
 *
 * This is the core scheduling primitive for KusgangAliwas.
 *
 * DESIGN GOALS:
 * - Simple, casual-user friendly scheduling (v1)
 * - Supports both:
 *      1) Weekly day-based scheduling (e.g., Mon/Wed/Fri)
 *      2) Cycle-based scheduling (Push → Rest → Pull → ...)
 * - Does NOT use HH-style anchors or complex recurrence
 *
 * RELATIONSHIPS:
 * - References SplitTemplateEntity via splitTemplateId
 * - Optionally grouped under ProgramEntity via programId
 */
@Entity(
    tableName = "split_schedule",
    indices = [
        Index(value = ["programId"]),
        Index(value = ["splitTemplateId"]),
    ],
)
data class SplitScheduleEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val programId: Long?,

    val splitTemplateId: Long,

    val title: String,

    val startEpochDay: Long,

    val horizonWeeks: Int,

    val scheduleMode: String,

    val daysOfWeekMask: Int = 0,

    val cycleOrder: Int = 0,

    val restDaysAfter: Int = 0,

    val isActive: Boolean = true,

    val createdAtEpochMillis: Long,

    val updatedAtEpochMillis: Long,
)