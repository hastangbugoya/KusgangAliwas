package com.example.kusgangaliwas.domain.usecase.session

import com.example.kusgangaliwas.data.local.entity.ActualSessionEntity
import com.example.kusgangaliwas.domain.repository.SessionRepository
import javax.inject.Inject

class CreateQuickSessionForDayUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
) {

    suspend operator fun invoke(
        epochDay: Long,
    ): Long {
        val now = System.currentTimeMillis()

        return sessionRepository.insertActualSession(
            ActualSessionEntity(
                plannedSessionId = null,
                performedDateEpochDay = epochDay,
                splitTemplateId = null,
                title = "Quick Session",
                status = "inProgress",
                startedAtEpochMillis = now,
                completedAtEpochMillis = null,
                notes = null,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
            )
        )
    }
}