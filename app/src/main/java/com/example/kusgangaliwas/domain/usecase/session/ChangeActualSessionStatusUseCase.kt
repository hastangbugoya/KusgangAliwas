package com.example.kusgangaliwas.domain.usecase.session

import com.example.kusgangaliwas.domain.model.session.ActualSessionStatus
import com.example.kusgangaliwas.domain.repository.SessionRepository
import javax.inject.Inject

class ChangeActualSessionStatusUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
) {

    suspend operator fun invoke(
        actualSessionId: Long,
        fromStatus: ActualSessionStatus,
        toStatus: ActualSessionStatus,
        updatedAtEpochMillis: Long,
    ): Boolean {
        val session = sessionRepository.getActualSessionById(actualSessionId)
            ?: return false

        if (session.status != fromStatus) {
            return false
        }

        sessionRepository.updateActualSessionStatus(
            actualSessionId = actualSessionId,
            status = toStatus,
            updatedAtEpochMillis = updatedAtEpochMillis,
        )

        return true
    }
}