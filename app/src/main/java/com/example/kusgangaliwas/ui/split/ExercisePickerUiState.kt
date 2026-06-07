package com.example.kusgangaliwas.ui.split

import com.example.kusgangaliwas.data.local.entity.MuscleGroupEntity

data class ExercisePickerUiState(
    val title: String = "Add exercises",
    val searchText: String = "",
    val availableMuscleGroups: List<MuscleGroupEntity> = emptyList(),
    val selectedMuscleGroupIds: Set<Long> = emptySet(),
    val exercises: List<ExercisePickerItem> = emptyList(),
    val selectedExerciseIds: Set<Long> = emptySet(),
)

data class ExercisePickerItem(
    val exerciseId: Long,
    val exerciseName: String,
    val exerciseTypeLabel: String,
    val supportingText: String = "",
    val muscleGroupIds: Set<Long> = emptySet(),
    val alreadySelected: Boolean,
)