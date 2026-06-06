package com.example.kusgangaliwas.ui.split

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.kusgangaliwas.data.local.entity.ExerciseMotivationalGoalEntity
import com.example.kusgangaliwas.data.local.entity.ExerciseMotivationalGoalType
import com.example.kusgangaliwas.data.local.entity.ExercisePaceProfileEntity
import com.example.kusgangaliwas.data.local.entity.ExerciseType
import com.example.kusgangaliwas.data.local.entity.MuscleGroupEntity
import com.example.kusgangaliwas.data.local.entity.SplitTemplateExerciseEntity
import com.example.kusgangaliwas.ui.theme.KaPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitRoadmapScreen(
    uiState: SplitRoadmapUiState,
    onBackClick: () -> Unit,
    onOverflowClick: () -> Unit,
    onAddExercise: (Long) -> Unit,
    onDeleteExercise: (Long) -> Unit,
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.splitName,
                        fontWeight = FontWeight.Bold,
                    )
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
                            tint = KaPalette.Amber,
                        )
                    }

                    IconButton(onClick = onOverflowClick) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
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
                KaSecondLevelSectionCard(
                    title = "Roadmap",
                    count = uiState.roadmapItems.size,
                    accentColor = KaPalette.Amber,
                    trailing = {
                        TextButton(
                            onClick = { onOpenExercisePicker(uiState.splitId) },
                        ) {
                            Text(
                                text = "Add",
                                color = KaPalette.Amber,
                            )
                        }
                    },
                ) {
                    if (uiState.roadmapItems.isEmpty()) {
                        EmptyText("No exercises in this split yet.")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.roadmapItems.forEachIndexed { index, item ->
                                RoadmapExerciseCard(
                                    index = index,
                                    item = item,
                                    onDeleteExercise = onDeleteExercise,
                                    onUpdateCardioTargets = onUpdateCardioTargets,
                                    onUpdatePaceProfileForSplitExercise =
                                        onUpdatePaceProfileForSplitExercise,
                                    onImportMotivationalGoalToSplitExercise =
                                        onImportMotivationalGoalToSplitExercise,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoadmapExerciseCard(
    index: Int,
    item: SplitRoadmapItemUiState,
    onDeleteExercise: (Long) -> Unit,
    onUpdateCardioTargets: (SplitTemplateExerciseEntity, Double?, String?, Int?) -> Unit,
    onUpdatePaceProfileForSplitExercise: (SplitTemplateExerciseEntity, Long?) -> Unit,
    onImportMotivationalGoalToSplitExercise: (Long, Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val splitExercise = item.splitTemplateExercise
    val exerciseType = item.exerciseType
    val accentColor = exerciseType.accentColor()

    KaInnerCard(
        modifier = modifier,
        borderColor = accentColor.copy(alpha = 0.35f),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    color = accentColor,
                    modifier = Modifier.padding(top = 2.dp),
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(
                        text = item.exerciseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TypePill(
                            text = exerciseType?.displayText() ?: "Unknown",
                            color = accentColor,
                        )

                        Text(
                            text = exerciseType.trainingHint(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                TextButton(
                    onClick = {
                        onDeleteExercise(splitExercise.id)
                    },
                ) {
                    Text(
                        text = "Delete",
                        color = KaPalette.Danger,
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

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

            if (exerciseType == ExerciseType.CARDIO) {
                Text(
                    text = "Uses latest logged cardio metrics during workout start. Examples: distance, duration, incline, resistance.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                CardioTargetEditorRow(
                    splitExercise = splitExercise,
                    onUpdateCardioTargets = onUpdateCardioTargets,
                )
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

private fun ExerciseType?.trainingHint(): String {
    return when (this) {
        ExerciseType.STRENGTH ->
            "Previous values suggested"

        ExerciseType.CARDIO ->
            "Latest cardio suggested"

        ExerciseType.MOBILITY ->
            "Mobility"

        ExerciseType.OTHER ->
            "General"

        null ->
            "Previous values suggested"
    }
}

@Composable
private fun ExerciseType?.accentColor(): Color {
    return when (this) {
        ExerciseType.STRENGTH -> KaPalette.SteelBlue
        ExerciseType.CARDIO -> KaPalette.Success
        ExerciseType.MOBILITY -> KaPalette.Purple
        ExerciseType.OTHER -> MaterialTheme.colorScheme.onSurfaceVariant
        null -> MaterialTheme.colorScheme.onSurfaceVariant
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

    KaSecondLevelSectionCard(
        title = "Split",
        accentColor = KaPalette.Amber,
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { value ->
                    text = value
                },
                label = {
                    Text("Split name")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = {
                    onRenameSplit(text)
                },
                enabled = text.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
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
    KaSecondLevelSectionCard(
        title = "Muscle groups",
        count = selectedMuscleGroupIds.size.takeIf { muscleGroups.isNotEmpty() },
        accentColor = KaPalette.SteelBlue,
    ) {
        if (muscleGroups.isEmpty()) {
            EmptyText("No muscle groups yet.")
        } else {
            Row(
                modifier = Modifier.horizontalScroll(
                    rememberScrollState(),
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                muscleGroups.forEach { muscleGroup ->
                    val selected = selectedMuscleGroupIds.contains(muscleGroup.id)

                    KaFilterChip(
                        selected = selected,
                        onClick = {
                            onToggleMuscleGroup(
                                muscleGroup.id,
                                selected,
                            )
                        },
                        label = muscleGroup.name,
                        accentColor = KaPalette.SteelBlue,
                    )
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
    KaSecondLevelSectionCard(
        title = "Schedule",
        accentColor = KaPalette.Purple,
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "Enabled",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )

                    Text(
                        text = if (uiState.scheduleEnabled) {
                            "This split can generate planned sessions."
                        } else {
                            "Scheduling is off for this split."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Switch(
                    checked = uiState.scheduleEnabled,
                    onCheckedChange = onScheduleEnabledChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = KaPalette.Purple,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    ),
                )
            }

            OutlinedTextField(
                value = uiState.scheduleTitle,
                onValueChange = onScheduleTitleChange,
                label = { Text("Schedule title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = "Days",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
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
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = onSaveSchedule,
                enabled = uiState.selectedDaysMask != 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
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

    KaSecondLevelSectionCard(
        title = "Apply pace name",
        accentColor = KaPalette.Amber,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
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
                        KaFilterChip(
                            selected = paceNameText.equals(
                                paceName,
                                ignoreCase = true,
                            ),
                            onClick = {
                                paceNameText = paceName
                            },
                            label = paceName,
                            accentColor = KaPalette.Amber,
                        )
                    }
                }
            } else {
                EmptyText("No pace profile names found in this split yet.")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
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

    KaFilterChip(
        selected = selected,
        onClick = {
            onToggleScheduleDay(bitIndex)
        },
        label = label,
        accentColor = KaPalette.Purple,
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
                KaFilterChip(
                    selected = splitExercise.paceProfileId == null,
                    onClick = {
                        onUpdatePaceProfileForSplitExercise(
                            splitExercise,
                            null,
                        )
                    },
                    label = "Exercise default",
                    accentColor = KaPalette.Amber,
                )

                paceProfiles.forEach { profile ->
                    KaFilterChip(
                        selected = splitExercise.paceProfileId == profile.id,
                        onClick = {
                            onUpdatePaceProfileForSplitExercise(
                                splitExercise,
                                profile.id,
                            )
                        },
                        label = if (profile.isDefault) {
                            "${profile.name} default"
                        } else {
                            profile.name
                        },
                        accentColor = KaPalette.Amber,
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
            fontWeight = FontWeight.Bold,
        )

        if (attachedMotivationalGoals.isEmpty()) {
            Text(
                text = "No goals attached to this split exercise.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                attachedMotivationalGoals.forEach { goal ->
                    GoalLine(text = buildMotivationalGoalLine(goal))
                }
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
                    KaFilterChip(
                        selected = false,
                        onClick = {
                            onImportMotivationalGoalToSplitExercise(
                                goal.id,
                                splitTemplateExerciseId,
                            )
                        },
                        label = goal.title,
                        accentColor = KaPalette.Purple,
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalLine(
    text: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = KaPalette.PurpleContainer.copy(alpha = 0.45f),
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = KaPalette.Purple.copy(alpha = 0.25f),
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            style = MaterialTheme.typography.bodySmall,
            color = KaPalette.Purple,
        )
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

    KaInsetCard(
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Cardio targets",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )

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

@Composable
private fun KaSecondLevelSectionCard(
    title: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    count: Int? = null,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = accentColor.copy(alpha = 0.18f),
        ),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .height(34.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(accentColor.copy(alpha = 0.9f)),
                )

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )

                count?.let {
                    Text(
                        text = it.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                trailing?.invoke()
            }

            content()
        }
    }
}

@Composable
private fun KaInnerCard(
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = borderColor,
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun KaInsetCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        ),
        shape = RoundedCornerShape(14.dp),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun TypePill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.16f),
            contentColor = color,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = 0.35f),
        ),
        shape = RoundedCornerShape(999.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun KaFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(label)
        },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = accentColor.copy(alpha = 0.18f),
            selectedLabelColor = accentColor,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            selectedBorderColor = accentColor.copy(alpha = 0.55f),
        ),
    )
}

@Composable
private fun EmptyText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
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
