package com.example.kusgangaliwas.ui.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kusgangaliwas.data.local.entity.ExerciseEntity
import com.example.kusgangaliwas.data.local.entity.ExerciseType
import com.example.kusgangaliwas.domain.repository.ExerciseRepository
import com.example.kusgangaliwas.domain.repository.SessionRepository
import com.example.kusgangaliwas.domain.usecase.exercise.CreateExerciseUseCase
import com.example.kusgangaliwas.domain.usecase.exercise.GetEstimatedOneRepMaxUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ExerciseListUiState(
    val exercises: List<ExerciseListItemUiState> = emptyList(),
    val errorMessage: String? = null,
)

data class ExerciseListItemUiState(
    val exercise: ExerciseEntity,
    val lastLogDateText: String? = null,
    val lastSetSummaryText: String? = null,
)

@HiltViewModel
class ExerciseListViewModel @Inject constructor(
    exerciseRepository: ExerciseRepository,
    private val sessionRepository: SessionRepository,
    private val createExerciseUseCase: CreateExerciseUseCase,
    private val getEstimatedOneRepMaxUseCase: GetEstimatedOneRepMaxUseCase,
) : ViewModel() {

    private val oneRepMaxMap = mutableMapOf<Long, Double?>()

    val uiState: StateFlow<ExerciseListUiState> =
        exerciseRepository
            .observeActiveExercises()
            .map { exercises ->
                ExerciseListUiState(
                    exercises = exercises.map { exercise ->
                        buildExerciseListItem(exercise)
                    },
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ExerciseListUiState(),
            )

    fun createExercise(
        name: String,
        exerciseType: ExerciseType,
    ) {
        viewModelScope.launch {
            runCatching {
                createExerciseUseCase(
                    name = name,
                    exerciseType = exerciseType,
                )
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

    private suspend fun buildExerciseListItem(
        exercise: ExerciseEntity,
    ): ExerciseListItemUiState {
        return when (exercise.exerciseType) {

            ExerciseType.CARDIO -> {
                val suggestion = sessionRepository
                    .getLatestCardioSuggestionForExercise(exercise.id)

                if (suggestion == null) {
                    ExerciseListItemUiState(
                        exercise = exercise,
                    )
                } else {

                    val cardioParts = buildList {
                        suggestion.distance?.let { distance ->
                            suggestion.distanceUnit?.let { unit ->
                                add("${formatWeight(distance)} $unit")
                            }
                        }

                        suggestion.durationSeconds?.let { seconds ->
                            add("${seconds / 60}m")
                        }

                        suggestion.averageInclinePercent?.let { incline ->
                            add("${formatWeight(incline)}% incline")
                        }

                        suggestion.averageResistance?.let { resistance ->
                            add("resistance ${formatWeight(resistance)}")
                        }
                    }

                    ExerciseListItemUiState(
                        exercise = exercise,
                        lastLogDateText =
                            "Last: ${formatEpochDay(suggestion.sourcePerformedDateEpochDay)}",
                        lastSetSummaryText =
                            if (cardioParts.isEmpty()) {
                                "Cardio logged"
                            } else {
                                cardioParts.joinToString(" · ")
                            },
                    )
                }
            }

            else -> {
                val latestLog = sessionRepository
                    .getLogsForExercise(exercise.id)
                    .firstOrNull()

                if (latestLog == null) {
                    ExerciseListItemUiState(
                        exercise = exercise,
                    )
                } else {
                    val sets = sessionRepository.getSetsForExercise(latestLog.id)

                    ExerciseListItemUiState(
                        exercise = exercise,
                        lastLogDateText = latestLog.performedAtEpochMillis?.let { epochMillis ->
                            "Last: ${formatDate(epochMillis)}"
                        } ?: "Last: logged",
                        lastSetSummaryText = buildSetSummary(sets),
                    )
                }
            }
        }
    }

    private fun buildSetSummary(
        sets: List<com.example.kusgangaliwas.data.local.entity.ActualExerciseSetLogEntity>,
    ): String {
        if (sets.isEmpty()) {
            return "Sets: 0"
        }

        val setTexts = sets.map { set ->
            val weight = set.weight?.let(::formatWeight) ?: "-"
            val reps = set.reps?.toString() ?: "-"
            "${weight}x$reps"
        }

        return "Sets: ${sets.size} · ${setTexts.joinToString(" · ")}"
    }

    private fun formatEpochDay(
        epochDay: Long,
    ): String {
        return Instant
            .ofEpochSecond(epochDay * 24L * 60L * 60L)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    }

    private fun formatDate(
        epochMillis: Long,
    ): String {
        return Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    }

    private fun formatWeight(
        value: Double,
    ): String {
        return if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            value.toString()
        }
    }
}