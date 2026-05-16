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
import com.example.kusgangaliwas.domain.usecase.cycle.GetCycleDayContextUseCase
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

data class SessionDayUiState(
    val epochDay: Long = 0L,
    val title: String = "Session",
    val plannedSessions: List<PlannedSessionEntity> = emptyList(),
    val actualSessions: List<ActualSessionEntity> = emptyList(),
    val availableSplits: List<SplitTemplateEntity> = emptyList(),
    val cycleDayContext: CycleDayContext? = null,
)

@HiltViewModel
class SessionDayViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    sessionRepository: SessionRepository,
    private val createQuickSessionForDayUseCase: CreateQuickSessionForDayUseCase,
    splitTemplateRepository: SplitTemplateRepository,
    private val createSessionFromSplitUseCase: CreateSessionFromSplitUseCase,
    private val getCycleDayContextUseCase: GetCycleDayContextUseCase,
    private val markCycleSplitDoneUseCase: MarkCycleSplitDoneUseCase,
) : ViewModel() {

    private val epochDay: Long = checkNotNull(
        savedStateHandle.get<Long>("epochDay")
    ) {
        "Missing epochDay."
    }

    private val date: LocalDate = LocalDate.ofEpochDay(epochDay)

    private val cycleDayContext =
        MutableStateFlow<CycleDayContext?>(null)

    val uiState: StateFlow<SessionDayUiState> =
        combine(
            sessionRepository.observeSessionsForDate(epochDay),
            sessionRepository.observeActualSessionsBetweenDates(
                startEpochDay = epochDay,
                endEpochDay = epochDay + 1,
            ),
            splitTemplateRepository.observeActiveSplits(),
            cycleDayContext,
        ) { planned, actual, splits, cycleContext ->
            SessionDayUiState(
                epochDay = epochDay,
                title = date.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                plannedSessions = planned,
                actualSessions = actual,
                availableSplits = splits,
                cycleDayContext = cycleContext,
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
        refreshCycleDayContext()
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

    fun startSessionFromSplit(splitTemplateId: Long) {
        viewModelScope.launch {
            runCatching {
                createSessionFromSplitUseCase(
                    splitTemplateId = splitTemplateId,
                    epochDay = epochDay,
                )
                refreshCycleDayContext()
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun startCycleSession() {
        val context = cycleDayContext.value
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
                refreshCycleDayContext()
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun markCycleSplitDone() {
        val context = cycleDayContext.value
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
                refreshCycleDayContext()
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun refreshCycleDayContext() {
        viewModelScope.launch {
            runCatching {
                getCycleDayContextUseCase()
            }.onSuccess { context ->
                cycleDayContext.value = context
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }
}