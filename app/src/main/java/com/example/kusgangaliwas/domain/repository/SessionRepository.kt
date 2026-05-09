package com.example.kusgangaliwas.domain.repository

import com.example.kusgangaliwas.data.local.entity.ActualExerciseLogEntity
import com.example.kusgangaliwas.data.local.entity.ActualExerciseSetLogEntity
import com.example.kusgangaliwas.data.local.entity.ActualSessionEntity
import com.example.kusgangaliwas.data.local.entity.PlannedSessionEntity
import com.example.kusgangaliwas.data.local.entity.PlannedSessionExerciseEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository boundary for planning + execution of sessions.
 *
 * Combines:
 * - planned sessions (calendar intent)
 * - actual sessions (what happened)
 * - exercise-level logs
 * - set-level logs
 */
interface SessionRepository {

    // ----------------------------
    // Planned Sessions
    // ----------------------------

    fun observeSessionsBetweenDates(
        startEpochDay: Long,
        endEpochDay: Long,
    ): Flow<List<PlannedSessionEntity>>

    fun observeSessionsForDate(
        epochDay: Long,
    ): Flow<List<PlannedSessionEntity>>

    suspend fun getPlannedSessionById(
        plannedSessionId: Long,
    ): PlannedSessionEntity?

    suspend fun insertPlannedSession(entity: PlannedSessionEntity): Long

    suspend fun updatePlannedSession(entity: PlannedSessionEntity)

    suspend fun updatePlannedSessionStatus(
        plannedSessionId: Long,
        status: String,
        updatedAtEpochMillis: Long,
    )

    suspend fun deletePlannedSession(plannedSessionId: Long)

    // ----------------------------
    // Planned Session Exercises
    // ----------------------------

    fun observeExercisesForPlannedSession(
        plannedSessionId: Long,
    ): Flow<List<PlannedSessionExerciseEntity>>

    suspend fun getExercisesForPlannedSession(
        plannedSessionId: Long,
    ): List<PlannedSessionExerciseEntity>

    suspend fun getCarryForwardCandidates(): List<PlannedSessionExerciseEntity>

    suspend fun insertPlannedSessionExercise(
        entity: PlannedSessionExerciseEntity,
    ): Long

    suspend fun insertPlannedSessionExercises(
        entities: List<PlannedSessionExerciseEntity>,
    )

    suspend fun updatePlannedSessionExercise(
        entity: PlannedSessionExerciseEntity,
    )

    suspend fun updatePlannedSessionExercises(
        entities: List<PlannedSessionExerciseEntity>,
    )

    suspend fun updatePlannedSessionExerciseStatus(
        plannedSessionExerciseId: Long,
        status: String,
    )

    suspend fun deletePlannedSessionExercise(
        plannedSessionExerciseId: Long,
    )

    suspend fun deleteAllPlannedExercisesForSession(
        plannedSessionId: Long,
    )

    // ----------------------------
    // Actual Sessions
    // ----------------------------

    fun observeAllActualSessions(): Flow<List<ActualSessionEntity>>

    fun observeActualSessionsBetweenDates(
        startEpochDay: Long,
        endEpochDay: Long,
    ): Flow<List<ActualSessionEntity>>

    suspend fun getActualSessionById(
        actualSessionId: Long,
    ): ActualSessionEntity?

    suspend fun getLatestActualForPlannedSession(
        plannedSessionId: Long,
    ): ActualSessionEntity?

    suspend fun insertActualSession(entity: ActualSessionEntity): Long

    suspend fun updateActualSession(entity: ActualSessionEntity)

    suspend fun updateActualSessionStatus(
        actualSessionId: Long,
        status: String,
        updatedAtEpochMillis: Long,
    )

    suspend fun deleteActualSession(actualSessionId: Long)

    // ----------------------------
    // Actual Exercise Logs
    // ----------------------------

    fun observeLogsForSession(
        actualSessionId: Long,
    ): Flow<List<ActualExerciseLogEntity>>

    suspend fun getLogsForSession(
        actualSessionId: Long,
    ): List<ActualExerciseLogEntity>

    suspend fun insertActualExerciseLog(
        entity: ActualExerciseLogEntity,
    ): Long

    suspend fun insertActualExerciseLogs(
        entities: List<ActualExerciseLogEntity>,
    )

    suspend fun updateActualExerciseLog(
        entity: ActualExerciseLogEntity,
    )

    suspend fun updateActualExerciseLogs(
        entities: List<ActualExerciseLogEntity>,
    )

    suspend fun deleteActualExerciseLog(actualExerciseLogId: Long)

    suspend fun deleteAllLogsForSession(actualSessionId: Long)

    // ----------------------------
    // Exercise Sets
    // ----------------------------

    fun observeSetsForExercise(
        actualExerciseLogId: Long,
    ): Flow<List<ActualExerciseSetLogEntity>>

    suspend fun getSetsForExercise(
        actualExerciseLogId: Long,
    ): List<ActualExerciseSetLogEntity>

    suspend fun insertSet(entity: ActualExerciseSetLogEntity): Long

    suspend fun insertSets(entities: List<ActualExerciseSetLogEntity>)

    suspend fun updateSet(entity: ActualExerciseSetLogEntity)

    suspend fun updateSets(entities: List<ActualExerciseSetLogEntity>)

    suspend fun deleteSet(setId: Long)

    suspend fun deleteAllSetsForExercise(actualExerciseLogId: Long)

    suspend fun getLogsForExercise(
        exerciseId: Long,
    ): List<ActualExerciseLogEntity>

    fun observeActualSessionById(actualSessionId: Long): Flow<ActualSessionEntity?>
}