package com.example.kusgangaliwas.ui.split

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kusgangaliwas.data.local.entity.SplitScheduleEntity
import com.example.kusgangaliwas.data.local.entity.SplitTemplateExerciseEntity
import com.example.kusgangaliwas.domain.repository.ExerciseRepository
import com.example.kusgangaliwas.domain.repository.SplitScheduleRepository
import com.example.kusgangaliwas.domain.repository.SplitTemplateRepository
import com.example.kusgangaliwas.domain.usecase.planning.RefreshPlannedSessionsUseCase
import com.example.kusgangaliwas.domain.usecase.split.AddExerciseToSplitUseCase
import com.example.kusgangaliwas.domain.usecase.split.GetSplitRoadmapUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SplitRoadmapViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val splitTemplateRepository: SplitTemplateRepository,
    private val exerciseRepository: ExerciseRepository,
    private val splitScheduleRepository: SplitScheduleRepository,
    private val refreshPlannedSessionsUseCase: RefreshPlannedSessionsUseCase,
    private val addExerciseToSplitUseCase: AddExerciseToSplitUseCase,
    getSplitRoadmapUseCase: GetSplitRoadmapUseCase,
) : ViewModel() {

    private val splitId: Long = checkNotNull(savedStateHandle.get<Long>("splitId")) {
        "Missing splitId."
    }

    private val scheduleState = MutableStateFlow(SplitRoadmapScheduleState())

    val uiState: StateFlow<SplitRoadmapUiState> =
        combine(
            getSplitRoadmapUseCase(splitId),
            exerciseRepository.observeActiveExercises(),
            scheduleState,
        ) { roadmap, exercises, schedule ->
            val exerciseById = exercises.associateBy { it.id }

            SplitRoadmapUiState(
                splitId = splitId,
                splitName = schedule.splitName,
                roadmapItems = roadmap.map { item ->
                    val exercise = exerciseById[item.exerciseId]

                    SplitRoadmapItemUiState(
                        splitTemplateExercise = item,
                        exerciseName = exercise?.name ?: "Unknown exercise",
                        exerciseType = exercise?.exerciseType,
                    )
                },
                availableExercises = exercises,
                scheduleEnabled = schedule.scheduleEnabled,
                selectedDaysMask = schedule.selectedDaysMask,
                horizonWeeksText = schedule.horizonWeeksText,
                scheduleTitle = schedule.scheduleTitle,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SplitRoadmapUiState(splitId = splitId),
        )

    init {
        viewModelScope.launch {
            val split = splitTemplateRepository.getSplitById(splitId)

            scheduleState.update {
                it.copy(
                    splitName = split?.name ?: "Split",
                    scheduleTitle = split?.name.orEmpty(),
                )
            }
        }

        viewModelScope.launch {
            splitScheduleRepository
                .observeSchedulesForSplitTemplate(splitId)
                .collect { schedules ->
                    val schedule = schedules.firstOrNull {
                        it.scheduleMode == "WEEKLY_DAYS"
                    } ?: return@collect

                    scheduleState.update {
                        it.copy(
                            savedScheduleId = schedule.id,
                            savedStartEpochDay = schedule.startEpochDay,
                            savedCreatedAtEpochMillis = schedule.createdAtEpochMillis,
                            scheduleEnabled = schedule.isActive,
                            selectedDaysMask = schedule.daysOfWeekMask,
                            horizonWeeksText = schedule.horizonWeeks.toString(),
                            scheduleTitle = schedule.title,
                        )
                    }
                }
        }
    }

    fun renameSplit(
        newName: String,
    ) {
        viewModelScope.launch {
            runCatching {
                val trimmed = newName.trim()

                if (trimmed.isBlank()) {
                    return@runCatching
                }

                val split = splitTemplateRepository
                    .getSplitById(splitId)
                    ?: return@runCatching

                splitTemplateRepository.updateSplit(
                    split.copy(
                        name = trimmed,
                        updatedAtEpochMillis = System.currentTimeMillis(),
                    )
                )

                scheduleState.update {
                    it.copy(
                        splitName = trimmed,
                        scheduleTitle = trimmed,
                    )
                }
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun setScheduleEnabled(enabled: Boolean) {
        scheduleState.update {
            it.copy(scheduleEnabled = enabled)
        }
    }

    fun toggleScheduleDay(bitIndex: Int) {
        if (bitIndex !in 0..6) return

        scheduleState.update { current ->
            val bit = 1 shl bitIndex
            current.copy(
                selectedDaysMask = current.selectedDaysMask xor bit,
            )
        }
    }

    fun updateHorizonWeeksText(value: String) {
        if (value.all { it.isDigit() }) {
            scheduleState.update {
                it.copy(horizonWeeksText = value)
            }
        }
    }

    fun updateScheduleTitle(title: String) {
        scheduleState.update {
            it.copy(scheduleTitle = title)
        }
    }

    fun saveSchedule() {
        viewModelScope.launch {
            runCatching {
                val current = scheduleState.value
                val now = System.currentTimeMillis()
                val title = current.scheduleTitle.ifBlank { current.splitName }
                val horizonWeeks = current.horizonWeeksText
                    .toIntOrNull()
                    ?.coerceIn(1, 52)
                    ?: 1

                splitScheduleRepository.upsertSchedule(
                    SplitScheduleEntity(
                        id = current.savedScheduleId,
                        programId = null,
                        splitTemplateId = splitId,
                        title = title,
                        startEpochDay = current.savedStartEpochDay
                            ?: LocalDate.now().toEpochDay(),
                        horizonWeeks = horizonWeeks,
                        scheduleMode = "WEEKLY_DAYS",
                        daysOfWeekMask = current.selectedDaysMask,
                        cycleOrder = 0,
                        restDaysAfter = 0,
                        isActive = current.scheduleEnabled,
                        createdAtEpochMillis = current.savedCreatedAtEpochMillis ?: now,
                        updatedAtEpochMillis = now,
                    )
                )

                scheduleState.update {
                    it.copy(horizonWeeksText = horizonWeeks.toString())
                }

                refreshPlannedSessionsUseCase()
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun addExerciseToSplit(exerciseId: Long) {
        viewModelScope.launch {
            runCatching {
                addExerciseToSplitUseCase(
                    splitTemplateId = splitId,
                    exerciseId = exerciseId,
                )
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun deleteExerciseFromSplit(splitTemplateExerciseId: Long) {
        viewModelScope.launch {
            runCatching {
                splitTemplateRepository.deleteSplitExercise(splitTemplateExerciseId)
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun updateExerciseTargets(
        entity: SplitTemplateExerciseEntity,
        targetSets: Int?,
        targetRepsMin: Int?,
        targetRepsMax: Int?,
    ) {
        viewModelScope.launch {
            runCatching {
                splitTemplateRepository.updateSplitExercise(
                    entity.copy(
                        targetSets = targetSets,
                        targetRepsMin = targetRepsMin,
                        targetRepsMax = targetRepsMax,
                    )
                )
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }
}

private data class SplitRoadmapScheduleState(
    val splitName: String = "Split",
    val savedScheduleId: Long = 0L,
    val savedStartEpochDay: Long? = null,
    val savedCreatedAtEpochMillis: Long? = null,
    val scheduleEnabled: Boolean = false,
    val selectedDaysMask: Int = 0,
    val horizonWeeksText: String = "4",
    val scheduleTitle: String = "",
)