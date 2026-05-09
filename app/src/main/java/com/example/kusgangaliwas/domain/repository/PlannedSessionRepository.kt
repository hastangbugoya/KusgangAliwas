package com.example.kusgangaliwas.domain.repository

import com.example.kusgangaliwas.data.local.entity.PlannedSessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for planned gym sessions.
 *
 * Planned sessions are concrete calendar-facing workout plans. They may come
 * from manual planning or, later, from SplitScheduleEntity horizon generation.
 *
 * This repository intentionally stays small for now:
 * - observe planned sessions by date/range
 * - create/update/delete planned sessions
 *
 * Schedule expansion should be added later as a use case rather than being
 * hidden inside this repository.
 */
interface PlannedSessionRepository {

    fun observePlannedSessionsForDate(epochDay: Long): Flow<List<PlannedSessionEntity>>

    fun observePlannedSessionsBetweenDates(
        startEpochDay: Long,
        endEpochDay: Long,
    ): Flow<List<PlannedSessionEntity>>

    suspend fun getPlannedSessionById(id: Long): PlannedSessionEntity?

    suspend fun upsertPlannedSession(session: PlannedSessionEntity): Long

    suspend fun deletePlannedSession(session: PlannedSessionEntity)

    suspend fun deletePlannedSessionById(id: Long)
}