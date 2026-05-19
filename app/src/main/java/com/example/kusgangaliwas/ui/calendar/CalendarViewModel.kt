package com.example.kusgangaliwas.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kusgangaliwas.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import com.example.kusgangaliwas.domain.model.WeeklyTrainingProgress
import com.example.kusgangaliwas.domain.usecase.session.GetWeeklyTrainingProgressUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.time.LocalDate
import com.example.kusgangaliwas.domain.model.cycle.CycleDayContext
import com.example.kusgangaliwas.domain.usecase.cycle.GetActiveCycleContextsUseCase
import com.example.kusgangaliwas.domain.model.session.ActualSessionStatus

/**
 * ViewModel for the monthly calendar screen.
 *
 * V1 responsibility:
 * - track the currently displayed month
 * - observe planned sessions inside that month
 * - expose lightweight day markers so generated schedules become visible
 *
 * This intentionally does not perform schedule generation itself.
 * Generation is handled by RefreshPlannedSessionsUseCase on app launch/resume
 * and by schedule save actions.
 *
 * Calendar completion logic:
 *
 * GREEN:
 * - logged >= planned
 *
 * YELLOW:
 * - planned > logged
 * - logged > 0
 *
 * RED:
 * - planned > 0
 * - logged == 0
 *
 * NEUTRAL:
 * - planned == 0
 * - logged == 0
 *
 * Important philosophy:
 * Any logged session counts toward satisfying the day, even if it was not the
 * originally planned split/session. The goal is encouraging consistency rather
 * than enforcing rigid adherence.
 */
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val getWeeklyTrainingProgressUseCase: GetWeeklyTrainingProgressUseCase,
    private val getActiveCycleContextsUseCase: GetActiveCycleContextsUseCase,
) : ViewModel() {

    private val visibleMonth = MutableStateFlow(YearMonth.now())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CalendarUiState> =
        visibleMonth
            .flatMapLatest { month ->
                val start = month.atDay(1)
                val endExclusive = month.plusMonths(1).atDay(1)

                combine(
                    sessionRepository.observeSessionsBetweenDates(
                        startEpochDay = start.toEpochDay(),
                        endEpochDay = endExclusive.toEpochDay(),
                    ),
                    sessionRepository.observeActualSessionsBetweenDates(
                        startEpochDay = start.toEpochDay(),
                        endEpochDay = endExclusive.toEpochDay(),
                    ),
                ) { plannedSessions, actualSessions ->

                    val plannedCounts =
                        plannedSessions.groupingBy {
                            it.scheduledDateEpochDay
                        }.eachCount()

                    val actualSessionsByDay =
                        actualSessions.groupBy {
                            it.performedDateEpochDay
                        }

                    val todayEpochDay = LocalDate.now().toEpochDay()

                    val allDays =
                        plannedCounts.keys + actualSessionsByDay.keys + todayEpochDay

                    val statusMap =
                        allDays.associateWith { epochDay ->

                            val planned = plannedCounts[epochDay] ?: 0
                            val dayActualSessions = actualSessionsByDay[epochDay].orEmpty()
                            val hasActualSession = dayActualSessions.isNotEmpty()
                            val hasInProgressSession = dayActualSessions.any {
                                it.status == ActualSessionStatus.IN_PROGRESS
                            }

                            when {
                                hasInProgressSession -> {
                                    CalendarDayStatus.IN_PROGRESS
                                }

                                hasActualSession -> {
                                    CalendarDayStatus.LOGGED
                                }

                                planned > 0 -> {
                                    CalendarDayStatus.PLANNED
                                }

                                epochDay == todayEpochDay -> {
                                    CalendarDayStatus.TODAY
                                }

                                else -> {
                                    CalendarDayStatus.NEUTRAL
                                }
                            }
                        }

                    val anchorDate = if (YearMonth.now() == month) {
                        LocalDate.now()
                    } else {
                        month.atDay(1)
                    }

                    val weeklyProgress = getWeeklyTrainingProgressUseCase(
                        anchorDate = anchorDate,
                    )

                    val activeCycleContexts = getActiveCycleContextsUseCase()

                    CalendarUiState(
                        month = month,
                        dayStatusByEpochDay = statusMap,
                        weeklyProgress = weeklyProgress,
                        activeCycleContexts = activeCycleContexts,
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = CalendarUiState(
                    month = visibleMonth.value,
                ),
            )

    fun setMonth(month: YearMonth) {
        visibleMonth.value = month
    }

    fun goToCurrentMonth() {
        visibleMonth.value = YearMonth.now()
    }

    fun goToPreviousMonth() {
        visibleMonth.value = visibleMonth.value.minusMonths(1)
    }

    fun goToNextMonth() {
        visibleMonth.value = visibleMonth.value.plusMonths(1)
    }
}

/**
 * UI state for the monthly calendar.
 *
 * dayStatusByEpochDay:
 * - key = LocalDate.toEpochDay()
 * - value = completion state for that day
 */
data class CalendarUiState(
    val month: YearMonth = YearMonth.now(),
    val dayStatusByEpochDay: Map<Long, CalendarDayStatus> = emptyMap(),
    val weeklyProgress: WeeklyTrainingProgress? = null,
    val activeCycleContexts: List<CycleDayContext> = emptyList(),
)

/**
 * Lightweight visual completion state for a calendar day.
 *
 * Drawable/icon mapping is intentionally deferred until later UI polish.
 * Temporary placeholders/resources are acceptable for now.
 */
enum class CalendarDayStatus {
    NEUTRAL,
    PLANNED,
    LOGGED,
    TODAY,
    IN_PROGRESS
}