package com.example.kusgangaliwas.ui.exercise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kusgangaliwas.data.local.entity.ExerciseType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseListScreen(
    uiState: ExerciseListUiState,
    onBackClick: () -> Unit,
    onOverflowClick: () -> Unit,
    onCreateExercise: (String, ExerciseType) -> Unit,
    modifier: Modifier = Modifier,
) {
    var newExerciseName by remember { mutableStateOf("") }
    var selectedExerciseType by remember { mutableStateOf(ExerciseType.STRENGTH) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Exercises")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraSmall,
                colors = CardDefaults.cardColors(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Add exercise",
                        style = MaterialTheme.typography.titleMedium,
                    )

                    OutlinedTextField(
                        value = newExerciseName,
                        onValueChange = { newExerciseName = it },
                        label = {
                            Text(text = "Exercise name")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Text(
                        text = "Type",
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ExerciseType.entries.forEach { type ->
                            OutlinedButton(
                                onClick = {
                                    selectedExerciseType = type
                                },
                            ) {
                                Text(
                                    text = if (selectedExerciseType == type) {
                                        "✓ ${type.displayText()}"
                                    } else {
                                        type.displayText()
                                    },
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            val cleaned = newExerciseName.trim()
                            if (cleaned.isNotBlank()) {
                                onCreateExercise(cleaned, selectedExerciseType)
                                newExerciseName = ""
                                selectedExerciseType = ExerciseType.STRENGTH
                            }
                        },
                    ) {
                        Text(text = "Add")
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (uiState.exercises.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.extraSmall,
                        ) {
                            Text(
                                text = "No exercises yet.",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                } else {
                    items(
                        items = uiState.exercises,
                        key = { item -> item.exercise.id },
                    ) { item ->
                        val exercise = item.exercise

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.extraSmall,
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = exercise.name,
                                    style = MaterialTheme.typography.titleMedium,
                                )

                                Text(
                                    text = "Type: ${exercise.exerciseType.displayText()}",
                                    style = MaterialTheme.typography.bodySmall,
                                )

                                item.lastLogDateText?.let { text ->
                                    Text(text = text)
                                }

                                item.lastSetSummaryText?.let { text ->
                                    Text(text = text)
                                }

                                Text(text = "1RM: —")

                                if (!exercise.notes.isNullOrBlank()) {
                                    Text(
                                        text = exercise.notes,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
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