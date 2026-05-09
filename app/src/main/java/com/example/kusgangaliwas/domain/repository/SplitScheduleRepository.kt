package com.example.kusgangaliwas.domain.repository

import com.example.kusgangaliwas.data.local.entity.SplitScheduleEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for split scheduling rules.
 *
 * Split schedules describe when a SplitTemplate should generate planned
 * calendar sessions.
 *
 * Scheduling is intentionally simple:
 * - weekly day masks for v1
 * - cycle-based scheduling supported by schema but expanded later
 * - optional Program grouping
 */
interface SplitScheduleRepository {

    fun observeAllSchedules(): Flow<List<SplitScheduleEntity>>

    fun observeActiveSchedules(): Flow<List<SplitScheduleEntity>>

    fun observeSchedulesForSplitTemplate(
        splitTemplateId: Long,
    ): Flow<List<SplitScheduleEntity>>

    fun observeSchedulesForProgram(
        programId: Long,
    ): Flow<List<SplitScheduleEntity>>

    suspend fun getScheduleById(id: Long): SplitScheduleEntity?

    suspend fun upsertSchedule(schedule: SplitScheduleEntity): Long

    suspend fun updateSchedule(schedule: SplitScheduleEntity)

    suspend fun deleteSchedule(schedule: SplitScheduleEntity)

    suspend fun deleteScheduleById(id: Long)
}