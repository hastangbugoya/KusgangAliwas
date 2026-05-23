package com.example.kusgangaliwas.ui.split

import com.example.kusgangaliwas.data.local.entity.ExerciseEntity
import com.example.kusgangaliwas.data.local.entity.ExerciseMotivationalGoalEntity
import com.example.kusgangaliwas.data.local.entity.ExercisePaceProfileEntity
import com.example.kusgangaliwas.data.local.entity.ExerciseType
import com.example.kusgangaliwas.data.local.entity.MuscleGroupEntity
import com.example.kusgangaliwas.data.local.entity.SplitTemplateExerciseEntity

data class SplitRoadmapUiState(
    val splitId: Long = 0L,
    val splitName: String = "Split",
    val roadmapItems: List<SplitRoadmapItemUiState> = emptyList(),
    val availableExercises: List<ExerciseEntity> = emptyList(),
    val availableMuscleGroups: List<MuscleGroupEntity> = emptyList(),
    val selectedMuscleGroupIds: Set<Long> = emptySet(),

    /**
     * Simple v1 scheduling controls.
     *
     * These are intentionally lightweight and UI-friendly.
     * Persistence conversion happens in the ViewModel/use case layer.
     */
    val scheduleEnabled: Boolean = false,

    /**
     * Bitmask:
     * 0 = Sunday
     * 1 = Monday
     * 2 = Tuesday
     * 3 = Wednesday
     * 4 = Thursday
     * 5 = Friday
     * 6 = Saturday
     */
    val selectedDaysMask: Int = 0,

    /**
     * Editable text field backing state for horizon weeks.
     *
     * Stored as text so the user can temporarily clear the field while editing
     * without Compose immediately restoring the previous numeric value.
     *
     * Parsing/clamping happens in the ViewModel during save/update operations.
     */
    val horizonWeeksText: String = "4",

    /**
     * Optional schedule title override.
     * Defaults later to split name if blank.
     */
    val scheduleTitle: String = "",
)

data class SplitRoadmapItemUiState(
    val splitTemplateExercise: SplitTemplateExerciseEntity,
    val exerciseName: String,
    val exerciseType: ExerciseType? = null,

    /**
     * Pace profiles available for this exercise.
     *
     * The selected split-specific profile lives on:
     * splitTemplateExercise.paceProfileId
     *
     * Resolution later:
     * - selected paceProfileId
     * - else exercise default profile
     * - else no pace nudges
     */
    val paceProfiles: List<ExercisePaceProfileEntity> = emptyList(),

    /**
     * Motivational goals already attached to this split exercise.
     *
     * These are contextual targets only. They should not be shown as required
     * loads, completion criteria, or pass/fail status.
     */
    val attachedMotivationalGoals: List<ExerciseMotivationalGoalEntity> = emptyList(),

    /**
     * Active long-term goals for this exercise that are not necessarily attached
     * to this split exercise yet.
     *
     * The split UI can offer a simple import/attach action from this list.
     */
    val availableLongTermMotivationalGoals: List<ExerciseMotivationalGoalEntity> = emptyList(),
)