package com.example.kusgangaliwas.domain.usecase.planning

import com.example.kusgangaliwas.domain.repository.SplitScheduleRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Refreshes planned sessions from currently active split schedules.
 *
 * This is a thin orchestration use case:
 * - loads active schedule rules
 * - delegates expansion/materialization to GeneratePlannedSessionsFromSchedulesUseCase
 *
 * Keeping this separate gives the app one clean entry point for:
 * - future app startup refresh
 * - future manual "Regenerate Plan" action
 * - future schedule-edit refresh
 * - future WorkManager refresh
 */
class RefreshPlannedSessionsUseCase @Inject constructor(
    private val splitScheduleRepository: SplitScheduleRepository,
    private val generatePlannedSessionsFromSchedules: GeneratePlannedSessionsFromSchedulesUseCase,
) {

    suspend operator fun invoke(
        currentEpochMillis: Long = System.currentTimeMillis(),
    ) {
        val activeSchedules = splitScheduleRepository
            .observeActiveSchedules()
            .first()

        generatePlannedSessionsFromSchedules(
            schedules = activeSchedules,
            currentEpochMillis = currentEpochMillis,
        )
    }
}