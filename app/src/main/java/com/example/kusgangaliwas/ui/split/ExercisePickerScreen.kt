package com.example.kusgangaliwas.ui.split

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kusgangaliwas.ui.common.KusgangTopBar
import com.example.kusgangaliwas.ui.common.SectionHeader
import com.example.kusgangaliwas.ui.common.SharpCard

@Composable
fun ExercisePickerScreen(
    uiState: ExercisePickerUiState,
    onBackClick: () -> Unit,
    onOverflowClick: () -> Unit,
    onSearchTextChange: (String) -> Unit,
    onToggleExercise: (Long) -> Unit,
    onAddSelectedClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            KusgangTopBar(
                title = uiState.title,
                onBackClick = onBackClick,
                onOverflowClick = onOverflowClick,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = uiState.searchText,
                onValueChange = onSearchTextChange,
                label = {
                    Text("Search exercises")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedButton(
                onClick = onAddSelectedClick,
                enabled = uiState.selectedExerciseIds.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Add selected (${uiState.selectedExerciseIds.size})")
            }

            SharpCard(
                modifier = Modifier.weight(1f),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SectionHeader("Exercises")

                    if (uiState.exercises.isEmpty()) {
                        Text("No matching exercises.")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            items(
                                count = uiState.exercises.size,
                                key = { index ->
                                    uiState.exercises[index].exerciseId
                                },
                            ) { index ->
                                val exercise = uiState.exercises[index]
                                val checked =
                                    exercise.alreadySelected ||
                                            exercise.exerciseId in
                                            uiState.selectedExerciseIds

                                Column {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(
                                                enabled = !exercise.alreadySelected,
                                            ) {
                                                onToggleExercise(exercise.exerciseId)
                                            }
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Checkbox(
                                            checked = checked,
                                            onCheckedChange = {
                                                onToggleExercise(exercise.exerciseId)
                                            },
                                            enabled = !exercise.alreadySelected,
                                        )

                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(1.dp),
                                        ) {
                                            Text(exercise.exerciseName)

                                            Text(
                                                text = exercise.exerciseTypeLabel,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                            if (exercise.supportingText.isNotBlank()) {
                                                Text(
                                                    text = exercise.supportingText,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }
                                            if (exercise.alreadySelected) {
                                                Text(
                                                    text = "Already selected",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
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
}