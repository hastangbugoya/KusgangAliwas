package com.example.kusgangaliwas.ui.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kusgangaliwas.data.local.entity.ActualExerciseSetLogEntity
import com.example.kusgangaliwas.data.local.entity.ExerciseEntity
import com.example.kusgangaliwas.data.local.entity.ExerciseMuscleEmphasis
import com.example.kusgangaliwas.data.local.entity.ExerciseMuscleGroupCrossRef
import com.example.kusgangaliwas.data.local.entity.ExercisePaceProfileEntity
import com.example.kusgangaliwas.data.local.entity.ExercisePrEntity
import com.example.kusgangaliwas.data.local.entity.ExercisePrType
import com.example.kusgangaliwas.data.local.entity.ExerciseType
import com.example.kusgangaliwas.data.local.entity.MuscleGroupEntity
import com.example.kusgangaliwas.domain.repository.ExercisePaceProfileRepository
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ExerciseListUiState(
    val exercises: List<ExerciseListItemUiState> = emptyList(),
    val availableMuscleGroups: List<MuscleGroupEntity> = emptyList(),
    val errorMessage: String? = null,
    val selectedFilterMuscleGroupIds: Set<Long> = emptySet(),
    val searchQuery: String = "",
)

data class ExerciseListItemUiState(
    val exercise: ExerciseEntity,
    val lastLogDateText: String? = null,
    val lastSetSummaryText: String? = null,
    val estimatedOneRepMaxText: String? = null,
    val actualOneRepMaxText: String? = null,
    val actualOneRepMaxDateText: String? = null,
    val latestMaxWeightText: String? = null,
    val selectedMuscleGroupIds: Set<Long> = emptySet(),
    val paceProfiles: List<ExercisePaceProfileEntity> = emptyList(),
    val historyPoints: List<ExerciseHistoryPointUiState> = emptyList(),
    val historyTrendText: String? = null,
)

data class ExerciseHistoryPointUiState(
    val label: String,
    val maxWeight: Double,
)

@HiltViewModel
class ExerciseListViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val exercisePaceProfileRepository: ExercisePaceProfileRepository,
    private val sessionRepository: SessionRepository,
    private val createExerciseUseCase: CreateExerciseUseCase,
    private val getEstimatedOneRepMaxUseCase: GetEstimatedOneRepMaxUseCase,
) : ViewModel() {

    private val refreshSignal = MutableStateFlow(0)

    private val errorMessage = MutableStateFlow<String?>(null)

    private val selectedFilterMuscleGroupIds = MutableStateFlow<Set<Long>>(emptySet())

    private val searchQuery = MutableStateFlow("")

    val uiState: StateFlow<ExerciseListUiState> =
        combine(
            combine(
                exerciseRepository.observeActiveExercises(),
                exerciseRepository.observeActiveMuscleGroups(),
                selectedFilterMuscleGroupIds,
                searchQuery,
                refreshSignal,
            ) { exercises, muscleGroups, selectedFilters, query, _ ->
                val listItems = exercises.map { exercise ->
                    buildExerciseListItem(exercise)
                }

                val filteredItems =
                    if (selectedFilters.isEmpty()) {
                        listItems
                    } else {
                        listItems.filter { item ->
                            item.selectedMuscleGroupIds.any { id ->
                                selectedFilters.contains(id)
                            }
                        }
                    }

                val searchFilteredItems =
                    if (query.isBlank()) {
                        filteredItems
                    } else {
                        filteredItems.filter { item ->
                            item.exercise.name.contains(
                                query.trim(),
                                ignoreCase = true,
                            )
                        }
                    }

                ExerciseListUiState(
                    exercises = searchFilteredItems,
                    availableMuscleGroups = muscleGroups,
                    selectedFilterMuscleGroupIds = selectedFilters,
                    searchQuery = query,
                )
            },
            errorMessage,
        ) { state, error ->
            state.copy(
                errorMessage = error,
            )
        }.stateIn(
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

    fun toggleMuscleGroupForExercise(
        exerciseId: Long,
        muscleGroupId: Long,
        isSelected: Boolean,
    ) {
        viewModelScope.launch {
            if (isSelected) {
                exerciseRepository.deleteExerciseMuscleGroupMapping(
                    exerciseId = exerciseId,
                    muscleGroupId = muscleGroupId,
                )
            } else {
                exerciseRepository.upsertExerciseMuscleGroup(
                    ExerciseMuscleGroupCrossRef(
                        exerciseId = exerciseId,
                        muscleGroupId = muscleGroupId,
                        emphasis = ExerciseMuscleEmphasis.SECONDARY,
                    )
                )
            }

            refreshSignal.update { it + 1 }
        }
    }

    fun saveActualOneRepMax(
        exerciseId: Long,
        value: Double,
        achievedAtEpochMillis: Long,
        notes: String? = null,
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()

            exerciseRepository.upsertExercisePr(
                ExercisePrEntity(
                    exerciseId = exerciseId,
                    prType = ExercisePrType.ACTUAL_1RM,
                    value = value,
                    achievedAtEpochMillis = achievedAtEpochMillis,
                    notes = notes?.takeIf { it.isNotBlank() },
                    createdAtEpochMillis = now,
                    updatedAtEpochMillis = now,
                )
            )

            refreshSignal.update { it + 1 }
        }
    }

    fun deleteActualOneRepMax(
        exerciseId: Long,
    ) {
        viewModelScope.launch {
            exerciseRepository.deletePrForExercise(
                exerciseId = exerciseId,
                prType = ExercisePrType.ACTUAL_1RM,
            )

            refreshSignal.update { it + 1 }
        }
    }

    fun createPaceProfile(
        exerciseId: Long,
        name: String,
        isDefault: Boolean = false,
        isEnabled: Boolean = true,
        prepLeadSeconds: Int = 0,
        expectedWorkSeconds: Int = 0,
        expectedRestSeconds: Int = 0,
        nextSetWarningSeconds: Int = 0,
        idleReminderIntervalSeconds: Int = 0,
        idleReminderEnabled: Boolean = false,
        etiquetteReminderEnabled: Boolean = false,
    ) {
        viewModelScope.launch {
            val cleanedName = cleanPaceProfileName(name) ?: return@launch
            val now = System.currentTimeMillis()

            runCatching {
                val existingProfile = exercisePaceProfileRepository
                    .getProfileForExerciseByName(
                        exerciseId = exerciseId,
                        name = cleanedName,
                    )

                require(existingProfile == null) {
                    "This exercise already has a pace profile named \"$cleanedName\"."
                }

                exercisePaceProfileRepository.insertProfile(
                    ExercisePaceProfileEntity(
                        exerciseId = exerciseId,
                        name = cleanedName,
                        isDefault = isDefault,
                        isEnabled = isEnabled,
                        prepLeadSeconds = sanitizeSeconds(prepLeadSeconds),
                        expectedWorkSeconds = sanitizeSeconds(expectedWorkSeconds),
                        expectedRestSeconds = sanitizeSeconds(expectedRestSeconds),
                        nextSetWarningSeconds = sanitizeSeconds(nextSetWarningSeconds),
                        idleReminderIntervalSeconds = sanitizeSeconds(
                            idleReminderIntervalSeconds,
                        ),
                        idleReminderEnabled = idleReminderEnabled,
                        etiquetteReminderEnabled = etiquetteReminderEnabled,
                        createdAtEpochMillis = now,
                        updatedAtEpochMillis = now,
                    )
                )
            }.onSuccess {
                errorMessage.value = null
                refreshSignal.update { it + 1 }
            }.onFailure { error ->
                errorMessage.value = error.message ?: "Could not save pace profile."
                error.printStackTrace()
            }
        }
    }

    fun updatePaceProfile(
        profile: ExercisePaceProfileEntity,
        name: String = profile.name,
        isDefault: Boolean = profile.isDefault,
        isEnabled: Boolean = profile.isEnabled,
        prepLeadSeconds: Int = profile.prepLeadSeconds,
        expectedWorkSeconds: Int = profile.expectedWorkSeconds,
        expectedRestSeconds: Int = profile.expectedRestSeconds,
        nextSetWarningSeconds: Int = profile.nextSetWarningSeconds,
        idleReminderIntervalSeconds: Int = profile.idleReminderIntervalSeconds,
        idleReminderEnabled: Boolean = profile.idleReminderEnabled,
        etiquetteReminderEnabled: Boolean = profile.etiquetteReminderEnabled,
    ) {
        viewModelScope.launch {
            val cleanedName = cleanPaceProfileName(name) ?: return@launch

            runCatching {
                val existingProfile = exercisePaceProfileRepository
                    .getProfileForExerciseByName(
                        exerciseId = profile.exerciseId,
                        name = cleanedName,
                    )

                require(existingProfile == null || existingProfile.id == profile.id) {
                    "This exercise already has a pace profile named \"$cleanedName\"."
                }

                exercisePaceProfileRepository.updateProfile(
                    profile.copy(
                        name = cleanedName,
                        isDefault = isDefault,
                        isEnabled = isEnabled,
                        prepLeadSeconds = sanitizeSeconds(prepLeadSeconds),
                        expectedWorkSeconds = sanitizeSeconds(expectedWorkSeconds),
                        expectedRestSeconds = sanitizeSeconds(expectedRestSeconds),
                        nextSetWarningSeconds = sanitizeSeconds(nextSetWarningSeconds),
                        idleReminderIntervalSeconds = sanitizeSeconds(
                            idleReminderIntervalSeconds,
                        ),
                        idleReminderEnabled = idleReminderEnabled,
                        etiquetteReminderEnabled = etiquetteReminderEnabled,
                        updatedAtEpochMillis = System.currentTimeMillis(),
                    )
                )
            }.onSuccess {
                errorMessage.value = null
                refreshSignal.update { it + 1 }
            }.onFailure { error ->
                errorMessage.value = error.message ?: "Could not update pace profile."
                error.printStackTrace()
            }
        }
    }

    fun setPaceProfileAsDefault(
        profile: ExercisePaceProfileEntity,
    ) {
        viewModelScope.launch {
            runCatching {
                exercisePaceProfileRepository.setDefaultProfileForExercise(
                    exerciseId = profile.exerciseId,
                    profileId = profile.id,
                    updatedAtEpochMillis = System.currentTimeMillis(),
                )
            }

            refreshSignal.update { it + 1 }
        }
    }

    fun togglePaceProfileEnabled(
        profile: ExercisePaceProfileEntity,
    ) {
        updatePaceProfile(
            profile = profile,
            isEnabled = !profile.isEnabled,
        )
    }

    fun deletePaceProfile(
        profile: ExercisePaceProfileEntity,
    ) {
        viewModelScope.launch {
            runCatching {
                exercisePaceProfileRepository.deleteProfile(profile)
            }

            refreshSignal.update { it + 1 }
        }
    }

    private suspend fun buildExerciseListItem(
        exercise: ExerciseEntity,
    ): ExerciseListItemUiState {
        val selectedMuscleGroupIds = exerciseRepository
            .observeMuscleGroupsForExercise(exercise.id)
            .first()
            .map { crossRef -> crossRef.muscleGroupId }
            .toSet()

        val paceProfiles = exercisePaceProfileRepository
            .getProfilesForExercise(exercise.id)

        return when (exercise.exerciseType) {

            ExerciseType.CARDIO -> {
                val suggestion = sessionRepository
                    .getLatestCardioSuggestionForExercise(exercise.id)

                if (suggestion == null) {
                    ExerciseListItemUiState(
                        exercise = exercise,
                        selectedMuscleGroupIds = selectedMuscleGroupIds,
                        paceProfiles = paceProfiles,
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
                        selectedMuscleGroupIds = selectedMuscleGroupIds,
                        paceProfiles = paceProfiles,
                    )
                }
            }

            else -> {
                val latestLog = sessionRepository
                    .getLogsForExercise(exercise.id)
                    .firstOrNull()

                val actualOneRepMax = exerciseRepository.getPrForExercise(
                    exerciseId = exercise.id,
                    prType = ExercisePrType.ACTUAL_1RM,
                )

                val estimatedOneRepMax = withContext(Dispatchers.IO) {
                    getEstimatedOneRepMaxUseCase(exercise.id)
                }

                val historyPoints = buildHistoryPoints(exercise.id)
                val historyTrendText = buildHistoryTrendText(historyPoints)

                if (latestLog == null) {
                    ExerciseListItemUiState(
                        exercise = exercise,
                        estimatedOneRepMaxText = estimatedOneRepMax?.let {
                            "Estimated 1RM: ${formatWeight(it)}"
                        },
                        actualOneRepMaxText = actualOneRepMax?.let {
                            "Actual 1RM: ${formatWeight(it.value)}"
                        },
                        actualOneRepMaxDateText = actualOneRepMax?.let {
                            "Actual 1RM date: ${formatDate(it.achievedAtEpochMillis)}"
                        },
                        selectedMuscleGroupIds = selectedMuscleGroupIds,
                        paceProfiles = paceProfiles,
                        historyPoints = historyPoints,
                        historyTrendText = historyTrendText,
                    )
                } else {
                    val sets = sessionRepository.getSetsForExercise(latestLog.id)
                    val latestMaxWeight = sets.mapNotNull { it.weight }.maxOrNull()

                    ExerciseListItemUiState(
                        exercise = exercise,
                        lastLogDateText = latestLog.performedAtEpochMillis?.let { epochMillis ->
                            "Last: ${formatDate(epochMillis)}"
                        } ?: "Last: logged",
                        lastSetSummaryText = buildSetSummary(sets),
                        latestMaxWeightText = latestMaxWeight?.let {
                            "Latest max: ${formatWeight(it)}"
                        },
                        estimatedOneRepMaxText = estimatedOneRepMax?.let {
                            "Estimated 1RM: ${formatWeight(it)}"
                        },
                        actualOneRepMaxText = actualOneRepMax?.let {
                            "Actual 1RM: ${formatWeight(it.value)}"
                        },
                        actualOneRepMaxDateText = actualOneRepMax?.let {
                            "Actual 1RM date: ${formatDate(it.achievedAtEpochMillis)}"
                        },
                        selectedMuscleGroupIds = selectedMuscleGroupIds,
                        paceProfiles = paceProfiles,
                        historyPoints = historyPoints,
                        historyTrendText = historyTrendText,
                    )
                }
            }
        }
    }

    fun toggleFilterMuscleGroup(
        muscleGroupId: Long,
    ) {
        selectedFilterMuscleGroupIds.update { current ->
            if (current.contains(muscleGroupId)) {
                current - muscleGroupId
            } else {
                current + muscleGroupId
            }
        }
    }

    fun clearMuscleGroupFilters() {
        selectedFilterMuscleGroupIds.value = emptySet()
    }

    fun onSearchQueryChange(
        value: String,
    ) {
        searchQuery.value = value
    }

    private suspend fun buildHistoryPoints(
        exerciseId: Long,
    ): List<ExerciseHistoryPointUiState> {
        return sessionRepository
            .getLogsForExercise(exerciseId)
            .take(10)
            .mapNotNull { log ->
                val maxWeight = sessionRepository
                    .getSetsForExercise(log.id)
                    .mapNotNull { set -> set.weight }
                    .maxOrNull()

                maxWeight?.let {
                    ExerciseHistoryPointUiState(
                        label = log.performedAtEpochMillis?.let(::formatShortDate) ?: "Log",
                        maxWeight = it,
                    )
                }
            }
            .asReversed()
    }

    private fun buildHistoryTrendText(
        points: List<ExerciseHistoryPointUiState>,
    ): String? {
        if (points.size < 2) {
            return null
        }

        val first = points.first().maxWeight
        val last = points.last().maxWeight
        val delta = last - first

        return when {
            delta > 0.0 -> "Trend: improving"
            delta < 0.0 -> "Trend: declining"
            else -> "Trend: stable"
        }
    }

    private fun formatShortDate(
        epochMillis: Long,
    ): String {
        return Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("M/d"))
    }

    private fun buildSetSummary(
        sets: List<ActualExerciseSetLogEntity>,
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

    fun renameExercise(
        exercise: ExerciseEntity,
        newName: String,
    ) {
        viewModelScope.launch {
            val cleaned = newName
                .trim()
                .replaceFirstChar { character ->
                    character.uppercase()
                }

            if (cleaned.isBlank()) return@launch

            runCatching {
                exerciseRepository.updateExercise(
                    exercise.copy(
                        name = cleaned,
                        updatedAtEpochMillis = System.currentTimeMillis(),
                    )
                )
            }
        }
    }

    fun createMuscleGroup(
        name: String,
    ) {
        viewModelScope.launch {
            val cleaned = name
                .trim()
                .replaceFirstChar { character ->
                    character.uppercase()
                }

            if (cleaned.isBlank()) return@launch

            runCatching {
                exerciseRepository.insertMuscleGroup(
                    MuscleGroupEntity(
                        name = cleaned,
                        sortOrder = uiState.value.availableMuscleGroups.size,
                        isActive = true,
                    )
                )
            }
        }
    }

    fun renameMuscleGroup(
        muscleGroup: MuscleGroupEntity,
        newName: String,
    ) {
        viewModelScope.launch {
            val cleaned = newName
                .trim()
                .replaceFirstChar { character ->
                    character.uppercase()
                }

            if (cleaned.isBlank()) return@launch

            runCatching {
                exerciseRepository.updateMuscleGroup(
                    muscleGroup.copy(
                        name = cleaned,
                    )
                )
            }
        }
    }

    fun deleteMuscleGroup(
        muscleGroupId: Long,
    ) {
        viewModelScope.launch {
            runCatching {
                exerciseRepository.softDeleteMuscleGroup(muscleGroupId)

                selectedFilterMuscleGroupIds.update { current ->
                    current - muscleGroupId
                }
            }
        }
    }

    private fun cleanPaceProfileName(
        value: String,
    ): String? {
        val cleaned = value.trim()

        if (cleaned.isBlank()) {
            return null
        }

        return cleaned.replaceFirstChar { character ->
            character.uppercase()
        }
    }

    private fun sanitizeSeconds(
        value: Int,
    ): Int {
        return value.coerceAtLeast(0)
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