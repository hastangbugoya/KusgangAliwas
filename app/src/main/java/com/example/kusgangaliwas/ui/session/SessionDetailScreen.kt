package com.example.kusgangaliwas.ui.session

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.kusgangaliwas.data.local.entity.ActualExerciseSetLogEntity
import com.example.kusgangaliwas.ui.common.KusgangTopBar
import com.example.kusgangaliwas.ui.common.SectionHeader
import com.example.kusgangaliwas.ui.common.SharpCard

@Composable
fun SessionDetailScreen(
    uiState: SessionDetailUiState,
    onBackClick: () -> Unit,
    onOverflowClick: () -> Unit,
    onAddExercise: (Long) -> Unit,
    onAddSet: (Long) -> Unit,
    onUpdateSet: (ActualExerciseSetLogEntity, Double?, Int?) -> Unit,
    onDeleteSet: (Long) -> Unit,
    onDuplicateSet: (ActualExerciseSetLogEntity) -> Unit,
    modifier: Modifier = Modifier,
    onRatingChange: (Int?) -> Unit,
    onDeleteExerciseLogIfEmpty: (Long) -> Unit,
    onDeleteSession: () -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            KusgangTopBar(
                title = uiState.session?.title ?: "Session",
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
                SharpCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader("Exercise logs")

                        if (uiState.exerciseLogs.isEmpty()) {
                            Text("No exercises logged yet.")
                        } else {
                            uiState.exerciseLogs.forEachIndexed { index, item ->
                                var expanded by rememberSaveable(item.log.id) {
                                    mutableStateOf(false)
                                }

                                SharpCard {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Text("${index + 1}. ${item.exerciseName}")
                                        Text(buildSetSummary(item.sets))

                                        OutlinedButton(
                                            onClick = { expanded = !expanded },
                                        ) {
                                            Text(if (expanded) "Collapse" else "Expand")
                                        }

                                        if (expanded) {
                                            if (item.sets.isEmpty()) {
                                                Text("No sets yet.")
                                            } else {
                                                item.sets.forEach { set ->
                                                    SetEditorRow(
                                                        set = set,
                                                        onUpdateSet = onUpdateSet,
                                                        onDeleteSet = onDeleteSet,
                                                        onDuplicateSet = onDuplicateSet,
                                                    )
                                                }
                                            }

                                            OutlinedButton(
                                                onClick = { onAddSet(item.log.id) },
                                            ) {
                                                Text("Add set")
                                            }
                                            if (item.sets.isEmpty()) {
                                                TextButton(
                                                    onClick = {
                                                        onDeleteExerciseLogIfEmpty(item.log.id)
                                                    },
                                                ) {
                                                    Text("Remove exercise")
                                                }
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
                        SectionHeader("Session rating")

                        StarRatingRow(
                            rating = uiState.session?.rating,
                            onRatingChange = onRatingChange,
                        )
                    }
                }
            }

            item {
                SharpCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader("Danger zone")

                        Text(
                            text = "Delete this workout session permanently.",
                            style = MaterialTheme.typography.bodySmall,
                        )

                        OutlinedButton(
                            onClick = onDeleteSession,
                        ) {
                            Text("Delete session")
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
                                    onClick = { onAddExercise(exercise.id) },
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

@Composable
private fun StarRatingRow(
    rating: Int?,
    onRatingChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        (1..5).forEach { star ->
            val filled = (rating ?: 0) >= star

            Text(
                text = if (filled) "★" else "☆",
                style = MaterialTheme.typography.headlineMedium, // BIGGER
                color = if (filled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                },
                modifier = Modifier
                    .padding(4.dp)
                    .clickable {
                        if (rating == star) {
                            onRatingChange(null)
                        } else {
                            onRatingChange(star)
                        }
                    },
            )
        }
    }
}

@Composable
private fun SetEditorRow(
    set: ActualExerciseSetLogEntity,
    onUpdateSet: (ActualExerciseSetLogEntity, Double?, Int?) -> Unit,
    onDeleteSet: (Long) -> Unit,
    onDuplicateSet: (ActualExerciseSetLogEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text("Set ${set.setOrder}")

        WeightRepsInputRow(
            set = set,
            onUpdateSet = onUpdateSet,
        )

        set.notes
            ?.takeIf { it.isNotBlank() }
            ?.let { note ->
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(
                onClick = { onDuplicateSet(set) },
            ) {
                Text("Duplicate")
            }

            TextButton(
                onClick = { onDeleteSet(set.id) },
            ) {
                Text("Delete")
            }
        }
    }
}

@Composable
private fun WeightRepsInputRow(
    set: ActualExerciseSetLogEntity,
    onUpdateSet: (ActualExerciseSetLogEntity, Double?, Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var weightText by remember(set.id) {
        mutableStateOf(set.weight?.toString() ?: "")
    }
    var repsText by remember(set.id) {
        mutableStateOf(set.reps?.toString() ?: "")
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NumberInputWithStepper(
            label = "Weight",
            value = weightText,
            onValueChange = { value ->
                weightText = value
                onUpdateSet(
                    set,
                    weightText.toDoubleOrNull(),
                    repsText.toIntOrNull(),
                )
            },
            onDecrement = {
                val newWeight = ((weightText.toDoubleOrNull() ?: 0.0) - 2.5)
                    .coerceAtLeast(0.0)
                weightText = formatWeight(newWeight)
                onUpdateSet(
                    set,
                    newWeight,
                    repsText.toIntOrNull(),
                )
            },
            onIncrement = {
                val newWeight = (weightText.toDoubleOrNull() ?: 0.0) + 2.5
                weightText = formatWeight(newWeight)
                onUpdateSet(
                    set,
                    newWeight,
                    repsText.toIntOrNull(),
                )
            },
            keyboardType = KeyboardType.Decimal,
            modifier = Modifier.weight(1f),
        )

        NumberInputWithStepper(
            label = "Reps",
            value = repsText,
            onValueChange = { value ->
                repsText = value
                onUpdateSet(
                    set,
                    weightText.toDoubleOrNull(),
                    repsText.toIntOrNull(),
                )
            },
            onDecrement = {
                val newReps = ((repsText.toIntOrNull() ?: 0) - 1)
                    .coerceAtLeast(0)
                repsText = newReps.toString()
                onUpdateSet(
                    set,
                    weightText.toDoubleOrNull(),
                    newReps,
                )
            },
            onIncrement = {
                val newReps = (repsText.toIntOrNull() ?: 0) + 1
                repsText = newReps.toString()
                onUpdateSet(
                    set,
                    weightText.toDoubleOrNull(),
                    newReps,
                )
            },
            keyboardType = KeyboardType.Number,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun NumberInputWithStepper(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
            ),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            OutlinedButton(
                onClick = onDecrement,
                modifier = Modifier.weight(1f),
            ) {
                Text("-")
            }

            OutlinedButton(
                onClick = onIncrement,
                modifier = Modifier.weight(1f),
            ) {
                Text("+")
            }
        }
    }
}

private fun buildSetSummary(
    sets: List<ActualExerciseSetLogEntity>,
): String {
    val weights = sets.mapNotNull { it.weight }

    if (sets.isEmpty()) {
        return "Sets: 0"
    }

    if (weights.isEmpty()) {
        return "Sets: ${sets.size}"
    }

    val minWeight = weights.minOrNull()
    val maxWeight = weights.maxOrNull()

    val weightText = if (minWeight == maxWeight) {
        formatWeight(minWeight ?: 0.0)
    } else {
        "${formatWeight(minWeight ?: 0.0)}–${formatWeight(maxWeight ?: 0.0)}"
    }

    return "Sets: ${sets.size} | Weight: $weightText"
}

internal fun formatWeight(
    value: Double,
): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        value.toString()
    }
}