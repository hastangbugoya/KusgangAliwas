package com.example.kusgangaliwas.domain.usecase.session

import com.example.kusgangaliwas.data.local.entity.ActualSessionEntity
import com.example.kusgangaliwas.domain.model.session.ActualSessionStatus
import com.example.kusgangaliwas.domain.repository.SessionRepository
import javax.inject.Inject

class CreateActualSessionFromPlannedSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val createSessionFromSplitUseCase: CreateSessionFromSplitUseCase,
) {

    suspend operator fun invoke(
        plannedSessionId: Long,
        performedDateEpochDay: Long,
    ): Long {
        val plannedSession = sessionRepository.getPlannedSessionById(
            plannedSessionId = plannedSessionId,
        ) ?: error("Planned session not found.")

        val splitTemplateId = plannedSession.splitTemplateId

        if (splitTemplateId != null) {
            return createSessionFromSplitUseCase(
                splitTemplateId = splitTemplateId,
                epochDay = performedDateEpochDay,
                trainingCycleId = plannedSession.cycleId,
                plannedSessionId = plannedSession.id,
            )
        }

        val now = System.currentTimeMillis()

        return sessionRepository.insertActualSession(
            ActualSessionEntity(
                plannedSessionId = plannedSession.id,
                performedDateEpochDay = performedDateEpochDay,
                splitTemplateId = null,
                trainingCycleId = plannedSession.cycleId,
                trainingCycleStepId = plannedSession.cycleStepId,
                trainingCycleStepOrderSnapshot = null,
                title = plannedSession.title,
                status = ActualSessionStatus.IN_PROGRESS,
                startedAtEpochMillis = now,
                completedAtEpochMillis = null,
                notes = plannedSession.notes,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
            )
        )
    }
}