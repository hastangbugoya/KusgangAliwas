package com.example.kusgangaliwas.ui.split

data class ExercisePickerUiState(
    val splitId: Long = 0L,
    val searchText: String = "",
    val exercises: List<ExercisePickerItem> = emptyList(),
    val selectedExerciseIds: Set<Long> = emptySet(),
    val splitName: String = "",
)

data class ExercisePickerItem(
    val exerciseId: Long,
    val exerciseName: String,
    val exerciseTypeLabel: String,
    val alreadyInSplit: Boolean,
)
