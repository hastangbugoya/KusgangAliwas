package com.example.kusgangaliwas.domain.usecase.session

import com.example.kusgangaliwas.domain.model.WeeklyTrainingDayProgress
import com.example.kusgangaliwas.domain.model.WeeklyTrainingProgress
import com.example.kusgangaliwas.domain.repository.SessionRepository
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject

class GetWeeklyTrainingProgressUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
) {

    suspend operator fun invoke(
        anchorDate: LocalDate = LocalDate.now(),
        locale: Locale = Locale.getDefault(),
    ): WeeklyTrainingProgress {
        val firstDayOfWeek = WeekFields.of(locale).firstDayOfWeek

        val daysBack = (
                anchorDate.dayOfWeek.value -
                        firstDayOfWeek.value +
                        7
                ) % 7

        val weekStart = anchorDate.minusDays(daysBack.toLong())
        val weekEndExclusive = weekStart.plusDays(7)

        val sessions = sessionRepository
            .getActualSessionsBetweenDates(
                startEpochDay = weekStart.toEpochDay(),
                endEpochDay = weekEndExclusive.toEpochDay(),
            )

        val days = (0..6).map { offset ->
            val date = weekStart.plusDays(offset.toLong())
            val epochDay = date.toEpochDay()

            val daySessions = sessions.filter {
                it.performedDateEpochDay == epochDay
            }

            var strengthVolume = 0.0
            var completedSets = 0
            var cardioDistance = 0.0
            var completedCardioEntries = 0

            daySessions.forEach { session ->

                val exerciseLogs = sessionRepository
                    .getLogsForSession(session.id)

                exerciseLogs.forEach { log ->
                    val sets = sessionRepository
                        .getSetsForExercise(log.id)

                    sets.forEach { set ->
                        val weight = set.weight
                        val reps = set.reps

                        if (weight != null && reps != null) {
                            strengthVolume += (weight * reps)
                            completedSets++
                        }
                    }
                }

                val cardioLogs = sessionRepository
                    .getCardioLogsForSession(session.id)

                cardioLogs.forEach { cardio ->
                    val hasActualData =
                        cardio.distance != null ||
                                cardio.durationSeconds != null ||
                                cardio.averageInclinePercent != null ||
                                cardio.averageResistance != null

                    if (hasActualData) {
                        cardioDistance += cardio.distance ?: 0.0
                        completedCardioEntries++
                    }
                }
            }

            WeeklyTrainingDayProgress(
                epochDay = epochDay,
                strengthVolume = strengthVolume,
                cardioDistance = cardioDistance,
                completedSets = completedSets,
                completedCardioEntries = completedCardioEntries,
            )
        }

        return WeeklyTrainingProgress(
            weekStartEpochDay = weekStart.toEpochDay(),
            days = days,
        )
    }
}