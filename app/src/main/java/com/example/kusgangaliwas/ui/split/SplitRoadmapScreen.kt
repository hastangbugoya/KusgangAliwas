package com.example.kusgangaliwas.ui.split

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.kusgangaliwas.data.local.entity.ExerciseMotivationalGoalEntity
import com.example.kusgangaliwas.data.local.entity.ExerciseMotivationalGoalType
import com.example.kusgangaliwas.data.local.entity.ExercisePaceProfileEntity
import com.example.kusgangaliwas.data.local.entity.ExerciseType
import com.example.kusgangaliwas.data.local.entity.MuscleGroupEntity
import com.example.kusgangaliwas.data.local.entity.SplitTemplateExerciseEntity
import com.example.kusgangaliwas.ui.common.SectionHeader
import com.example.kusgangaliwas.ui.common.SharpCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitRoadmapScreen(
    uiState: SplitRoadmapUiState,
    onBackClick: () -> Unit,
    onOverflowClick: () -> Unit,
    onAddExercise: (Long) -> Unit,
    onDeleteExercise: (Long) -> Unit,
    onUpdateExerciseTargets: (SplitTemplateExerciseEntity, Int?, Int?, Int?) -> Unit,
    onScheduleEnabledChange: (Boolean) -> Unit,
    onToggleScheduleDay: (Int) -> Unit,
    onHorizonWeeksTextChange: (String) -> Unit,
    onScheduleTitleChange: (String) -> Unit,
    onSaveSchedule: () -> Unit,
    modifier: Modifier = Modifier,
    onRenameSplit: (String) -> Unit,
    onToggleMuscleGroupForSplit: (Long, Boolean) -> Unit,
    onUpdateCardioTargets: (SplitTemplateExerciseEntity, Double?, String?, Int?) -> Unit,
    onUpdatePaceProfileForSplitExercise: (SplitTemplateExerciseEntity, Long?) -> Unit,
    onImportMotivationalGoalToSplitExercise: (Long, Long) -> Unit,
    onApplyPaceProfileNameToSplit: (String) -> Unit,
    onOpenExercisePicker: (Long) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(uiState.splitName)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onOpenExercisePicker(uiState.splitId) },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add exercise",
                        )
                    }

                    IconButton(onClick = onOverflowClick) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                RenameSplitCard(
                    splitName = uiState.splitName,
                    onRenameSplit = onRenameSplit,
                )
            }

            item {
                SplitMuscleGroupCard(
                    muscleGroups = uiState.availableMuscleGroups,
                    selectedMuscleGroupIds = uiState.selectedMuscleGroupIds,
                    onToggleMuscleGroup = onToggleMuscleGroupForSplit,
                )
            }

            item {
                ScheduleCard(
                    uiState = uiState,
                    onScheduleEnabledChange = onScheduleEnabledChange,
                    onToggleScheduleDay = onToggleScheduleDay,
                    onHorizonWeeksTextChange = onHorizonWeeksTextChange,
                    onScheduleTitleChange = onScheduleTitleChange,
                    onSaveSchedule = onSaveSchedule,
                )
            }

            item {
                ApplyPaceProfileNameCard(
                    uiState = uiState,
                    onApplyPaceProfileNameToSplit = onApplyPaceProfileNameToSplit,
                )
            }

            item {
                SharpCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader("Roadmap")

                        if (uiState.roadmapItems.isEmpty()) {
                            Text("No exercises in this split yet.")
                        } else {
                            uiState.roadmapItems.forEach { item ->
                                val splitExercise = item.splitTemplateExercise
                                val exerciseType = item.exerciseType

                                SharpCard {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Text(
                                            text = item.exerciseName,
                                        )

                                        Text(
                                            text = exerciseType?.displayText() ?: "Unknown",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )

                                        PaceProfileSelectorRow(
                                            splitExercise = splitExercise,
                                            paceProfiles = item.paceProfiles,
                                            onUpdatePaceProfileForSplitExercise =
                                                onUpdatePaceProfileForSplitExercise,
                                        )

                                        MotivationalGoalRoadmapSection(
                                            splitTemplateExerciseId = splitExercise.id,
                                            attachedMotivationalGoals =
                                                item.attachedMotivationalGoals,
                                            availableLongTermMotivationalGoals =
                                                item.availableLongTermMotivationalGoals,
                                            onImportMotivationalGoalToSplitExercise =
                                                onImportMotivationalGoalToSplitExercise,
                                        )

                                        buildTargetText(
                                            sets = splitExercise.targetSets,
                                            min = splitExercise.targetRepsMin,
                                            max = splitExercise.targetRepsMax,
                                        )?.let { text ->
                                            Text(text = text)
                                        }

                                        Text(
                                            text = when (exerciseType) {
                                                ExerciseType.STRENGTH ->
                                                    "Previous workout values will be suggested during logging."

                                                ExerciseType.CARDIO ->
                                                    "Uses latest logged cardio metrics during workout start."

                                                ExerciseType.MOBILITY ->
                                                    "Mobility exercise."

                                                ExerciseType.OTHER ->
                                                    "General exercise."

                                                null ->
                                                    "Previous workout values will be suggested during logging."
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )

                                        if (exerciseType == ExerciseType.CARDIO) {
                                            Text(
                                                text = "Examples: distance, duration, incline, resistance.",
                                            )
                                        }

                                        when (exerciseType) {
                                            ExerciseType.STRENGTH -> {
                                                TargetEditorRow(
                                                    splitExercise = splitExercise,
                                                    onUpdateExerciseTargets = onUpdateExerciseTargets,
                                                )
                                            }

                                            ExerciseType.CARDIO -> {
                                                CardioTargetEditorRow(
                                                    splitExercise = splitExercise,
                                                    onUpdateCardioTargets = onUpdateCardioTargets,
                                                )
                                            }

                                            else -> Unit
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            TextButton(
                                                onClick = {
                                                    onDeleteExercise(splitExercise.id)
                                                },
                                            ) {
                                                Text("Delete")
                                            }
                                        }
                                    }
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun ExerciseType.displayText(): String {
    return when (this) {
        ExerciseType.STRENGTH -> "Strength"
        ExerciseType.CARDIO -> "Cardio"
        ExerciseType.MOBILITY -> "Mobility"
        ExerciseType.OTHER -> "Other"
    }
}

@Composable
private fun RenameSplitCard(
    splitName: String,
    onRenameSplit: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by remember(splitName) {
        mutableStateOf(splitName)
    }

    SharpCard {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SectionHeader("Split")

            OutlinedTextField(
                value = text,
                onValueChange = { value ->
                    text = value
                },
                label = {
                    Text("Split name")
                },
                singleLine = true,
            )

            Button(
                onClick = {
                    onRenameSplit(text)
                },
                enabled = text.isNotBlank(),
            ) {
                Text("Rename split")
            }
        }
    }
}

@Composable
private fun SplitMuscleGroupCard(
    muscleGroups: List<MuscleGroupEntity>,
    selectedMuscleGroupIds: Set<Long>,
    onToggleMuscleGroup: (Long, Boolean) -> Unit,
) {
    SharpCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionHeader("Muscle groups")

            if (muscleGroups.isEmpty()) {
                Text("No muscle groups yet.")
            } else {
                Row(
                    modifier = Modifier.horizontalScroll(
                        rememberScrollState(),
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    muscleGroups.forEach { muscleGroup ->
                        val selected = selectedMuscleGroupIds.contains(muscleGroup.id)

                        FilterChip(
                            selected = selected,
                            onClick = {
                                onToggleMuscleGroup(
                                    muscleGroup.id,
                                    selected,
                                )
                            },
                            label = {
                                Text(muscleGroup.name)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleCard(
    uiState: SplitRoadmapUiState,
    onScheduleEnabledChange: (Boolean) -> Unit,
    onToggleScheduleDay: (Int) -> Unit,
    onHorizonWeeksTextChange: (String) -> Unit,
    onScheduleTitleChange: (String) -> Unit,
    onSaveSchedule: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SharpCard {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SectionHeader("Schedule")

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Enabled")
                Switch(
                    checked = uiState.scheduleEnabled,
                    onCheckedChange = onScheduleEnabledChange,
                )
            }

            OutlinedTextField(
                value = uiState.scheduleTitle,
                onValueChange = onScheduleTitleChange,
                label = { Text("Schedule title") },
                singleLine = true,
            )

            Text("Days")

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ScheduleDayChip("S", 0, uiState.selectedDaysMask, onToggleScheduleDay)
                ScheduleDayChip("M", 1, uiState.selectedDaysMask, onToggleScheduleDay)
                ScheduleDayChip("T", 2, uiState.selectedDaysMask, onToggleScheduleDay)
                ScheduleDayChip("W", 3, uiState.selectedDaysMask, onToggleScheduleDay)
                ScheduleDayChip("T", 4, uiState.selectedDaysMask, onToggleScheduleDay)
                ScheduleDayChip("F", 5, uiState.selectedDaysMask, onToggleScheduleDay)
                ScheduleDayChip("S", 6, uiState.selectedDaysMask, onToggleScheduleDay)
            }

            OutlinedTextField(
                value = uiState.horizonWeeksText,
                onValueChange = onHorizonWeeksTextChange,
                label = { Text("Horizon weeks") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                ),
            )

            Button(
                onClick = onSaveSchedule,
                enabled = uiState.selectedDaysMask != 0,
            ) {
                Text("Save schedule")
            }
        }
    }
}

@Composable
private fun ApplyPaceProfileNameCard(
    uiState: SplitRoadmapUiState,
    onApplyPaceProfileNameToSplit: (String) -> Unit,
) {
    var paceNameText by remember(uiState.splitId) {
        mutableStateOf("")
    }

    val reusablePaceNames = remember(uiState.roadmapItems) {
        uiState.roadmapItems
            .flatMap { item -> item.paceProfiles }
            .map { profile -> profile.name.trim() }
            .filter { name -> name.isNotBlank() }
            .distinctBy { name -> name.lowercase() }
            .sortedWith(String.CASE_INSENSITIVE_ORDER)
    }

    SharpCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SectionHeader("Apply pace name")

            Text(
                text = "Applies a matching enabled pace profile by name. Exercises without that pace are skipped.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (reusablePaceNames.isNotEmpty()) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    reusablePaceNames.forEach { paceName ->
                        FilterChip(
                            selected = paceNameText.equals(
                                paceName,
                                ignoreCase = true,
                            ),
                            onClick = {
                                paceNameText = paceName
                            },
                            label = {
                                Text(paceName)
                            },
                        )
                    }
                }
            } else {
                Text(
                    text = "No pace profile names found in this split yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = paceNameText,
                    onValueChange = { value ->
                        paceNameText = value
                    },
                    label = {
                        Text("Pace name")
                    },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )

                Button(
                    onClick = {
                        onApplyPaceProfileNameToSplit(paceNameText)
                    },
                    enabled = paceNameText.isNotBlank(),
                ) {
                    Text("Apply")
                }
            }
        }
    }
}

@Composable
private fun ScheduleDayChip(
    label: String,
    bitIndex: Int,
    selectedDaysMask: Int,
    onToggleScheduleDay: (Int) -> Unit,
) {
    val selected = (selectedDaysMask and (1 shl bitIndex)) != 0

    FilterChip(
        selected = selected,
        onClick = {
            onToggleScheduleDay(bitIndex)
        },
        label = {
            Text(label)
        },
    )
}

@Composable
private fun PaceProfileSelectorRow(
    splitExercise: SplitTemplateExerciseEntity,
    paceProfiles: List<ExercisePaceProfileEntity>,
    onUpdatePaceProfileForSplitExercise: (SplitTemplateExerciseEntity, Long?) -> Unit,
) {
    val selectedProfile = paceProfiles.firstOrNull { profile ->
        profile.id == splitExercise.paceProfileId
    }
    val defaultProfile = paceProfiles.firstOrNull { profile ->
        profile.isDefault
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = buildPaceSelectionText(
                selectedProfile = selectedProfile,
                defaultProfile = defaultProfile,
                profileCount = paceProfiles.size,
                hasExplicitSelection = splitExercise.paceProfileId != null,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (paceProfiles.isNotEmpty()) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = splitExercise.paceProfileId == null,
                    onClick = {
                        onUpdatePaceProfileForSplitExercise(
                            splitExercise,
                            null,
                        )
                    },
                    label = {
                        Text("Exercise default")
                    },
                )

                paceProfiles.forEach { profile ->
                    FilterChip(
                        selected = splitExercise.paceProfileId == profile.id,
                        onClick = {
                            onUpdatePaceProfileForSplitExercise(
                                splitExercise,
                                profile.id,
                            )
                        },
                        label = {
                            Text(
                                text = if (profile.isDefault) {
                                    "${profile.name} default"
                                } else {
                                    profile.name
                                },
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun MotivationalGoalRoadmapSection(
    splitTemplateExerciseId: Long,
    attachedMotivationalGoals: List<ExerciseMotivationalGoalEntity>,
    availableLongTermMotivationalGoals: List<ExerciseMotivationalGoalEntity>,
    onImportMotivationalGoalToSplitExercise: (Long, Long) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "Motivational goals",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (attachedMotivationalGoals.isEmpty()) {
            Text(
                text = "No goals attached to this split exercise.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            attachedMotivationalGoals.forEach { goal ->
                Text(
                    text = buildMotivationalGoalLine(goal),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }

        if (availableLongTermMotivationalGoals.isNotEmpty()) {
            Text(
                text = "Import from exercise goals",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                availableLongTermMotivationalGoals.forEach { goal ->
                    FilterChip(
                        selected = false,
                        onClick = {
                            onImportMotivationalGoalToSplitExercise(
                                goal.id,
                                splitTemplateExerciseId,
                            )
                        },
                        label = {
                            Text(goal.title)
                        },
                    )
                }
            }
        }
    }
}

private fun buildPaceSelectionText(
    selectedProfile: ExercisePaceProfileEntity?,
    defaultProfile: ExercisePaceProfileEntity?,
    profileCount: Int,
    hasExplicitSelection: Boolean,
): String {
    return when {
        selectedProfile != null ->
            "Split pace: ${selectedProfile.name}"

        hasExplicitSelection ->
            "Split pace: selected profile missing"

        defaultProfile != null ->
            "Split pace: exercise default ${defaultProfile.name}"

        profileCount > 0 ->
            "Split pace: this exercise has pace profiles, but no default is set"

        else ->
            "Split pace: none"
    }
}

@Composable
private fun CardioTargetEditorRow(
    splitExercise: SplitTemplateExerciseEntity,
    onUpdateCardioTargets: (SplitTemplateExerciseEntity, Double?, String?, Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var distanceText by remember(splitExercise.id) {
        mutableStateOf(splitExercise.targetDistance?.toString() ?: "")
    }
    var distanceUnitText by remember(splitExercise.id) {
        mutableStateOf(splitExercise.targetDistanceUnit ?: "mi")
    }
    var durationText by remember(splitExercise.id) {
        mutableStateOf(splitExercise.targetDurationMinutes?.toString() ?: "")
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TargetNumberField(
                label = "Distance",
                value = distanceText,
                onValueChange = { value ->
                    distanceText = value
                    onUpdateCardioTargets(
                        splitExercise,
                        distanceText.toDoubleOrNull(),
                        distanceUnitText,
                        durationText.toIntOrNull(),
                    )
                },
                modifier = Modifier.weight(1f),
            )

            OutlinedTextField(
                value = distanceUnitText,
                onValueChange = { value ->
                    distanceUnitText = value
                    onUpdateCardioTargets(
                        splitExercise,
                        distanceText.toDoubleOrNull(),
                        distanceUnitText,
                        durationText.toIntOrNull(),
                    )
                },
                label = { Text("Unit") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }

        TargetNumberField(
            label = "Duration minutes",
            value = durationText,
            onValueChange = { value ->
                durationText = value
                onUpdateCardioTargets(
                    splitExercise,
                    distanceText.toDoubleOrNull(),
                    distanceUnitText,
                    durationText.toIntOrNull(),
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun TargetEditorRow(
    splitExercise: SplitTemplateExerciseEntity,
    onUpdateExerciseTargets: (SplitTemplateExerciseEntity, Int?, Int?, Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var setsText by remember(splitExercise.id) {
        mutableStateOf(splitExercise.targetSets?.toString() ?: "")
    }
    var minText by remember(splitExercise.id) {
        mutableStateOf(splitExercise.targetRepsMin?.toString() ?: "")
    }
    var maxText by remember(splitExercise.id) {
        mutableStateOf(splitExercise.targetRepsMax?.toString() ?: "")
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TargetNumberField(
            label = "Sets",
            value = setsText,
            onValueChange = { value ->
                setsText = value
                onUpdateExerciseTargets(
                    splitExercise,
                    setsText.toIntOrNull(),
                    minText.toIntOrNull(),
                    maxText.toIntOrNull(),
                )
            },
            modifier = Modifier.weight(1f),
        )

        TargetNumberField(
            label = "Min",
            value = minText,
            onValueChange = { value ->
                minText = value
                onUpdateExerciseTargets(
                    splitExercise,
                    setsText.toIntOrNull(),
                    minText.toIntOrNull(),
                    maxText.toIntOrNull(),
                )
            },
            modifier = Modifier.weight(1f),
        )

        TargetNumberField(
            label = "Max",
            value = maxText,
            onValueChange = { value ->
                maxText = value
                onUpdateExerciseTargets(
                    splitExercise,
                    setsText.toIntOrNull(),
                    minText.toIntOrNull(),
                    maxText.toIntOrNull(),
                )
            },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TargetNumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(label)
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
        ),
        textStyle = MaterialTheme.typography.bodySmall,
        modifier = modifier,
    )
}

private fun buildTargetText(
    sets: Int?,
    min: Int?,
    max: Int?,
): String? {
    val setsPart = sets?.let { "$it sets" }

    val repsPart = when {
        min != null && max != null && min != max -> "$min-$max reps"
        min != null -> "$min reps"
        max != null -> "up to $max reps"
        else -> null
    }

    return listOfNotNull(setsPart, repsPart)
        .takeIf { it.isNotEmpty() }
        ?.joinToString(" · ")
}

private fun buildMotivationalGoalLine(
    goal: ExerciseMotivationalGoalEntity,
): String {
    val detail = buildMotivationalGoalDetail(goal)

    return if (detail == null) {
        goal.title
    } else {
        "${goal.title} · $detail"
    }
}

private fun buildMotivationalGoalDetail(
    goal: ExerciseMotivationalGoalEntity,
): String? {
    return when (goal.goalType) {
        ExerciseMotivationalGoalType.WEIGHT_REPS -> {
            val weightText = goal.targetWeight?.let { "${formatGoalDouble(it)} lb" }
            val repsText = goal.targetReps?.let { "$it reps" }

            listOfNotNull(weightText, repsText)
                .takeIf { it.isNotEmpty() }
                ?.joinToString(" x ")
        }

        ExerciseMotivationalGoalType.ESTIMATED_1RM,
        ExerciseMotivationalGoalType.ACTUAL_1RM -> {
            goal.targetOneRepMax?.let {
                "${formatGoalDouble(it)} lb"
            }
        }

        ExerciseMotivationalGoalType.CARDIO_DISTANCE -> {
            goal.targetDistance?.let {
                "${formatGoalDouble(it)} ${goal.targetDistanceUnit ?: "mi"}"
            }
        }

        ExerciseMotivationalGoalType.CARDIO_DURATION -> {
            goal.targetDurationSeconds?.let(::formatDurationSeconds)
        }

        ExerciseMotivationalGoalType.CARDIO_DISTANCE_DURATION -> {
            val distanceText = goal.targetDistance?.let {
                "${formatGoalDouble(it)} ${goal.targetDistanceUnit ?: "mi"}"
            }
            val durationText = goal.targetDurationSeconds?.let(::formatDurationSeconds)

            listOfNotNull(distanceText, durationText)
                .takeIf { it.isNotEmpty() }
                ?.joinToString(" in ")
        }
    }
}

private fun formatGoalDouble(
    value: Double,
): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        value.toString()
    }
}

private fun formatDurationSeconds(
    seconds: Int,
): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60

    return if (remainingSeconds == 0) {
        "${minutes}m"
    } else {
        "${minutes}m ${remainingSeconds}s"
    }
}
