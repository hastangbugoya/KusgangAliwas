package com.example.kusgangaliwas.ui.split

import com.example.kusgangaliwas.data.local.entity.ExerciseEntity
import com.example.kusgangaliwas.data.local.entity.SplitTemplateExerciseEntity

data class SplitRoadmapUiState(
    val splitId: Long = 0L,
    val splitName: String = "Split",
    val roadmapItems: List<SplitRoadmapItemUiState> = emptyList(),
    val availableExercises: List<ExerciseEntity> = emptyList(),
)

data class SplitRoadmapItemUiState(
    val splitTemplateExercise: SplitTemplateExerciseEntity,
    val exerciseName: String,
)
