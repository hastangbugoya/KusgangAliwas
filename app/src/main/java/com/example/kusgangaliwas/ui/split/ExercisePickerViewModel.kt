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
import com.example.kusgangaliwas.data.local.entity.ExerciseEntity
import com.example.kusgangaliwas.data.local.entity.MuscleGroupEntity
import com.example.kusgangaliwas.data.local.entity.SplitTemplateExerciseEntity
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.ExperimentalCoroutinesApi


private data class ExercisePickerSourceState(
    val exercises: List<ExerciseEntity>,
    val splitExercises: List<SplitTemplateExerciseEntity>,
    val muscleGroups: List<MuscleGroupEntity>,
    val muscleGroupIdsByExerciseId: Map<Long, Set<Long>>,
)

@OptIn(ExperimentalCoroutinesApi::class)
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
    private val selectedMuscleGroupIds = MutableStateFlow<Set<Long>>(emptySet())
    private val selectedExerciseIds = MutableStateFlow<Set<Long>>(emptySet())


    private val title = MutableStateFlow("")

    private val muscleGroupIdsByExerciseId =
        exerciseRepository.observeActiveExercises()
            .flatMapLatest { exercises ->
                if (exercises.isEmpty()) {
                    flowOf(emptyMap())
                } else {
                    combine(
                        exercises.map { exercise ->
                            exerciseRepository.observeMuscleGroupsForExercise(exercise.id)
                                .map { mappings ->
                                    exercise.id to mappings
                                        .map { mapping -> mapping.muscleGroupId }
                                        .toSet()
                                }
                        }
                    ) { pairs ->
                        pairs.toMap()
                    }
                }
            }

    private val sourceState =
        combine(
            exerciseRepository.observeActiveExercises(),
            splitTemplateRepository.observeExercisesForSplit(splitId),
            exerciseRepository.observeActiveMuscleGroups(),
            muscleGroupIdsByExerciseId,
        ) { exercises, splitExercises, muscleGroups, muscleGroupIdsByExerciseId ->
            ExercisePickerSourceState(
                exercises = exercises,
                splitExercises = splitExercises,
                muscleGroups = muscleGroups.sortedBy { it.name.lowercase() },
                muscleGroupIdsByExerciseId = muscleGroupIdsByExerciseId,
            )
        }

    val uiState: StateFlow<ExercisePickerUiState> =
        combine(
            sourceState,
            searchText,
            selectedMuscleGroupIds,
            selectedExerciseIds,
            title,
        ) { source, search, selectedMuscleIds, selectedIds, currentTitle ->

            val existingExerciseIds = source.splitExercises
                .map { it.exerciseId }
                .toSet()

            val muscleGroupNameById = source.muscleGroups.associateBy { it.id }

            val normalizedSearch = search.trim()

            val filteredExercises = source.exercises
                .filter { exercise ->
                    normalizedSearch.isBlank() ||
                            exercise.name.contains(
                                normalizedSearch,
                                ignoreCase = true,
                            )
                }
                .filter { exercise ->
                    selectedMuscleIds.isEmpty() ||
                            source.muscleGroupIdsByExerciseId[exercise.id]
                                .orEmpty()
                                .any { muscleGroupId ->
                                    muscleGroupId in selectedMuscleIds
                                }
                }
                .sortedBy { it.name.lowercase() }

            ExercisePickerUiState(
                title = currentTitle,
                searchText = search,
                availableMuscleGroups = source.muscleGroups,
                selectedMuscleGroupIds = selectedMuscleIds,
                exercises = filteredExercises.map { exercise ->
                    val muscleGroupIds = source.muscleGroupIdsByExerciseId[exercise.id]
                        .orEmpty()

                    val muscleText = muscleGroupIds
                        .mapNotNull { muscleGroupId ->
                            muscleGroupNameById[muscleGroupId]?.name
                        }
                        .sortedWith(String.CASE_INSENSITIVE_ORDER)
                        .takeIf { it.isNotEmpty() }
                        ?.joinToString(", ")

                    ExercisePickerItem(
                        exerciseId = exercise.id,
                        exerciseName = exercise.name,
                        exerciseTypeLabel = exercise.exerciseType.displayText(),
                        supportingText = listOfNotNull(
                            when (exercise.exerciseType) {
                                ExerciseType.STRENGTH -> "Strength exercise"
                                ExerciseType.CARDIO -> "Cardio exercise"
                                ExerciseType.MOBILITY -> "Mobility exercise"
                                ExerciseType.OTHER -> "General exercise"
                            },
                            muscleText,
                        ).joinToString(" · "),
                        muscleGroupIds = muscleGroupIds,
                        alreadySelected = exercise.id in existingExerciseIds,
                    )
                },
                selectedExerciseIds = selectedIds,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ExercisePickerUiState(),
        )

    fun onSearchTextChange(value: String) {
        searchText.value = value
    }

    fun clearMuscleGroupSelection() {
        selectedMuscleGroupIds.value = emptySet()
    }

    fun toggleExercise(exerciseId: Long) {
        val currentState = uiState.value
        val item = currentState.exercises.firstOrNull {
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

    fun toggleMuscleGroup(
        muscleGroupId: Long,
        currentlySelected: Boolean,
    ) {
        selectedMuscleGroupIds.value =
            if (currentlySelected) {
                selectedMuscleGroupIds.value - muscleGroupId
            } else {
                selectedMuscleGroupIds.value + muscleGroupId
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
            title.value = "Add to ${split?.name.orEmpty().ifBlank { "Split" }}"
        }
    }
}