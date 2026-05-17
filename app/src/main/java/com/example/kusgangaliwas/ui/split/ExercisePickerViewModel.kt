package com.example.kusgangaliwas.ui.split

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kusgangaliwas.data.local.entity.ExerciseType
import com.example.kusgangaliwas.domain.repository.ExerciseRepository
import com.example.kusgangaliwas.domain.repository.SplitTemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ExercisePickerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val splitTemplateRepository: SplitTemplateRepository,
    exerciseRepository: ExerciseRepository,
) : ViewModel() {

    private val splitId: Long = checkNotNull(
        savedStateHandle.get<Long>("splitId")
    ) {
        "Missing splitId."
    }

    private val searchText = MutableStateFlow("")
    private val selectedExerciseIds = MutableStateFlow<Set<Long>>(emptySet())

    private val splitName = MutableStateFlow("")

    val uiState: StateFlow<ExercisePickerUiState> =
        combine(
            exerciseRepository.observeActiveExercises(),
            splitTemplateRepository.observeExercisesForSplit(splitId),
            searchText,
            selectedExerciseIds,
            splitName,
        ) { exercises, splitExercises, search, selectedIds, currentSplitName  ->

            val existingExerciseIds = splitExercises
                .map { it.exerciseId }
                .toSet()

            val normalizedSearch = search.trim()

            val filteredExercises = exercises
                .filter { exercise ->
                    normalizedSearch.isBlank() ||
                            exercise.name.contains(
                                normalizedSearch,
                                ignoreCase = true,
                            )
                }
                .sortedBy { it.name.lowercase() }

            ExercisePickerUiState(
                splitId = splitId,
                searchText = search,
                exercises = filteredExercises.map { exercise ->
                    ExercisePickerItem(
                        exerciseId = exercise.id,
                        exerciseName = exercise.name,
                        exerciseTypeLabel =
                            exercise.exerciseType.displayText(),
                        alreadyInSplit =
                            exercise.id in existingExerciseIds,
                    )
                },
                selectedExerciseIds = selectedIds,
                splitName = currentSplitName
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ExercisePickerUiState(
                splitId = splitId,
            ),
        )

    fun onSearchTextChange(value: String) {
        searchText.value = value
    }

    fun toggleExercise(exerciseId: Long) {
        val currentState = uiState.value
        val item = currentState.exercises.firstOrNull {
            it.exerciseId == exerciseId
        } ?: return

        if (item.alreadyInSplit) {
            return
        }

        selectedExerciseIds.value =
            if (exerciseId in selectedExerciseIds.value) {
                selectedExerciseIds.value - exerciseId
            } else {
                selectedExerciseIds.value + exerciseId
            }
    }

    fun addSelectedExercises(
        onDone: () -> Unit,
    ) {
        val idsToAdd = selectedExerciseIds.value

        if (idsToAdd.isEmpty()) {
            onDone()
            return
        }

        viewModelScope.launch {
            runCatching {
                val currentCount = splitTemplateRepository
                    .getExercisesForSplit(splitId)
                    .size

                idsToAdd.forEachIndexed { index, exerciseId ->
                    splitTemplateRepository.insertSplitExercise(
                        com.example.kusgangaliwas.data.local.entity.SplitTemplateExerciseEntity(
                            splitTemplateId = splitId,
                            exerciseId = exerciseId,
                            suggestedOrder = currentCount + index,
                        )
                    )
                }

                selectedExerciseIds.value = emptySet()
                onDone()
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    private fun ExerciseType.displayText(): String {
        return when (this) {
            ExerciseType.STRENGTH -> "Strength"
            ExerciseType.CARDIO -> "Cardio"
            ExerciseType.MOBILITY -> "Mobility"
            ExerciseType.OTHER -> "Other"
        }
    }

    init {
        viewModelScope.launch {
            val split = splitTemplateRepository.getSplitById(splitId)
            splitName.value = split?.name.orEmpty()
        }
    }
}