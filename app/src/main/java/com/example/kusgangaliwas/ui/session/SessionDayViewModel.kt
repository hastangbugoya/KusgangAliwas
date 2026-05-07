package com.example.kusgangaliwas.ui.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.kusgangaliwas.data.local.entity.ActualSessionEntity
import com.example.kusgangaliwas.data.local.entity.PlannedSessionEntity
import com.example.kusgangaliwas.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope

data class SessionDayUiState(
    val epochDay: Long = 0L,
    val title: String = "Session",
    val plannedSessions: List<PlannedSessionEntity> = emptyList(),
    val actualSessions: List<ActualSessionEntity> = emptyList(),
)

@HiltViewModel
class SessionDayViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    sessionRepository: SessionRepository,
) : ViewModel() {

    private val epochDay: Long = checkNotNull(
        savedStateHandle.get<Long>("epochDay")
    ) {
        "Missing epochDay."
    }

    private val date: LocalDate = LocalDate.ofEpochDay(epochDay)

    val uiState: StateFlow<SessionDayUiState> =
        combine(
            sessionRepository.observeSessionsForDate(epochDay),
            sessionRepository.observeActualSessionsBetweenDates(
                startEpochDay = epochDay,
                endEpochDay = epochDay,
            ),
        ) { planned, actual ->
            SessionDayUiState(
                epochDay = epochDay,
                title = date.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                plannedSessions = planned,
                actualSessions = actual,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SessionDayUiState(
                epochDay = epochDay,
                title = date.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
            ),
        )
}