package com.example.kusgangaliwas.ui.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kusgangaliwas.data.local.entity.ActualCardioLogEntity
import com.example.kusgangaliwas.data.local.entity.ActualExerciseLogEntity
import com.example.kusgangaliwas.data.local.entity.ExerciseType
import com.example.kusgangaliwas.domain.repository.ExerciseRepository
import com.example.kusgangaliwas.domain.repository.SessionRepository
import com.example.kusgangaliwas.ui.split.ExercisePickerItem
import com.example.kusgangaliwas.ui.split.ExercisePickerUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SessionExercisePickerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionRepository: SessionRepository,
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {

    val actualSessionId: Long = checkNotNull(
        savedStateHandle.get<Long>("actualSessionId")
    ) {
        "Missing actualSessionId."
    }

    private val searchText = MutableStateFlow("")
    private val selectedExerciseIds = MutableStateFlow<Set<Long>>(emptySet())

    val uiState: StateFlow<ExercisePickerUiState> =
        combine(
            exerciseRepository.observeActiveExercises(),
            sessionRepository.observeLogsForSession(actualSessionId),
            sessionRepository.observeCardioLogsForSession(actualSessionId),
            searchText,
            selectedExerciseIds,
        ) { exercises, strengthLogs, cardioLogs, search, selectedIds ->

            val existingExerciseIds =
                strengthLogs.mapNotNull { it.exerciseId }.toSet() +
                        cardioLogs.mapNotNull { it.exerciseId }.toSet()

            val normalizedSearch = search.trim()

            val filteredExercises = exercises
                .filter { exercise ->
                    normalizedSearch.isBlank() ||
                            exercise.name.contains(
                                normalizedSearch,
                                ignoreCase = true,
                            )
                }
                .sortedWith(
                    compareBy(
                        { it.exerciseType.sortOrder() },
                        { it.name.lowercase() },
                    )
                )

            ExercisePickerUiState(
                title = "Add session items",
                searchText = search,
                exercises = filteredExercises.map { exercise ->
                    ExercisePickerItem(
                        exerciseId = exercise.id,
                        exerciseName = exercise.name,
                        exerciseTypeLabel = exercise.exerciseType.displayText(),
                        supportingText = exercise.exerciseType.supportingText(),
                        alreadySelected = exercise.id in existingExerciseIds,
                    )
                },
                selectedExerciseIds = selectedIds,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ExercisePickerUiState(
                title = "Add session items",
            ),
        )

    fun onSearchTextChange(value: String) {
        searchText.value = value
    }

    fun toggleExercise(exerciseId: Long) {
        val item = uiState.value.exercises.firstOrNull {
            it.exerciseId == exerciseId
        } ?: return

        if (item.alreadySelected) {
            return
        }

        selectedExerciseIds.value =
            if (exerciseId in selectedExerciseIds.value) {
                selectedExerciseIds.value - exerciseId
            } else {
                selectedExerciseIds.value + exerciseId
            }
    }

    fun addSelectedExercisesToSession(
        onDone: () -> Unit,
    ) {
        val selectedIds = selectedExerciseIds.value

        if (selectedIds.isEmpty()) {
            onDone()
            return
        }

        viewModelScope.launch {
            runCatching {
                var nextOrder = getNextSessionItemLogOrder()
                val now = System.currentTimeMillis()

                selectedIds.forEach { exerciseId ->
                    val exercise = exerciseRepository.getExerciseById(exerciseId)
                        ?: return@forEach

                    when (exercise.exerciseType) {
                        ExerciseType.CARDIO -> {
                            val suggestion = sessionRepository
                                .getLatestCardioSuggestionForExercise(exercise.id)

                            sessionRepository.insertCardioLog(
                                ActualCardioLogEntity(
                                    actualSessionId = actualSessionId,
                                    exerciseId = exercise.id,
                                    logOrder = nextOrder,
                                    logType = "steadyState",
                                    freeTextName = exercise.name,
                                    distance = null,
                                    distanceUnit = suggestion?.distanceUnit ?: "mi",
                                    durationSeconds = null,
                                    averageInclinePercent = null,
                                    averageResistance = null,
                                    notes = null,
                                    createdAtEpochMillis = now,
                                    updatedAtEpochMillis = now,
                                )
                            )
                        }

                        else -> {
                            sessionRepository.insertActualExerciseLog(
                                ActualExerciseLogEntity(
                                    actualSessionId = actualSessionId,
                                    exerciseId = exercise.id,
                                    logOrder = nextOrder,
                                    logType = "plannedExercise",
                                    freeTextName = exercise.name,
                                    notes = null,
                                    performedAtEpochMillis = now,
                                )
                            )
                        }
                    }

                    nextOrder += 1
                }

                selectedExerciseIds.value = emptySet()
                onDone()
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    private suspend fun getNextSessionItemLogOrder(): Int {
        val exerciseLogs = sessionRepository.getLogsForSession(actualSessionId)
        val cardioLogs = sessionRepository.getCardioLogsForSession(actualSessionId)

        return (
                exerciseLogs.map { it.logOrder } +
                        cardioLogs.map { it.logOrder }
                )
            .maxOrNull()
            ?.plus(1)
            ?: 1
    }

    private fun ExerciseType.displayText(): String {
        return when (this) {
            ExerciseType.STRENGTH -> "Strength"
            ExerciseType.CARDIO -> "Cardio"
            ExerciseType.MOBILITY -> "Mobility"
            ExerciseType.OTHER -> "Other"
        }
    }

    private fun ExerciseType.supportingText(): String {
        return when (this) {
            ExerciseType.STRENGTH -> "Strength exercise"
            ExerciseType.CARDIO -> "Cardio exercise"
            ExerciseType.MOBILITY -> "Mobility exercise"
            ExerciseType.OTHER -> "General exercise"
        }
    }

    private fun ExerciseType.sortOrder(): Int {
        return when (this) {
            ExerciseType.STRENGTH -> 0
            ExerciseType.CARDIO -> 1
            ExerciseType.MOBILITY -> 2
            ExerciseType.OTHER -> 3
        }
    }
}