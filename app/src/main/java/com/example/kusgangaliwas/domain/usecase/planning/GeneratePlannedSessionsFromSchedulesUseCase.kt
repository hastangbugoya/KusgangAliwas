package com.example.kusgangaliwas.domain.usecase.planning

import com.example.kusgangaliwas.data.local.entity.PlannedSessionEntity
import com.example.kusgangaliwas.data.local.entity.SplitScheduleEntity
import com.example.kusgangaliwas.domain.repository.PlannedSessionRepository
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

/**
 * Generates PlannedSessionEntity entries from active SplitScheduleEntity rules.
 *
 * DESIGN GOALS:
 * - Simple week-based expansion
 * - Casual-user friendly
 * - Deterministic and testable
 * - No HH-style anchors or occurrence complexity
 *
 * CURRENT V1 LIMITATIONS:
 * - Supports WEEKLY_DAYS schedules only
 * - Ignores CYCLE schedules for now
 * - Uses lightweight duplicate prevention
 * - Does not reconcile/move existing sessions
 */
class GeneratePlannedSessionsFromSchedulesUseCase @Inject constructor(
    private val plannedSessionRepository: PlannedSessionRepository,
) {

    suspend operator fun invoke(
        schedules: List<SplitScheduleEntity>,
        currentEpochMillis: Long = System.currentTimeMillis(),
    ) {
        schedules
            .filter { it.isActive }
            .forEach { schedule ->
                when (schedule.scheduleMode) {
                    "WEEKLY_DAYS" -> {
                        generateWeeklySchedule(
                            schedule = schedule,
                            currentEpochMillis = currentEpochMillis,
                        )
                    }

                    "CYCLE" -> {
                        // v2
                    }
                }
            }
    }

    private suspend fun generateWeeklySchedule(
        schedule: SplitScheduleEntity,
        currentEpochMillis: Long,
    ) {
        val startDate = LocalDate.ofEpochDay(schedule.startEpochDay)

        val endDate = startDate.plusWeeks(schedule.horizonWeeks.toLong())

        val existingSessions = plannedSessionRepository
            .observePlannedSessionsBetweenDates(
                startEpochDay = startDate.toEpochDay(),
                endEpochDay = endDate.toEpochDay(),
            )
            .first()

        val existingKeys = existingSessions.map {
            "${it.scheduledDateEpochDay}_${it.splitTemplateId}"
        }.toSet()

        var currentDate = startDate

        while (!currentDate.isAfter(endDate)) {

            if (matchesDayMask(currentDate.dayOfWeek, schedule.daysOfWeekMask)) {

                val key =
                    "${currentDate.toEpochDay()}_${schedule.splitTemplateId}"

                if (!existingKeys.contains(key)) {

                    plannedSessionRepository.upsertPlannedSession(
                        PlannedSessionEntity(
                            scheduledDateEpochDay = currentDate.toEpochDay(),
                            title = schedule.title,
                            splitTemplateId = schedule.splitTemplateId,
                            sourceType = "scheduleGenerated",
                            status = "planned",
                            notes = null,
                            createdAtEpochMillis = currentEpochMillis,
                            updatedAtEpochMillis = currentEpochMillis,
                        )
                    )
                }
            }

            currentDate = currentDate.plusDays(1)
        }
    }

    /**
     * Bit positions:
     *
     * 0 = Sunday
     * 1 = Monday
     * 2 = Tuesday
     * 3 = Wednesday
     * 4 = Thursday
     * 5 = Friday
     * 6 = Saturday
     */
    private fun matchesDayMask(
        dayOfWeek: DayOfWeek,
        mask: Int,
    ): Boolean {

        val bitIndex = when (dayOfWeek) {
            DayOfWeek.SUNDAY -> 0
            DayOfWeek.MONDAY -> 1
            DayOfWeek.TUESDAY -> 2
            DayOfWeek.WEDNESDAY -> 3
            DayOfWeek.THURSDAY -> 4
            DayOfWeek.FRIDAY -> 5
            DayOfWeek.SATURDAY -> 6
        }

        return (mask and (1 shl bitIndex)) != 0
    }
}