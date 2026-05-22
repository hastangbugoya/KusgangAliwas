package com.example.kusgangaliwas.domain.usecase.planning

import com.example.kusgangaliwas.data.local.entity.PlannedSessionEntity
import com.example.kusgangaliwas.data.local.entity.PlannedSessionExerciseEntity
import com.example.kusgangaliwas.data.local.entity.SplitScheduleEntity
import com.example.kusgangaliwas.domain.repository.PlannedSessionRepository
import com.example.kusgangaliwas.domain.repository.SplitTemplateRepository
import com.example.kusgangaliwas.domain.usecase.pace.ResolveExercisePaceProfileUseCase
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
    private val splitTemplateRepository: SplitTemplateRepository,
    private val resolveExercisePaceProfileUseCase: ResolveExercisePaceProfileUseCase,
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

        val splitExercises = splitTemplateRepository
            .getExercisesForSplit(schedule.splitTemplateId)

        var currentDate = startDate

        while (!currentDate.isAfter(endDate)) {

            if (matchesDayMask(currentDate.dayOfWeek, schedule.daysOfWeekMask)) {

                val key =
                    "${currentDate.toEpochDay()}_${schedule.splitTemplateId}"

                if (!existingKeys.contains(key)) {
                    val plannedSessionId = plannedSessionRepository.upsertPlannedSession(
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

                    createPlannedExercisesForSession(
                        plannedSessionId = plannedSessionId,
                        splitExercises = splitExercises,
                    )
                }
            }

            currentDate = currentDate.plusDays(1)
        }
    }

    private suspend fun createPlannedExercisesForSession(
        plannedSessionId: Long,
        splitExercises: List<com.example.kusgangaliwas.data.local.entity.SplitTemplateExerciseEntity>,
    ) {
        if (plannedSessionId <= 0L || splitExercises.isEmpty()) {
            return
        }

        val plannedExercises = splitExercises.map { splitExercise ->
            val resolvedPaceProfile = resolveExercisePaceProfileUseCase(
                exerciseId = splitExercise.exerciseId,
                paceProfileId = splitExercise.paceProfileId,
            )

            PlannedSessionExerciseEntity(
                plannedSessionId = plannedSessionId,
                plannedExerciseId = splitExercise.exerciseId,
                paceProfileId = resolvedPaceProfile?.id,
                sourceSplitTemplateExerciseId = splitExercise.id,
                sourcePlannedSessionExerciseId = null,
                originType = "template",
                suggestedOrder = splitExercise.suggestedOrder,
                status = "suggested",
                notes = splitExercise.notes,
            )
        }

        plannedSessionRepository.upsertPlannedSessionExercises(plannedExercises)
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