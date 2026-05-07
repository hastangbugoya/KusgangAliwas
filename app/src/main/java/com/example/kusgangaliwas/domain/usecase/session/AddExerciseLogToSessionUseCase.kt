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

        val existingLogs = sessionRepository.getLogsForSession(actualSessionId)
        val nextOrder = if (existingLogs.isEmpty()) {
            0
        } else {
            existingLogs.maxOf { it.logOrder } + 1
        }

        return sessionRepository.insertActualExerciseLog(
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
    }
}