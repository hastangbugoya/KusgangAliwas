package com.example.kusgangaliwas.ui.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kusgangaliwas.data.local.entity.ActualSessionEntity
import com.example.kusgangaliwas.data.local.entity.PlannedSessionEntity
import com.example.kusgangaliwas.data.local.entity.SplitTemplateEntity
import com.example.kusgangaliwas.domain.model.cycle.CycleDayContext
import com.example.kusgangaliwas.domain.repository.SessionRepository
import com.example.kusgangaliwas.domain.repository.SplitTemplateRepository
import com.example.kusgangaliwas.domain.usecase.cycle.GetActiveCycleContextsUseCase
import com.example.kusgangaliwas.domain.usecase.session.CreateQuickSessionForDayUseCase
import com.example.kusgangaliwas.domain.usecase.session.CreateSessionFromSplitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.kusgangaliwas.domain.usecase.cycle.MarkCycleSplitDoneUseCase
import com.example.kusgangaliwas.domain.usecase.session.CreateActualSessionFromPlannedSessionUseCase

data class SessionDayUiState(
    val epochDay: Long = 0L,
    val title: String = "Session",
    val plannedSessions: List<PlannedSessionEntity> = emptyList(),
    val actualSessions: List<ActualSessionEntity> = emptyList(),
    val availableSplits: List<SplitTemplateEntity> = emptyList(),
    val activeCycleContexts: List<CycleDayContext> = emptyList(),
)

@HiltViewModel
class SessionDayViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    sessionRepository: SessionRepository,
    private val createQuickSessionForDayUseCase: CreateQuickSessionForDayUseCase,
    splitTemplateRepository: SplitTemplateRepository,
    private val createSessionFromSplitUseCase: CreateSessionFromSplitUseCase,
    private val getActiveCycleContextsUseCase: GetActiveCycleContextsUseCase,
    private val markCycleSplitDoneUseCase: MarkCycleSplitDoneUseCase,
    private val createActualSessionFromPlannedSessionUseCase: CreateActualSessionFromPlannedSessionUseCase,
) : ViewModel() {

    private val epochDay: Long = checkNotNull(
        savedStateHandle.get<Long>("epochDay")
    ) {
        "Missing epochDay."
    }

    private val date: LocalDate = LocalDate.ofEpochDay(epochDay)

    private val activeCycleContexts =
        MutableStateFlow<List<CycleDayContext>>(emptyList())

    val uiState: StateFlow<SessionDayUiState> =
        combine(
            sessionRepository.observeSessionsForDate(epochDay),
            sessionRepository.observeActualSessionsBetweenDates(
                startEpochDay = epochDay,
                endEpochDay = epochDay + 1,
            ),
            splitTemplateRepository.observeActiveSplits(),
            activeCycleContexts,
        ) { planned, actual, splits, cycleContexts ->
            SessionDayUiState(
                epochDay = epochDay,
                title = date.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                plannedSessions = planned,
                actualSessions = actual,
                availableSplits = splits,
                activeCycleContexts = cycleContexts,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SessionDayUiState(
                epochDay = epochDay,
                title = date.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
            ),
        )

    init {
        refreshCycleDayContexts()
    }

    fun startQuickSession() {
        viewModelScope.launch {
            runCatching {
                createQuickSessionForDayUseCase(epochDay)
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun startPlannedSession(plannedSessionId: Long) {
        viewModelScope.launch {
            runCatching {
                createActualSessionFromPlannedSessionUseCase(
                    plannedSessionId = plannedSessionId,
                    performedDateEpochDay = epochDay,
                )
                refreshCycleDayContexts()
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun startSessionFromSplit(splitTemplateId: Long) {
        viewModelScope.launch {
            runCatching {
                createSessionFromSplitUseCase(
                    splitTemplateId = splitTemplateId,
                    epochDay = epochDay,
                )
                refreshCycleDayContexts()
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun startCycleSession(trainingCycleId: Long) {
        val context = activeCycleContexts.value
            .firstOrNull { it.trainingCycleId == trainingCycleId }
            ?: return

        val splitTemplateId = context.nextSplitTemplateId
            ?: return

        viewModelScope.launch {
            runCatching {
                createSessionFromSplitUseCase(
                    splitTemplateId = splitTemplateId,
                    epochDay = epochDay,
                    trainingCycleId = context.trainingCycleId,
                )
                refreshCycleDayContexts()
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun markCycleSplitDone(trainingCycleId: Long) {
        val context = activeCycleContexts.value
            .firstOrNull { it.trainingCycleId == trainingCycleId }
            ?: return

        val nextStepId = context.nextStepId
            ?: return

        viewModelScope.launch {
            runCatching {
                markCycleSplitDoneUseCase(
                    trainingCycleId = context.trainingCycleId,
                    trainingCycleStepId = nextStepId,
                    eventDateEpochDay = epochDay,
                    createdAtEpochMillis = System.currentTimeMillis(),
                )
                refreshCycleDayContexts()
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun refreshCycleDayContexts() {
        viewModelScope.launch {
            runCatching {
                getActiveCycleContextsUseCase()
            }.onSuccess { contexts ->
                activeCycleContexts.value = contexts
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }
}