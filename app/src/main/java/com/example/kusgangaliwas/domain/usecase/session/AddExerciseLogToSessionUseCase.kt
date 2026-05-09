package com.example.kusgangaliwas.domain.usecase.session

import com.example.kusgangaliwas.data.local.entity.ActualExerciseLogEntity
import com.example.kusgangaliwas.data.local.entity.ActualExerciseSetLogEntity
import com.example.kusgangaliwas.domain.repository.SessionRepository
import javax.inject.Inject

/**
 * Adds an exercise log to an actual session.
 *
 * This supports the simplest Session v1 behavior:
 * - user opens a quick session
 * - user adds an exercise from the exercise library
 *
 * The log is treated as impromptu for now because it is not linked to a
 * planned session exercise yet.
 *
 * When possible, this also seeds the new exercise log with the sets from the
 * most recent previous log for the same exercise. This gives the user a
 * practical starting point based on recent performance without making the plan
 * strict or punitive.
 */
class AddExerciseLogToSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
) {

    suspend operator fun invoke(
        actualSessionId: Long,
        exerciseId: Long,
    ): Long {
        require(actualSessionId > 0) {
            "Invalid actualSessionId."
        }

        require(exerciseId > 0) {
            "Invalid exerciseId."
        }

        val existingSessionLogs = sessionRepository.getLogsForSession(actualSessionId)
        val nextOrder = if (existingSessionLogs.isEmpty()) {
            0
        } else {
            existingSessionLogs.maxOf { it.logOrder } + 1
        }

        val newLogId = sessionRepository.insertActualExerciseLog(
            ActualExerciseLogEntity(
                actualSessionId = actualSessionId,
                plannedSessionExerciseId = null,
                exerciseId = exerciseId,
                logOrder = nextOrder,
                logType = "impromptu",
                freeTextName = null,
                notes = null,
                performedAtEpochMillis = System.currentTimeMillis(),
            )
        )

        seedSetsFromMostRecentPreviousLog(
            exerciseId = exerciseId,
            newLogId = newLogId,
        )

        return newLogId
    }

    private suspend fun seedSetsFromMostRecentPreviousLog(
        exerciseId: Long,
        newLogId: Long,
    ) {
        val previousLog = sessionRepository
            .getLogsForExercise(exerciseId)
            .firstOrNull { log ->
                log.id != newLogId
            } ?: return

        val previousSets = sessionRepository.getSetsForExercise(previousLog.id)

        if (previousSets.isEmpty()) {
            return
        }

        val copiedSets = previousSets.mapIndexed { index, set ->
            ActualExerciseSetLogEntity(
                actualExerciseLogId = newLogId,
                setOrder = index + 1,
                weight = set.weight,
                reps = set.reps,
                durationSeconds = set.durationSeconds,
                distance = set.distance,
                notes = null,
            )
        }

        sessionRepository.insertSets(copiedSets)
    }
}