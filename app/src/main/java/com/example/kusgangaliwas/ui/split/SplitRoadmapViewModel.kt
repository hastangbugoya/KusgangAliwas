package com.example.kusgangaliwas.ui.split

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kusgangaliwas.data.local.entity.ExerciseEntity
import com.example.kusgangaliwas.data.local.entity.SplitTemplateExerciseEntity
import com.example.kusgangaliwas.domain.repository.ExerciseRepository
import com.example.kusgangaliwas.domain.repository.SplitTemplateRepository
import com.example.kusgangaliwas.domain.usecase.split.AddExerciseToSplitUseCase
import com.example.kusgangaliwas.domain.usecase.split.GetSplitRoadmapUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SplitRoadmapViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val splitTemplateRepository: SplitTemplateRepository,
    private val exerciseRepository: ExerciseRepository,
    private val addExerciseToSplitUseCase: AddExerciseToSplitUseCase,
    getSplitRoadmapUseCase: GetSplitRoadmapUseCase,
) : ViewModel() {

    private val splitId: Long = checkNotNull(
        savedStateHandle.get<Long>("splitId")
    ) {
        "Missing splitId."
    }
    val uiState: StateFlow<SplitRoadmapUiState> =
        combine(
            getSplitRoadmapUseCase(splitId),
            exerciseRepository.observeActiveExercises(),
        ) { roadmap, exercises ->
            val exerciseById = exercises.associateBy { it.id }

            SplitRoadmapUiState(
                splitId = splitId,
                roadmapItems = roadmap.map { item ->
                    SplitRoadmapItemUiState(
                        splitTemplateExercise = item,
                        exerciseName = exerciseById[item.exerciseId]?.name ?: "Unknown exercise",
                    )
                },
                availableExercises = exercises,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SplitRoadmapUiState(splitId = splitId),
        )

    init {
        viewModelScope.launch {
            val split = splitTemplateRepository.getSplitById(splitId)
            // We will wire split name into state more cleanly after the screen is visible.
        }
    }

    fun addExerciseToSplit(exerciseId: Long) {
        viewModelScope.launch {
            runCatching {
                addExerciseToSplitUseCase(
                    splitTemplateId = splitId,
                    exerciseId = exerciseId,
                )
            }
        }
    }
}