package com.example.kusgangaliwas.ui.split

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.kusgangaliwas.data.local.entity.ExerciseType
import com.example.kusgangaliwas.data.local.entity.SplitTemplateExerciseEntity
import com.example.kusgangaliwas.ui.common.KusgangTopBar
import com.example.kusgangaliwas.ui.common.SectionHeader
import com.example.kusgangaliwas.ui.common.SharpCard

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
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            KusgangTopBar(
                title = uiState.splitName,
                onBackClick = onBackClick,
                onOverflowClick = onOverflowClick,
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
                SharpCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader("Roadmap")

                        if (uiState.roadmapItems.isEmpty()) {
                            Text("No exercises in this split yet.")
                        } else {
                            uiState.roadmapItems.forEachIndexed { index, item ->
                                val splitExercise = item.splitTemplateExercise
                                val exerciseType = item.exerciseType

                                SharpCard {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Text(
                                            text = "${index + 1}. ${item.exerciseName}",
                                        )
                                        Text(
                                            text = "Type: ${exerciseType?.displayText() ?: "Unknown"}",
                                        )

                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
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
                                            )

                                            if (exerciseType == ExerciseType.CARDIO) {
                                                Text(
                                                    text = "Examples: distance, duration, incline, resistance.",
                                                )
                                            }
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
                            }
                        }
                    }
                }
            }

            item {
                SharpCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader("Add exercise")

                        if (uiState.availableExercises.isEmpty()) {
                            Text("Add exercises in the Exercises tab first.")
                        } else {
                            uiState.availableExercises.forEach { exercise ->
                                OutlinedButton(
                                    onClick = {
                                        onAddExercise(exercise.id)
                                    },
                                ) {
                                    Text(exercise.name)
                                }
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
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
        ),
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