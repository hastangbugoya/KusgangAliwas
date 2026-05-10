package com.example.kusgangaliwas.ui.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kusgangaliwas.data.local.entity.ActualExerciseLogEntity
import com.example.kusgangaliwas.data.local.entity.ActualExerciseSetLogEntity
import com.example.kusgangaliwas.data.local.entity.ActualSessionEntity
import com.example.kusgangaliwas.data.local.entity.ExerciseEntity
import com.example.kusgangaliwas.domain.repository.ExerciseRepository
import com.example.kusgangaliwas.domain.repository.SessionRepository
import com.example.kusgangaliwas.domain.usecase.session.AddExerciseLogToSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SessionDetailUiState(
    val session: ActualSessionEntity? = null,
    val exerciseLogs: List<SessionExerciseLogUiState> = emptyList(),
    val availableExercises: List<ExerciseEntity> = emptyList(),
)

data class SessionExerciseLogUiState(
    val log: ActualExerciseLogEntity,
    val exerciseName: String,
    val sets: List<ActualExerciseSetLogEntity> = emptyList(),

    /**
     * Suggested starting weight derived from previous exercise history.
     *
     * V1:
     * - latest session
     * - maximum logged weight from that session
     *
     * Null means:
     * - no prior history
     * - or no logged weights yet
     */
    val suggestedWeight: Double? = null,
)

private data class ExerciseLogWithSets(
    val log: ActualExerciseLogEntity,
    val sets: List<ActualExerciseSetLogEntity>,
)

@HiltViewModel
class SessionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionRepository: SessionRepository,
    exerciseRepository: ExerciseRepository,
    private val addExerciseLogToSessionUseCase: AddExerciseLogToSessionUseCase,
) : ViewModel() {

    private val actualSessionId: Long = checkNotNull(
        savedStateHandle.get<Long>("actualSessionId")
    ) {
        "Missing actualSessionId."
    }

    private val exerciseLogsWithSets =
        sessionRepository.observeLogsForSession(actualSessionId)
            .flatMapLatest { logs ->
                if (logs.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    combine(
                        logs.map { log ->
                            sessionRepository.observeSetsForExercise(log.id)
                                .map { sets ->
                                    ExerciseLogWithSets(
                                        log = log,
                                        sets = sets,
                                    )
                                }
                        }
                    ) { items ->
                        items.toList()
                    }
                }
            }

    val uiState: StateFlow<SessionDetailUiState> =
        combine(
            sessionRepository.observeActualSessionById(actualSessionId),
            exerciseLogsWithSets,
            exerciseRepository.observeActiveExercises(),
        ) { session, logsWithSets, exercises ->
            val exerciseById = exercises.associateBy { it.id }

            SessionDetailUiState(
                session = session,
                exerciseLogs = logsWithSets.map { item ->
                    SessionExerciseLogUiState(
                        log = item.log,
                        exerciseName = item.log.exerciseId
                            ?.let { exerciseById[it]?.name }
                            ?: item.log.freeTextName
                            ?: "Loose exercise note",
                        sets = item.sets,
                    )
                },
                availableExercises = exercises,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SessionDetailUiState(),
        )
    fun addSet(actualExerciseLogId: Long) {
        viewModelScope.launch {
            runCatching {
                val existingSets = sessionRepository.getSetsForExercise(actualExerciseLogId)
                val nextOrder = existingSets.size + 1

                val exerciseLog = sessionRepository
                    .getLogsForSession(actualSessionId)
                    .firstOrNull { it.id == actualExerciseLogId }

                val suggestion =
                    if (nextOrder == 1) {
                        exerciseLog?.exerciseId
                            ?.let { exerciseId ->
                                sessionRepository
                                    .getLatestWeightSuggestionForExercise(exerciseId)
                            }
                    } else {
                        null
                    }

                sessionRepository.insertSet(
                    ActualExerciseSetLogEntity(
                        actualExerciseLogId = actualExerciseLogId,
                        setOrder = nextOrder,
                        reps = suggestion?.suggestedReps,
                        weight = suggestion?.suggestedWeight,
                        notes = suggestion?.let {
                            buildString {
                                append("From previous session max")

                                it.suggestedWeight.let { weight ->
                                    append(" (")
                                    append(formatWeight(weight))

                                    it.suggestedReps?.let { reps ->
                                        append(" × ")
                                        append(reps)
                                    }

                                    append(")")
                                }
                            }
                        },
                    )
                )
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun addExercise(exerciseId: Long) {
        viewModelScope.launch {
            runCatching {
                addExerciseLogToSessionUseCase(
                    actualSessionId = actualSessionId,
                    exerciseId = exerciseId,
                )
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun updateSet(
        set: ActualExerciseSetLogEntity,
        weight: Double?,
        reps: Int?,
    ) {
        viewModelScope.launch {
            runCatching {
                sessionRepository.updateSet(
                    set.copy(
                        weight = weight,
                        reps = reps,
                    )
                )
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun deleteSet(setId: Long) {
        viewModelScope.launch {
            runCatching {
                sessionRepository.deleteSet(setId)
            }.onFailure { it.printStackTrace() }
        }
    }

    fun duplicateSet(set: ActualExerciseSetLogEntity) {
        viewModelScope.launch {
            runCatching {
                val existing = sessionRepository.getSetsForExercise(set.actualExerciseLogId)
                val nextOrder = existing.size + 1

                sessionRepository.insertSet(
                    set.copy(
                        id = 0L,
                        setOrder = nextOrder,
                    )
                )
            }.onFailure { it.printStackTrace() }
        }
    }

    fun updateSessionRating(rating: Int?) {
        viewModelScope.launch {
            runCatching {
                val session = sessionRepository.getActualSessionById(actualSessionId)
                    ?: return@runCatching

                sessionRepository.updateActualSession(
                    session.copy(
                        rating = rating,
                        updatedAtEpochMillis = System.currentTimeMillis(),
                    )
                )
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun deleteExerciseLogIfEmpty(
        actualExerciseLogId: Long,
    ) {
        viewModelScope.launch {
            runCatching {
                val sets = sessionRepository.getSetsForExercise(actualExerciseLogId)

                if (sets.isEmpty()) {
                    sessionRepository.deleteActualExerciseLog(actualExerciseLogId)
                }
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun deleteSession() {
        viewModelScope.launch {
            runCatching {
                sessionRepository.deleteActualSession(actualSessionId)
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }
}