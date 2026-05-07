package com.example.kusgangaliwas.ui.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kusgangaliwas.data.local.entity.ExerciseEntity
import com.example.kusgangaliwas.domain.repository.ExerciseRepository
import com.example.kusgangaliwas.domain.usecase.exercise.CreateExerciseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.kusgangaliwas.domain.usecase.exercise.GetEstimatedOneRepMaxUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ExerciseListUiState(
    val exercises: List<ExerciseEntity> = emptyList(),
    val errorMessage: String? = null,
)

@HiltViewModel
class ExerciseListViewModel @Inject constructor(
    exerciseRepository: ExerciseRepository,
    private val createExerciseUseCase: CreateExerciseUseCase,
    private val getEstimatedOneRepMaxUseCase: GetEstimatedOneRepMaxUseCase,
) : ViewModel() {

    private val oneRepMaxMap = mutableMapOf<Long, Double?>()

    val uiState: StateFlow<ExerciseListUiState> =
        exerciseRepository
            .observeActiveExercises()
            .map { exercises ->
                ExerciseListUiState(
                    exercises = exercises,
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ExerciseListUiState(),
            )

    fun createExercise(name: String) {
        viewModelScope.launch {
            runCatching {
                createExerciseUseCase(name = name)
            }.onFailure {
                // Basic for now. Later we can expose one-shot snackbar events.
            }
        }
    }

    fun getOneRepMax(exerciseId: Long): Double? {
        return oneRepMaxMap[exerciseId]
    }

    fun loadOneRepMax(exerciseId: Long) {
        if (oneRepMaxMap.containsKey(exerciseId)) return

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                getEstimatedOneRepMaxUseCase(exerciseId)
            }
            oneRepMaxMap[exerciseId] = result
        }
    }
}