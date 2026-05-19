package com.example.kusgangaliwas.ui.split

data class ExercisePickerUiState(
    val title: String = "Add exercises",
    val searchText: String = "",
    val exercises: List<ExercisePickerItem> = emptyList(),
    val selectedExerciseIds: Set<Long> = emptySet(),
)

data class ExercisePickerItem(
    val exerciseId: Long,
    val exerciseName: String,
    val exerciseTypeLabel: String,
    val supportingText: String = "",
    val alreadySelected: Boolean,
)
