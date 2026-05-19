package com.example.kusgangaliwas.ui.cycle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kusgangaliwas.data.local.entity.SplitTemplateEntity
import com.example.kusgangaliwas.data.local.entity.TrainingCycleActivationEntity
import com.example.kusgangaliwas.data.local.entity.TrainingCycleEntity
import com.example.kusgangaliwas.data.local.entity.TrainingCycleStepEntity
import com.example.kusgangaliwas.data.local.model.TrainingCycleStepSummaryRow
import com.example.kusgangaliwas.domain.repository.SessionRepository
import com.example.kusgangaliwas.domain.repository.SplitTemplateRepository
import com.example.kusgangaliwas.domain.repository.TrainingCycleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class TrainingCycleViewModel @Inject constructor(
    private val trainingCycleRepository: TrainingCycleRepository,
    private val splitTemplateRepository: SplitTemplateRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val selectedCycleId = MutableStateFlow<Long?>(null)
    private val selectedCycleSteps =
        MutableStateFlow<List<TrainingCycleStepSummaryRow>>(emptyList())

    private val newCycleName = MutableStateFlow("")
    private val newCycleNotes = MutableStateFlow("")
    private val isCreateCycleExpanded = MutableStateFlow(false)

    val uiState: StateFlow<TrainingCycleUiState> =
        combine(
            combine(
                trainingCycleRepository.observeAllCycles(),
                splitTemplateRepository.observeActiveSplits(),
                selectedCycleId,
                selectedCycleSteps,
            ) { cycles, splits, selectedId, steps ->
                CycleBaseState(
                    cycles = cycles,
                    splits = splits,
                    selectedId = selectedId,
                    steps = steps,
                )
            },
            newCycleName,
            newCycleNotes,
            isCreateCycleExpanded,
        ) { base, name, notes, expanded ->

            val selectedCycle = base.cycles.firstOrNull { it.id == base.selectedId }
                ?: base.cycles.firstOrNull { it.isActive }
                ?: base.cycles.firstOrNull()

            val selectedStepSplitIds = base.steps
                .map { it.splitTemplateId }
                .toSet()

            TrainingCycleUiState(
                cycles = base.cycles.map { cycle ->
                    TrainingCycleListItem(
                        id = cycle.id,
                        name = cycle.name,
                        notes = cycle.notes,
                        isActive = cycle.isActive,
                        startedDateText = buildStartedDateText(cycle),
                        lastLoggedSessionDateText = buildLastLoggedSessionText(cycle),
                    )
                },
                selectedCycleId = selectedCycle?.id,
                selectedCycleName = selectedCycle?.name.orEmpty(),
                selectedCycleNotes = selectedCycle?.notes.orEmpty(),
                steps = base.steps
                    .sortedBy { it.stepOrder }
                    .map { step ->
                        TrainingCycleStepListItem(
                            id = step.stepId,
                            splitTemplateId = step.splitTemplateId,
                            splitName = step.splitName,
                            stepOrder = step.stepOrder,
                            warnBeforeMarkDone = step.warnBeforeMarkDone,
                            notes = null,
                            muscleGroupsText = step.muscleGroupsText,
                            strengthExerciseCount = step.strengthExerciseCount,
                            cardioExerciseCount = step.cardioExerciseCount,
                            totalExerciseCount = step.totalExerciseCount,
                        )
                    },
                availableSplits = base.splits.map { split ->
                    TrainingCycleSplitOption(
                        splitTemplateId = split.id,
                        splitName = split.name,
                        alreadyInSelectedCycle =
                            split.id in selectedStepSplitIds,
                    )
                },
                newCycleName = name,
                newCycleNotes = notes,
                isCreateCycleExpanded = expanded,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TrainingCycleUiState(),
        )

    init {
        viewModelScope.launch {
            trainingCycleRepository.observeAllCycles()
                .collect { cycles ->
                    val currentSelectedId = selectedCycleId.value
                    val selectedStillExists = cycles.any {
                        it.id == currentSelectedId
                    }

                    if (!selectedStillExists) {
                        selectedCycleId.value = cycles
                            .firstOrNull { it.isActive }
                            ?.id
                            ?: cycles.firstOrNull()?.id
                    }

                    refreshSelectedSteps()
                }
        }
    }

    fun selectCycle(cycleId: Long) {
        selectedCycleId.value = cycleId
        refreshSelectedSteps()
    }

    fun onNewCycleNameChange(value: String) {
        newCycleName.value = value
    }

    fun onNewCycleNotesChange(value: String) {
        newCycleNotes.value = value
    }

    fun setCreateCycleExpanded(expanded: Boolean) {
        isCreateCycleExpanded.value = expanded
    }

    fun createCycle() {
        val name = newCycleName.value.trim()

        if (name.isBlank()) {
            return
        }

        viewModelScope.launch {
            runCatching {
                val now = System.currentTimeMillis()
                val today = LocalDate.now().toEpochDay()

                val cycleId = trainingCycleRepository.insertCycle(
                    TrainingCycleEntity(
                        name = name,
                        notes = newCycleNotes.value.trim()
                            .takeIf { it.isNotBlank() },
                        isActive = true,
                        createdAtEpochMillis = now,
                        updatedAtEpochMillis = now,
                    )
                )

                trainingCycleRepository.insertActivation(
                    TrainingCycleActivationEntity(
                        cycleId = cycleId,
                        activatedDateEpochDay = today,
                        deactivatedDateEpochDay = null,
                        notes = null,
                        createdAtEpochMillis = now,
                        updatedAtEpochMillis = now,
                    )
                )

                newCycleName.value = ""
                newCycleNotes.value = ""
                isCreateCycleExpanded.value = false
                selectedCycleId.value = cycleId
                refreshSelectedSteps()
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun addSplitToSelectedCycle(splitTemplateId: Long) {
        val cycleId = selectedCycleId.value
            ?: return

        viewModelScope.launch {
            runCatching {
                val currentSteps = trainingCycleRepository
                    .getStepsForCycle(cycleId)

                if (currentSteps.any { it.splitTemplateId == splitTemplateId }) {
                    return@runCatching
                }

                val nextOrder = (
                        currentSteps.maxOfOrNull { it.stepOrder } ?: -1
                        ) + 1

                trainingCycleRepository.insertStep(
                    TrainingCycleStepEntity(
                        cycleId = cycleId,
                        splitTemplateId = splitTemplateId,
                        stepOrder = nextOrder,
                    )
                )

                refreshSelectedSteps()
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun removeStep(stepId: Long) {
        val cycleId = selectedCycleId.value
            ?: return

        viewModelScope.launch {
            runCatching {
                trainingCycleRepository.deleteStep(stepId)

                val normalizedSteps = trainingCycleRepository
                    .getStepsForCycle(cycleId)
                    .sortedBy { it.stepOrder }
                    .mapIndexed { index, step ->
                        step.copy(stepOrder = index)
                    }

                trainingCycleRepository.updateSteps(normalizedSteps)
                refreshSelectedSteps()
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun moveStepUp(stepId: Long) {
        moveStep(stepId = stepId, direction = -1)
    }

    fun moveStepDown(stepId: Long) {
        moveStep(stepId = stepId, direction = 1)
    }

    fun setCycleActive(
        cycleId: Long,
        isActive: Boolean,
    ) {
        viewModelScope.launch {
            runCatching {
                val cycle = trainingCycleRepository.getCycleById(cycleId)
                    ?: return@runCatching

                val now = System.currentTimeMillis()
                val today = LocalDate.now().toEpochDay()

                trainingCycleRepository.updateCycle(
                    cycle.copy(
                        isActive = isActive,
                        updatedAtEpochMillis = now,
                    )
                )

                if (isActive) {
                    val activeActivation =
                        trainingCycleRepository.getActiveActivationForCycle(cycleId)

                    if (activeActivation == null) {
                        trainingCycleRepository.insertActivation(
                            TrainingCycleActivationEntity(
                                cycleId = cycleId,
                                activatedDateEpochDay = today,
                                deactivatedDateEpochDay = null,
                                notes = null,
                                createdAtEpochMillis = now,
                                updatedAtEpochMillis = now,
                            )
                        )
                    }
                } else {
                    trainingCycleRepository.deactivateActiveActivationForCycle(
                        cycleId = cycleId,
                        deactivatedDateEpochDay = today,
                        updatedAtEpochMillis = now,
                    )
                }
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun toggleWarnBeforeMarkDone(stepId: Long) {
        viewModelScope.launch {
            runCatching {
                val step = trainingCycleRepository.getStepById(stepId)
                    ?: return@runCatching

                trainingCycleRepository.updateStep(
                    step.copy(
                        warnBeforeMarkDone =
                            !step.warnBeforeMarkDone,
                    )
                )

                refreshSelectedSteps()
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun softDeleteSelectedCycle() {
        val cycleId = selectedCycleId.value
            ?: return

        viewModelScope.launch {
            runCatching {
                trainingCycleRepository.softDeleteCycle(
                    cycleId = cycleId,
                    updatedAtEpochMillis = System.currentTimeMillis(),
                )
                selectedCycleId.value = null
                refreshSelectedSteps()
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    private suspend fun buildStartedDateText(
        cycle: TrainingCycleEntity,
    ): String? {
        if (!cycle.isActive) {
            return null
        }

        val activation = trainingCycleRepository
            .getActiveActivationForCycle(cycle.id)
            ?: trainingCycleRepository.getLatestActivationForCycle(cycle.id)
            ?: return null

        return "Started: ${formatEpochDay(activation.activatedDateEpochDay)}"
    }

    private suspend fun buildLastLoggedSessionText(
        cycle: TrainingCycleEntity,
    ): String? {
        if (cycle.isActive) {
            return null
        }

        val latestSession = sessionRepository
            .getLatestCompletedCycleSession(cycle.id)

        return latestSession
            ?.let {
                "Last session: ${formatEpochDay(it.performedDateEpochDay)}"
            }
            ?: "No logged sessions yet"
    }

    private fun formatEpochDay(
        epochDay: Long,
    ): String {
        return LocalDate
            .ofEpochDay(epochDay)
            .format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    }

    private fun moveStep(
        stepId: Long,
        direction: Int,
    ) {
        val cycleId = selectedCycleId.value
            ?: return

        viewModelScope.launch {
            runCatching {
                val steps = trainingCycleRepository
                    .getStepsForCycle(cycleId)
                    .sortedBy { it.stepOrder }

                val index = steps.indexOfFirst { it.id == stepId }

                if (index == -1) {
                    return@runCatching
                }

                val targetIndex = index + direction

                if (targetIndex !in steps.indices) {
                    return@runCatching
                }

                val current = steps[index]
                val target = steps[targetIndex]
                val temporaryOrder = -1_000_000 - current.id.toInt()

                trainingCycleRepository.updateStep(
                    current.copy(stepOrder = temporaryOrder)
                )

                trainingCycleRepository.updateStep(
                    target.copy(stepOrder = current.stepOrder)
                )

                trainingCycleRepository.updateStep(
                    current.copy(stepOrder = target.stepOrder)
                )

                refreshSelectedSteps()
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    private fun normalizeSelectedStepOrder() {
        val cycleId = selectedCycleId.value
            ?: return

        viewModelScope.launch {
            runCatching {
                val normalizedSteps = trainingCycleRepository
                    .getStepsForCycle(cycleId)
                    .sortedBy { it.stepOrder }
                    .mapIndexed { index, step ->
                        step.copy(stepOrder = index)
                    }

                trainingCycleRepository.updateSteps(normalizedSteps)
                refreshSelectedSteps()
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    private fun refreshSelectedSteps() {
        val cycleId = selectedCycleId.value

        if (cycleId == null) {
            selectedCycleSteps.value = emptyList()
            return
        }

        viewModelScope.launch {
            selectedCycleSteps.value = trainingCycleRepository
                .getStepSummariesForCycle(cycleId)
        }
    }
}

private data class CycleBaseState(
    val cycles: List<TrainingCycleEntity>,
    val splits: List<SplitTemplateEntity>,
    val selectedId: Long?,
    val steps: List<TrainingCycleStepSummaryRow>,
)