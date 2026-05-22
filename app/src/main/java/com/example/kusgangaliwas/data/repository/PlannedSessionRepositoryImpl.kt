package com.example.kusgangaliwas.data.repository

import com.example.kusgangaliwas.data.local.dao.PlannedSessionDao
import com.example.kusgangaliwas.data.local.dao.PlannedSessionExerciseDao
import com.example.kusgangaliwas.data.local.entity.PlannedSessionEntity
import com.example.kusgangaliwas.data.local.entity.PlannedSessionExerciseEntity
import com.example.kusgangaliwas.domain.repository.PlannedSessionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room-backed implementation of PlannedSessionRepository.
 *
 * This class intentionally keeps scheduling expansion out of the repository.
 * Planned session generation should live in a use case so rules stay testable
 * and easy to evolve.
 */
@Singleton
class PlannedSessionRepositoryImpl @Inject constructor(
    private val plannedSessionDao: PlannedSessionDao,
    private val plannedSessionExerciseDao: PlannedSessionExerciseDao,
) : PlannedSessionRepository {

    override fun observePlannedSessionsForDate(epochDay: Long): Flow<List<PlannedSessionEntity>> {
        return plannedSessionDao.observeSessionsForDate(epochDay)
    }

    override fun observePlannedSessionsBetweenDates(
        startEpochDay: Long,
        endEpochDay: Long,
    ): Flow<List<PlannedSessionEntity>> {
        return plannedSessionDao.observeSessionsBetweenDates(
            startEpochDay = startEpochDay,
            endEpochDay = endEpochDay,
        )
    }

    override suspend fun getPlannedSessionById(id: Long): PlannedSessionEntity? {
        return plannedSessionDao.getPlannedSessionById(id)
    }

    override suspend fun upsertPlannedSession(session: PlannedSessionEntity): Long {
        return if (session.id == 0L) {
            plannedSessionDao.insertPlannedSession(session)
        } else {
            plannedSessionDao.updatePlannedSession(session)
            session.id
        }
    }

    override suspend fun deletePlannedSession(session: PlannedSessionEntity) {
        plannedSessionDao.deletePlannedSession(session.id)
    }

    override suspend fun deletePlannedSessionById(id: Long) {
        plannedSessionDao.deletePlannedSession(id)
    }

    override fun observePlannedExercisesForSession(
        plannedSessionId: Long,
    ): Flow<List<PlannedSessionExerciseEntity>> {
        return plannedSessionExerciseDao.observeExercisesForPlannedSession(
            plannedSessionId = plannedSessionId,
        )
    }

    override suspend fun getPlannedExercisesForSession(
        plannedSessionId: Long,
    ): List<PlannedSessionExerciseEntity> {
        return plannedSessionExerciseDao.getExercisesForPlannedSession(
            plannedSessionId = plannedSessionId,
        )
    }

    override suspend fun upsertPlannedSessionExercise(
        exercise: PlannedSessionExerciseEntity,
    ): Long {
        return if (exercise.id == 0L) {
            plannedSessionExerciseDao.insertPlannedSessionExercise(exercise)
        } else {
            plannedSessionExerciseDao.updatePlannedSessionExercise(exercise)
            exercise.id
        }
    }

    override suspend fun upsertPlannedSessionExercises(
        exercises: List<PlannedSessionExerciseEntity>,
    ) {
        if (exercises.isEmpty()) {
            return
        }

        val newExercises = exercises.filter { exercise ->
            exercise.id == 0L
        }

        val existingExercises = exercises.filter { exercise ->
            exercise.id != 0L
        }

        if (newExercises.isNotEmpty()) {
            plannedSessionExerciseDao.insertPlannedSessionExercises(newExercises)
        }

        if (existingExercises.isNotEmpty()) {
            plannedSessionExerciseDao.updatePlannedSessionExercises(existingExercises)
        }
    }

    override suspend fun deletePlannedSessionExerciseById(id: Long) {
        plannedSessionExerciseDao.deletePlannedSessionExercise(
            plannedSessionExerciseId = id,
        )
    }

    override suspend fun deletePlannedExercisesForSession(plannedSessionId: Long) {
        plannedSessionExerciseDao.deleteAllForPlannedSession(
            plannedSessionId = plannedSessionId,
        )
    }
}