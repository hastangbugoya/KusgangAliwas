package com.example.kusgangaliwas.ui.cycle

data class TrainingCycleUiState(
    val cycles: List<TrainingCycleListItem> = emptyList(),
    val selectedCycleId: Long? = null,
    val selectedCycleName: String = "",
    val selectedCycleNotes: String = "",
    val steps: List<TrainingCycleStepListItem> = emptyList(),
    val availableSplits: List<TrainingCycleSplitOption> = emptyList(),
    val newCycleName: String = "",
    val newCycleNotes: String = "",
    val isCreateCycleExpanded: Boolean = false,
)

data class TrainingCycleListItem(
    val id: Long,
    val name: String,
    val notes: String?,
    val isActive: Boolean,
    val startedDateText: String? = null,
    val lastLoggedSessionDateText: String? = null,
)

data class TrainingCycleStepListItem(
    val id: Long,
    val splitTemplateId: Long,
    val splitName: String,
    val stepOrder: Int,
    val warnBeforeMarkDone: Boolean,
    val notes: String?,

    /**
     * Example:
     * "Chest, Shoulders, Triceps"
     */
    val muscleGroupsText: String,

    val strengthExerciseCount: Int,
    val cardioExerciseCount: Int,
    val totalExerciseCount: Int,
)

data class TrainingCycleSplitOption(
    val splitTemplateId: Long,
    val splitName: String,
    val alreadyInSelectedCycle: Boolean,
)