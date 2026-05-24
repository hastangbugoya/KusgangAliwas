package com.example.kusgangaliwas.ui.split

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kusgangaliwas.data.local.entity.SplitScheduleEntity
import com.example.kusgangaliwas.data.local.entity.SplitTemplateExerciseEntity
import com.example.kusgangaliwas.data.local.entity.SplitTemplateMuscleGroupCrossRef
import com.example.kusgangaliwas.domain.repository.ExerciseMotivationalGoalRepository
import com.example.kusgangaliwas.domain.repository.ExercisePaceProfileRepository
import com.example.kusgangaliwas.domain.repository.ExerciseRepository
import com.example.kusgangaliwas.domain.repository.SplitScheduleRepository
import com.example.kusgangaliwas.domain.repository.SplitTemplateRepository
import com.example.kusgangaliwas.domain.usecase.pace.ApplyPaceProfileNameToSplitUseCase
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
    private val exercisePaceProfileRepository: ExercisePaceProfileRepository,
    private val exerciseMotivationalGoalRepository: ExerciseMotivationalGoalRepository,
    private val splitScheduleRepository: SplitScheduleRepository,
    private val refreshPlannedSessionsUseCase: RefreshPlannedSessionsUseCase,
    private val addExerciseToSplitUseCase: AddExerciseToSplitUseCase,
    private val applyPaceProfileNameToSplitUseCase: ApplyPaceProfileNameToSplitUseCase,
    getSplitRoadmapUseCase: GetSplitRoadmapUseCase,
) : ViewModel() {

    private val splitId: Long = checkNotNull(savedStateHandle.get<Long>("splitId")) {
        "Missing splitId."
    }

    private val scheduleState = MutableStateFlow(SplitRoadmapScheduleState())

    private val goalRefreshSignal = MutableStateFlow(0)

    val uiState: StateFlow<SplitRoadmapUiState> =
        combine(
            getSplitRoadmapUseCase(splitId),
            exerciseRepository.observeActiveExercises(),
            exerciseRepository.observeActiveMuscleGroups(),
            splitTemplateRepository.observeMuscleGroupsForSplit(splitId),
            combine(
                scheduleState,
                goalRefreshSignal,
            ) { schedule, _ ->
                schedule
            },
        ) { roadmap, exercises, muscleGroups, selectedSplitMuscleGroups, schedule ->
            val exerciseById = exercises.associateBy { it.id }

            SplitRoadmapUiState(
                splitId = splitId,
                splitName = schedule.splitName,
                roadmapItems = roadmap.map { item ->
                    val exercise = exerciseById[item.exerciseId]
                    val paceProfiles = exercisePaceProfileRepository
                        .getProfilesForExercise(item.exerciseId)
                    val attachedMotivationalGoals = exerciseMotivationalGoalRepository
                        .getActiveGoalsForSplitTemplateExercise(item.id)
                    val attachedGoalIds = attachedMotivationalGoals
                        .map { goal -> goal.id }
                        .toSet()
                    val availableLongTermMotivationalGoals = exerciseMotivationalGoalRepository
                        .getActiveLongTermGoalsForExercise(item.exerciseId)
                        .filterNot { goal ->
                            attachedGoalIds.contains(goal.id)
                        }

                    SplitRoadmapItemUiState(
                        splitTemplateExercise = item,
                        exerciseName = exercise?.name ?: "Unknown exercise",
                        exerciseType = exercise?.exerciseType,
                        paceProfiles = paceProfiles,
                        attachedMotivationalGoals = attachedMotivationalGoals,
                        availableLongTermMotivationalGoals = availableLongTermMotivationalGoals,
                    )
                },
                availableExercises = exercises,
                scheduleEnabled = schedule.scheduleEnabled,
                selectedDaysMask = schedule.selectedDaysMask,
                horizonWeeksText = schedule.horizonWeeksText,
                scheduleTitle = schedule.scheduleTitle,
                availableMuscleGroups = muscleGroups,
                selectedMuscleGroupIds = selectedSplitMuscleGroups
                    .map { it.muscleGroupId }
                    .toSet(),
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

    fun toggleMuscleGroupForSplit(
        muscleGroupId: Long,
        isSelected: Boolean,
    ) {
        viewModelScope.launch {
            if (isSelected) {
                splitTemplateRepository.deleteSplitMuscleGroup(
                    splitTemplateId = splitId,
                    muscleGroupId = muscleGroupId,
                )
            } else {
                splitTemplateRepository.upsertSplitMuscleGroup(
                    SplitTemplateMuscleGroupCrossRef(
                        splitTemplateId = splitId,
                        muscleGroupId = muscleGroupId,
                    )
                )
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
        saveSchedule()
    }

    fun toggleScheduleDay(bitIndex: Int) {
        if (bitIndex !in 0..6) return
        scheduleState.update { current ->
            val bit = 1 shl bitIndex
            current.copy(
                selectedDaysMask = current.selectedDaysMask xor bit,
            )
        }
        saveSchedule()
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

    fun updateCardioTargets(
        entity: SplitTemplateExerciseEntity,
        targetDistance: Double?,
        targetDistanceUnit: String?,
        targetDurationMinutes: Int?,
    ) {
        viewModelScope.launch {
            runCatching {
                splitTemplateRepository.updateSplitExercise(
                    entity.copy(
                        targetDistance = targetDistance,
                        targetDistanceUnit = targetDistanceUnit?.takeIf { it.isNotBlank() },
                        targetDurationMinutes = targetDurationMinutes,
                    )
                )
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun updatePaceProfileForSplitExercise(
        entity: SplitTemplateExerciseEntity,
        paceProfileId: Long?,
    ) {
        viewModelScope.launch {
            runCatching {
                splitTemplateRepository.updatePaceProfileForSplitExercise(
                    splitTemplateExerciseId = entity.id,
                    paceProfileId = paceProfileId,
                )
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun importMotivationalGoalToSplitExercise(
        goalId: Long,
        splitTemplateExerciseId: Long,
    ) {
        viewModelScope.launch {
            runCatching {
                exerciseMotivationalGoalRepository.assignGoalToSplitTemplateExerciseIfMissing(
                    goalId = goalId,
                    splitTemplateExerciseId = splitTemplateExerciseId,
                    createdAtEpochMillis = System.currentTimeMillis(),
                )
            }.onSuccess {
                goalRefreshSignal.update { it + 1 }
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun applyPaceProfileNameToSplit(
        paceProfileName: String,
    ) {
        viewModelScope.launch {
            runCatching {
                applyPaceProfileNameToSplitUseCase(
                    splitTemplateId = splitId,
                    paceProfileName = paceProfileName,
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