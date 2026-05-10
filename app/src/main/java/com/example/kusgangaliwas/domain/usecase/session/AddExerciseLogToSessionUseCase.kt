package com.example.kusgangaliwas.domain.usecase.session

import com.example.kusgangaliwas.data.local.entity.ActualExerciseLogEntity
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
 * This use case intentionally does not copy prior sets.
 *
 * Weight suggestions are applied later when the user adds the first set for
 * the exercise. That keeps previous history as a hint instead of silently
 * creating multiple copied sets.
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

        return newLogId
    }
}