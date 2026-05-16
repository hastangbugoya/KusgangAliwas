package com.example.kusgangaliwas.ui.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.kusgangaliwas.data.local.entity.ExerciseType
import com.example.kusgangaliwas.data.local.entity.MuscleGroupEntity
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.res.painterResource
import com.example.kusgangaliwas.R
import com.example.kusgangaliwas.data.local.entity.ExerciseEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseListScreen(
    uiState: ExerciseListUiState,
    onBackClick: () -> Unit,
    onOverflowClick: () -> Unit,
    onCreateExercise: (String, ExerciseType) -> Unit,
    onSaveActualOneRepMax: (Long, Double, Long, String?) -> Unit,
    onDeleteActualOneRepMax: (Long) -> Unit,
    modifier: Modifier = Modifier,
    onToggleMuscleGroupForExercise: (Long, Long, Boolean) -> Unit,
    selectedFilterMuscleGroupIds: Set<Long>,
    onToggleFilterMuscleGroup: (Long) -> Unit,
    onClearMuscleGroupFilters: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCreateMuscleGroup: (String) -> Unit,
    onRenameMuscleGroup: (MuscleGroupEntity, String) -> Unit,
    onDeleteMuscleGroup: (Long) -> Unit,
    onRenameExercise: (ExerciseEntity, String) -> Unit,
) {
    var newExerciseName by remember { mutableStateOf("") }
    var selectedExerciseType by remember { mutableStateOf(ExerciseType.STRENGTH) }
    var selectedExerciseId by remember { mutableLongStateOf(0L) }
    val exerciseDetailSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )
    var showAddExerciseSheet by remember { mutableStateOf(false) }
    var localSearchQuery by remember {
        mutableStateOf(searchQuery)
    }
    val selectedExerciseItem = uiState.exercises.firstOrNull { item ->
        item.exercise.id == selectedExerciseId
    }
    var showManageMuscleGroupsSheet by remember { mutableStateOf(false) }

    if (showAddExerciseSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showAddExerciseSheet = false
            },
        ) {
            AddExerciseSheetContent(
                exerciseName = newExerciseName,
                selectedExerciseType = selectedExerciseType,
                onExerciseNameChange = {
                    newExerciseName = it
                },
                onExerciseTypeSelected = {
                    selectedExerciseType = it
                },
                onSave = {
                    val cleaned = newExerciseName
                        .trim()
                        .replaceFirstChar { character ->
                            character.uppercase()
                        }

                    if (cleaned.isNotBlank()) {
                        onCreateExercise(cleaned, selectedExerciseType)

                        newExerciseName = ""
                        selectedExerciseType = ExerciseType.STRENGTH
                        showAddExerciseSheet = false
                    }
                },
            )
        }
    }

    if (selectedExerciseItem != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedExerciseId = 0L },
            sheetState = exerciseDetailSheetState,
        ) {
            ExerciseDetailSheetContent(
                item = selectedExerciseItem,
                onSaveActualOneRepMax = onSaveActualOneRepMax,
                onDeleteActualOneRepMax = onDeleteActualOneRepMax,
                onClose = { selectedExerciseId = 0L },
                availableMuscleGroups = uiState.availableMuscleGroups,
                onToggleMuscleGroupForExercise = onToggleMuscleGroupForExercise,
                onRenameExercise = onRenameExercise,
            )
        }
    }

    if (showManageMuscleGroupsSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showManageMuscleGroupsSheet = false
            },
        ) {
            ManageMuscleGroupsSheetContent(
                muscleGroups = uiState.availableMuscleGroups,
                onCreateMuscleGroup = onCreateMuscleGroup,
                onRenameMuscleGroup = onRenameMuscleGroup,
                onDeleteMuscleGroup = onDeleteMuscleGroup,
            )
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "Exercises") },
                actions = {
                    IconButton(
                        onClick = {
                            showAddExerciseSheet = true
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add exercise",
                        )
                    }

                    IconButton(
                        onClick = {
                            showManageMuscleGroupsSheet = true
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Manage muscle groups",
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
            OutlinedTextField(
                value = localSearchQuery,
                onValueChange = { value ->
                    localSearchQuery = value
                    onSearchQueryChange(value)
                },
                label = { Text("Search exercises") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            MuscleGroupFilterChips(
                muscleGroups = uiState.availableMuscleGroups,
                selectedMuscleGroupIds = selectedFilterMuscleGroupIds,
                onToggleMuscleGroup = onToggleFilterMuscleGroup,
                onClearFilters = onClearMuscleGroupFilters,
            )
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
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Text(
                                            text = exercise.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                        )

                                        Text(
                                            text = "Type: ${exercise.exerciseType.displayText()}",
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }

                                    TextButton(
                                        onClick = { selectedExerciseId = exercise.id },
                                    ) {
                                        Text("Details")
                                    }
                                }

                                item.lastLogDateText?.let { text ->
                                    Text(text = text)
                                }

                                item.lastSetSummaryText?.let { text ->
                                    Text(text = text)
                                }

                                if (exercise.exerciseType == ExerciseType.STRENGTH) {
                                    Text(text = item.estimatedOneRepMaxText ?: "Estimated 1RM: —")
                                    Text(text = item.actualOneRepMaxText ?: "Actual 1RM: —")
                                }

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

@Composable
private fun AddExerciseSheetContent(
    exerciseName: String,
    selectedExerciseType: ExerciseType,
    onExerciseNameChange: (String) -> Unit,
    onExerciseTypeSelected: (ExerciseType) -> Unit,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Add exercise",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        OutlinedTextField(
            value = exerciseName,
            onValueChange = onExerciseNameChange,
            label = { Text("Exercise name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ExerciseType.entries.forEach { type ->
                FilterChip(
                    selected = selectedExerciseType == type,
                    onClick = {
                        onExerciseTypeSelected(type)
                    },
                    label = {
                        Text(type.displayText())
                    },
                )
            }
        }

        OutlinedButton(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save exercise")
        }
    }
}

@Composable
private fun MuscleGroupFilterChips(
    muscleGroups: List<MuscleGroupEntity>,
    selectedMuscleGroupIds: Set<Long>,
    onToggleMuscleGroup: (Long) -> Unit,
    onClearFilters: () -> Unit,
) {
    if (muscleGroups.isEmpty()) return

    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selectedMuscleGroupIds.isEmpty(),
            onClick = onClearFilters,
            label = { Text("All") },
        )

        muscleGroups.forEach { muscleGroup ->
            FilterChip(
                selected = selectedMuscleGroupIds.contains(muscleGroup.id),
                onClick = { onToggleMuscleGroup(muscleGroup.id) },
                label = { Text(muscleGroup.name) },
            )
        }
    }
}

@Composable
private fun ExerciseDetailSheetContent(
    item: ExerciseListItemUiState,
    onSaveActualOneRepMax: (Long, Double, Long, String?) -> Unit,
    onDeleteActualOneRepMax: (Long) -> Unit,
    onClose: () -> Unit,
    availableMuscleGroups: List<MuscleGroupEntity>,
    onToggleMuscleGroupForExercise: (Long, Long, Boolean) -> Unit,
    onRenameExercise: (ExerciseEntity, String) -> Unit,
) {
    val exercise = item.exercise
    val todayText = remember {
        LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    var actualOneRepMaxText by remember(exercise.id, item.actualOneRepMaxText) {
        mutableStateOf(
            item.actualOneRepMaxText
                ?.removePrefix("Actual 1RM: ")
                ?: ""
        )
    }

    var actualOneRepMaxDateText by remember(exercise.id, item.actualOneRepMaxDateText) {
        mutableStateOf(todayText)
    }

    var actualOneRepMaxNotesText by remember(exercise.id) {
        mutableStateOf("")
    }

    var exerciseNameText by remember(exercise.id, exercise.name) {
        mutableStateOf(exercise.name)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .navigationBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = exerciseNameText,
                onValueChange = { exerciseNameText = it },
                label = { Text("Exercise name") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )

            IconButton(
                onClick = {
                    onRenameExercise(exercise, exerciseNameText)
                },
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.floppy_disk_pen),
                    contentDescription = "Update exercise name",
                )
            }
        }

        Text(
            text = "Type: ${exercise.exerciseType.displayText()}",
            style = MaterialTheme.typography.bodyMedium,
        )

        HorizontalDivider()

        Text(
            text = "Muscle groups",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        val sortedMuscleGroups = availableMuscleGroups.sortedWith(
            compareByDescending<MuscleGroupEntity> { muscleGroup ->
                item.selectedMuscleGroupIds.contains(muscleGroup.id)
            }.thenBy { muscleGroup ->
                muscleGroup.name.lowercase()
            }
        )

        if (sortedMuscleGroups.isEmpty()) {
            Text(
                text = "No muscle groups yet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        } else {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                sortedMuscleGroups.forEach { muscleGroup ->
                    val selected = item.selectedMuscleGroupIds.contains(muscleGroup.id)

                    FilterChip(
                        selected = selected,
                        onClick = {
                            onToggleMuscleGroupForExercise(
                                exercise.id,
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

        HorizontalDivider()

        Text(
            text = "Recent performance",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Text(text = item.lastLogDateText ?: "No logged sessions yet.")
        Text(text = item.lastSetSummaryText ?: "No set summary yet.")

        if (exercise.exerciseType == ExerciseType.STRENGTH) {
            HorizontalDivider()

            Text(
                text = "Strength records",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Text(text = item.latestMaxWeightText ?: "Latest max weight: —")
            Text(text = item.estimatedOneRepMaxText ?: "Estimated 1RM: —")
            Text(text = item.actualOneRepMaxText ?: "Actual 1RM: —")
            Text(text = item.actualOneRepMaxDateText ?: "Actual 1RM date: —")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = actualOneRepMaxText,
                    onValueChange = { actualOneRepMaxText = it },
                    label = { Text("Actual 1RM") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                    ),
                    modifier = Modifier.weight(1f),
                )

                OutlinedTextField(
                    value = actualOneRepMaxDateText,
                    onValueChange = { actualOneRepMaxDateText = it },
                    label = { Text("Date") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }

            OutlinedTextField(
                value = actualOneRepMaxNotesText,
                onValueChange = { actualOneRepMaxNotesText = it },
                label = { Text("Notes optional") },
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = {
                        val value = actualOneRepMaxText.toDoubleOrNull()
                        val date = runCatching {
                            LocalDate.parse(actualOneRepMaxDateText)
                        }.getOrNull()

                        if (value != null && date != null) {
                            val achievedAtEpochMillis = date
                                .atStartOfDay(ZoneId.systemDefault())
                                .toInstant()
                                .toEpochMilli()

                            onSaveActualOneRepMax(
                                exercise.id,
                                value,
                                achievedAtEpochMillis,
                                actualOneRepMaxNotesText,
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Save 1RM")
                }

                OutlinedButton(
                    onClick = {
                        onDeleteActualOneRepMax(exercise.id)
                        actualOneRepMaxText = ""
                        actualOneRepMaxNotesText = ""
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Delete 1RM")
                }
            }
        }

        HorizontalDivider()

        Text(
            text = "Last 10 trend",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        ExerciseHistoryGraph(
            historyPoints = item.historyPoints,
            historyTrendText = item.historyTrendText,
        )

        if (!exercise.notes.isNullOrBlank()) {
            HorizontalDivider()

            Text(
                text = "Notes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Text(text = exercise.notes)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onClose) {
                Text("Close")
            }
        }
    }
}

@Composable
private fun ExerciseHistoryGraph(
    historyPoints: List<ExerciseHistoryPointUiState>,
    historyTrendText: String?,
) {
    if (historyPoints.isEmpty()) {
        Text(
            text = "No history yet.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
        )
        return
    }

    val maxWeight = historyPoints
        .maxOfOrNull { point -> point.maxWeight }
        ?.takeIf { it > 0.0 }
        ?: 1.0

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        historyTrendText?.let { text ->
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            historyPoints.forEach { point ->
                val barHeight = ((point.maxWeight / maxWeight) * 72.0)
                    .coerceAtLeast(12.0)
                    .dp

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = formatGraphWeight(point.maxWeight),
                        style = MaterialTheme.typography.labelSmall,
                    )

                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(barHeight)
                            .background(MaterialTheme.colorScheme.primary),
                    )

                    Text(
                        text = point.label,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun ManageMuscleGroupsSheetContent(
    muscleGroups: List<MuscleGroupEntity>,
    onCreateMuscleGroup: (String) -> Unit,
    onRenameMuscleGroup: (MuscleGroupEntity, String) -> Unit,
    onDeleteMuscleGroup: (Long) -> Unit,
) {
    var newName by remember { mutableStateOf("") }

    var searchText by remember { mutableStateOf("") }

    val filteredGroups = muscleGroups.filter { group ->
        searchText.isBlank() ||
                group.name.contains(searchText.trim(), ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Manage muscle groups",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        OutlinedTextField(
            value = newName,
            onValueChange = { newName = it },
            label = { Text("New muscle group") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedButton(
            onClick = {
                onCreateMuscleGroup(newName)
                newName = ""
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Add muscle group")
        }

        HorizontalDivider()

        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Search muscle groups") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        filteredGroups
            .filter { it.name.isNotBlank() }
            .forEach { muscleGroup ->
            MuscleGroupEditRow(
                muscleGroup = muscleGroup,
                onRenameMuscleGroup = onRenameMuscleGroup,
                onDeleteMuscleGroup = onDeleteMuscleGroup,
            )
        }
    }
}

@Composable
private fun MuscleGroupEditRow(
    muscleGroup: MuscleGroupEntity,
    onRenameMuscleGroup: (MuscleGroupEntity, String) -> Unit,
    onDeleteMuscleGroup: (Long) -> Unit,
) {
    var nameText by remember(muscleGroup.id, muscleGroup.name) {
        mutableStateOf(muscleGroup.name)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = nameText,
            onValueChange = { nameText = it },
            label = { Text("Name") },
            singleLine = true,
            modifier = Modifier.weight(1f),
        )

        IconButton(
            onClick = {
                onRenameMuscleGroup(
                    muscleGroup,
                    nameText,
                )
            },
        ) {
            Icon(
                painter = painterResource(id = R.drawable.floppy_disk_pen),
                contentDescription = "Update muscle group",
            )
        }

        IconButton(
            onClick = {
                onDeleteMuscleGroup(muscleGroup.id)
            },
        ) {
            Icon(
                painter = painterResource(id = R.drawable.trash),
                contentDescription = "Delete muscle group",
            )
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

private fun formatGraphWeight(
    value: Double,
): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        value.toString()
    }
}