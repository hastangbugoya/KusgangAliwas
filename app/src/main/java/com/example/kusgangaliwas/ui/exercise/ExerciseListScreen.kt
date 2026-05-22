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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.kusgangaliwas.R
import com.example.kusgangaliwas.data.local.entity.ExerciseEntity
import com.example.kusgangaliwas.data.local.entity.ExercisePaceProfileEntity
import com.example.kusgangaliwas.data.local.entity.ExerciseType
import com.example.kusgangaliwas.data.local.entity.MuscleGroupEntity
import com.example.kusgangaliwas.ui.common.MuscleGroupChipRow
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
    onCreatePaceProfile: (
        Long,
        String,
        Boolean,
        Boolean,
        Int,
        Int,
        Int,
        Int,
        Int,
        Boolean,
        Boolean,
    ) -> Unit,
    onUpdatePaceProfile: (
        ExercisePaceProfileEntity,
        String,
        Boolean,
        Boolean,
        Int,
        Int,
        Int,
        Int,
        Int,
        Boolean,
        Boolean,
    ) -> Unit,
    onSetPaceProfileAsDefault: (ExercisePaceProfileEntity) -> Unit,
    onTogglePaceProfileEnabled: (ExercisePaceProfileEntity) -> Unit,
    onDeletePaceProfile: (ExercisePaceProfileEntity) -> Unit,
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
                onCreatePaceProfile = onCreatePaceProfile,
                onUpdatePaceProfile = onUpdatePaceProfile,
                onSetPaceProfileAsDefault = onSetPaceProfileAsDefault,
                onTogglePaceProfileEnabled = onTogglePaceProfileEnabled,
                onDeletePaceProfile = onDeletePaceProfile,
                errorMessage = uiState.errorMessage,
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
                            painter = painterResource(R.drawable.plus),
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

            MuscleGroupChipRow(
                muscleGroups = uiState.availableMuscleGroups,
                selectedMuscleGroupIds = selectedFilterMuscleGroupIds,
                allLabel = "All",
                onClearSelection = onClearMuscleGroupFilters,
                onToggleMuscleGroup = { muscleGroupId, _ ->
                    onToggleFilterMuscleGroup(muscleGroupId)
                },
            )

            uiState.errorMessage?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraSmall,
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
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

                                        val paceSummary = buildPaceSummaryText(item.paceProfiles)
                                        if (paceSummary != null) {
                                            Text(
                                                text = paceSummary,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.secondary,
                                            )
                                        }
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
private fun ExerciseDetailSheetContent(
    item: ExerciseListItemUiState,
    errorMessage: String?,
    onSaveActualOneRepMax: (Long, Double, Long, String?) -> Unit,
    onDeleteActualOneRepMax: (Long) -> Unit,
    onClose: () -> Unit,
    availableMuscleGroups: List<MuscleGroupEntity>,
    onToggleMuscleGroupForExercise: (Long, Long, Boolean) -> Unit,
    onRenameExercise: (ExerciseEntity, String) -> Unit,
    onCreatePaceProfile: (
        Long,
        String,
        Boolean,
        Boolean,
        Int,
        Int,
        Int,
        Int,
        Int,
        Boolean,
        Boolean,
    ) -> Unit,
    onUpdatePaceProfile: (
        ExercisePaceProfileEntity,
        String,
        Boolean,
        Boolean,
        Int,
        Int,
        Int,
        Int,
        Int,
        Boolean,
        Boolean,
    ) -> Unit,
    onSetPaceProfileAsDefault: (ExercisePaceProfileEntity) -> Unit,
    onTogglePaceProfileEnabled: (ExercisePaceProfileEntity) -> Unit,
    onDeletePaceProfile: (ExercisePaceProfileEntity) -> Unit,
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

        if (availableMuscleGroups.isEmpty()) {
            Text(
                text = "No muscle groups yet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        } else {
            MuscleGroupChipRow(
                muscleGroups = availableMuscleGroups,
                selectedMuscleGroupIds = item.selectedMuscleGroupIds,
                onToggleMuscleGroup = { muscleGroupId, isSelected ->
                    onToggleMuscleGroupForExercise(
                        exercise.id,
                        muscleGroupId,
                        isSelected,
                    )
                },
            )
        }

        HorizontalDivider()

        PaceProfilesSection(
            exerciseId = exercise.id,
            paceProfiles = item.paceProfiles,
            onCreatePaceProfile = onCreatePaceProfile,
            errorMessage = errorMessage,
            onUpdatePaceProfile = onUpdatePaceProfile,
            onSetPaceProfileAsDefault = onSetPaceProfileAsDefault,
            onTogglePaceProfileEnabled = onTogglePaceProfileEnabled,
            onDeletePaceProfile = onDeletePaceProfile,
        )

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
private fun PaceProfilesSection(
    exerciseId: Long,
    paceProfiles: List<ExercisePaceProfileEntity>,
    errorMessage: String?,
    onCreatePaceProfile: (
        Long,
        String,
        Boolean,
        Boolean,
        Int,
        Int,
        Int,
        Int,
        Int,
        Boolean,
        Boolean,
    ) -> Unit,
    onUpdatePaceProfile: (
        ExercisePaceProfileEntity,
        String,
        Boolean,
        Boolean,
        Int,
        Int,
        Int,
        Int,
        Int,
        Boolean,
        Boolean,
    ) -> Unit,
    onSetPaceProfileAsDefault: (ExercisePaceProfileEntity) -> Unit,
    onTogglePaceProfileEnabled: (ExercisePaceProfileEntity) -> Unit,
    onDeletePaceProfile: (ExercisePaceProfileEntity) -> Unit,
) {
    var showAddPaceProfileForm by remember(exerciseId) { mutableStateOf(false) }
    var newName by remember(exerciseId) { mutableStateOf("") }
    var newIsDefault by remember(exerciseId) { mutableStateOf(false) }
    var newIsEnabled by remember(exerciseId) { mutableStateOf(true) }
    var newPrepLeadSeconds by remember(exerciseId) { mutableStateOf("0") }
    var newExpectedWorkSeconds by remember(exerciseId) { mutableStateOf("0") }
    var newExpectedRestSeconds by remember(exerciseId) { mutableStateOf("0") }
    var newNextSetWarningSeconds by remember(exerciseId) { mutableStateOf("0") }
    var newIdleReminderIntervalSeconds by remember(exerciseId) { mutableStateOf("0") }
    var newIdleReminderEnabled by remember(exerciseId) { mutableStateOf(false) }
    var newEtiquetteReminderEnabled by remember(exerciseId) { mutableStateOf(false) }
    var previousPaceProfileCount by remember(exerciseId) {
        mutableStateOf(paceProfiles.size)
    }

    LaunchedEffect(exerciseId, paceProfiles.size) {
        val profileWasAdded = paceProfiles.size > previousPaceProfileCount

        if (profileWasAdded) {
            newName = ""
            newIsDefault = false
            newIsEnabled = true
            newPrepLeadSeconds = "0"
            newExpectedWorkSeconds = "0"
            newExpectedRestSeconds = "0"
            newNextSetWarningSeconds = "0"
            newIdleReminderIntervalSeconds = "0"
            newIdleReminderEnabled = false
            newEtiquetteReminderEnabled = false
            showAddPaceProfileForm = false
        }

        previousPaceProfileCount = paceProfiles.size
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Pace profiles",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "Optional gentle timing nudges. Leave disabled or use 0 seconds for no nudge.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
        )

        errorMessage?.let { message ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraSmall,
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        if (paceProfiles.isEmpty()) {
            Text(
                text = "No pace profiles yet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        } else {
            val hasDefaultProfile = paceProfiles.any { profile ->
                profile.isDefault
            }

            if (!hasDefaultProfile) {
                Text(
                    text = "This exercise has pace profiles, but no default is set.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }

            paceProfiles.forEach { profile ->
                PaceProfileEditCard(
                    profile = profile,
                    onUpdatePaceProfile = onUpdatePaceProfile,
                    onSetPaceProfileAsDefault = onSetPaceProfileAsDefault,
                    onTogglePaceProfileEnabled = onTogglePaceProfileEnabled,
                    onDeletePaceProfile = onDeletePaceProfile,
                )
            }
        }

        HorizontalDivider()

        if (!showAddPaceProfileForm) {
            OutlinedButton(
                onClick = {
                    showAddPaceProfileForm = true
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Add another pace profile")
            }
        } else {
            Text(
                text = "Add pace profile",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )

            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Profile name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            PaceSwitchRow(
                label = "Default for this exercise",
                checked = newIsDefault,
                onCheckedChange = { newIsDefault = it },
            )

            PaceSwitchRow(
                label = "Pace enabled",
                checked = newIsEnabled,
                onCheckedChange = { newIsEnabled = it },
            )

            PaceTimingFields(
                prepLeadSeconds = newPrepLeadSeconds,
                onPrepLeadSecondsChange = { newPrepLeadSeconds = it },
                expectedWorkSeconds = newExpectedWorkSeconds,
                onExpectedWorkSecondsChange = { newExpectedWorkSeconds = it },
                expectedRestSeconds = newExpectedRestSeconds,
                onExpectedRestSecondsChange = { newExpectedRestSeconds = it },
                nextSetWarningSeconds = newNextSetWarningSeconds,
                onNextSetWarningSecondsChange = { newNextSetWarningSeconds = it },
                idleReminderIntervalSeconds = newIdleReminderIntervalSeconds,
                onIdleReminderIntervalSecondsChange = {
                    newIdleReminderIntervalSeconds = it
                },
            )

            PaceSwitchRow(
                label = "Idle reminders",
                checked = newIdleReminderEnabled,
                onCheckedChange = { newIdleReminderEnabled = it },
            )

            PaceSwitchRow(
                label = "Equipment etiquette reminder",
                checked = newEtiquetteReminderEnabled,
                onCheckedChange = { newEtiquetteReminderEnabled = it },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = {
                        showAddPaceProfileForm = false
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Cancel")
                }

                OutlinedButton(
                    onClick = {
                        onCreatePaceProfile(
                            exerciseId,
                            newName,
                            newIsDefault,
                            newIsEnabled,
                            secondsFromText(newPrepLeadSeconds),
                            secondsFromText(newExpectedWorkSeconds),
                            secondsFromText(newExpectedRestSeconds),
                            secondsFromText(newNextSetWarningSeconds),
                            secondsFromText(newIdleReminderIntervalSeconds),
                            newIdleReminderEnabled,
                            newEtiquetteReminderEnabled,
                        )
                    },
                    enabled = newName.isNotBlank(),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
private fun PaceProfileEditCard(
    profile: ExercisePaceProfileEntity,
    onUpdatePaceProfile: (
        ExercisePaceProfileEntity,
        String,
        Boolean,
        Boolean,
        Int,
        Int,
        Int,
        Int,
        Int,
        Boolean,
        Boolean,
    ) -> Unit,
    onSetPaceProfileAsDefault: (ExercisePaceProfileEntity) -> Unit,
    onTogglePaceProfileEnabled: (ExercisePaceProfileEntity) -> Unit,
    onDeletePaceProfile: (ExercisePaceProfileEntity) -> Unit,
) {
    var expanded by remember(profile.id) {
        mutableStateOf(false)
    }
    var name by remember(profile.id, profile.name) {
        mutableStateOf(profile.name)
    }
    var isDefault by remember(profile.id, profile.isDefault) {
        mutableStateOf(profile.isDefault)
    }
    var isEnabled by remember(profile.id, profile.isEnabled) {
        mutableStateOf(profile.isEnabled)
    }
    var prepLeadSeconds by remember(profile.id, profile.prepLeadSeconds) {
        mutableStateOf(profile.prepLeadSeconds.toString())
    }
    var expectedWorkSeconds by remember(profile.id, profile.expectedWorkSeconds) {
        mutableStateOf(profile.expectedWorkSeconds.toString())
    }
    var expectedRestSeconds by remember(profile.id, profile.expectedRestSeconds) {
        mutableStateOf(profile.expectedRestSeconds.toString())
    }
    var nextSetWarningSeconds by remember(profile.id, profile.nextSetWarningSeconds) {
        mutableStateOf(profile.nextSetWarningSeconds.toString())
    }
    var idleReminderIntervalSeconds by remember(
        profile.id,
        profile.idleReminderIntervalSeconds,
    ) {
        mutableStateOf(profile.idleReminderIntervalSeconds.toString())
    }
    var idleReminderEnabled by remember(profile.id, profile.idleReminderEnabled) {
        mutableStateOf(profile.idleReminderEnabled)
    }
    var etiquetteReminderEnabled by remember(
        profile.id,
        profile.etiquetteReminderEnabled,
    ) {
        mutableStateOf(profile.etiquetteReminderEnabled)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraSmall,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = {
                    Text(
                        text = if (profile.isDefault) {
                            "Profile name (default)"
                        } else {
                            "Profile name"
                        },
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (profile.isEnabled) {
                        buildProfileTimingSummary(profile)
                    } else {
                        "Disabled"
                    },
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )

                IconButton(
                    onClick = {
                        expanded = !expanded
                    },
                ) {
                    Icon(
                        imageVector = if (expanded) {
                            Icons.Default.KeyboardArrowUp
                        } else {
                            Icons.Default.KeyboardArrowDown
                        },
                        contentDescription = if (expanded) {
                            "Collapse pace profile"
                        } else {
                            "Expand pace profile"
                        },
                    )
                }
            }

            if (expanded) {
                PaceSwitchRow(
                    label = "Default for this exercise",
                    checked = isDefault,
                    onCheckedChange = { isDefault = it },
                )

                PaceSwitchRow(
                    label = "Pace enabled",
                    checked = isEnabled,
                    onCheckedChange = { isEnabled = it },
                )

                PaceTimingFields(
                    prepLeadSeconds = prepLeadSeconds,
                    onPrepLeadSecondsChange = { prepLeadSeconds = it },
                    expectedWorkSeconds = expectedWorkSeconds,
                    onExpectedWorkSecondsChange = { expectedWorkSeconds = it },
                    expectedRestSeconds = expectedRestSeconds,
                    onExpectedRestSecondsChange = { expectedRestSeconds = it },
                    nextSetWarningSeconds = nextSetWarningSeconds,
                    onNextSetWarningSecondsChange = { nextSetWarningSeconds = it },
                    idleReminderIntervalSeconds = idleReminderIntervalSeconds,
                    onIdleReminderIntervalSecondsChange = {
                        idleReminderIntervalSeconds = it
                    },
                )

                PaceSwitchRow(
                    label = "Idle reminders",
                    checked = idleReminderEnabled,
                    onCheckedChange = { idleReminderEnabled = it },
                )

                PaceSwitchRow(
                    label = "Equipment etiquette reminder",
                    checked = etiquetteReminderEnabled,
                    onCheckedChange = { etiquetteReminderEnabled = it },
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = {
                            onUpdatePaceProfile(
                                profile,
                                name,
                                isDefault,
                                isEnabled,
                                secondsFromText(prepLeadSeconds),
                                secondsFromText(expectedWorkSeconds),
                                secondsFromText(expectedRestSeconds),
                                secondsFromText(nextSetWarningSeconds),
                                secondsFromText(idleReminderIntervalSeconds),
                                idleReminderEnabled,
                                etiquetteReminderEnabled,
                            )
                        },
                    ) {
                        Text("Save changes")
                    }

                    OutlinedButton(
                        onClick = {
                            onSetPaceProfileAsDefault(profile)
                            isDefault = true
                        },
                        enabled = !profile.isDefault,
                    ) {
                        Text("Set default")
                    }

                    OutlinedButton(
                        onClick = {
                            onTogglePaceProfileEnabled(profile)
                            isEnabled = !isEnabled
                        },
                    ) {
                        Text(if (profile.isEnabled) "Disable" else "Enable")
                    }

                    OutlinedButton(
                        onClick = {
                            onDeletePaceProfile(profile)
                        },
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Composable
private fun PaceTimingFields(
    prepLeadSeconds: String,
    onPrepLeadSecondsChange: (String) -> Unit,
    expectedWorkSeconds: String,
    onExpectedWorkSecondsChange: (String) -> Unit,
    expectedRestSeconds: String,
    onExpectedRestSecondsChange: (String) -> Unit,
    nextSetWarningSeconds: String,
    onNextSetWarningSecondsChange: (String) -> Unit,
    idleReminderIntervalSeconds: String,
    onIdleReminderIntervalSecondsChange: (String) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SecondsTextField(
                value = prepLeadSeconds,
                onValueChange = onPrepLeadSecondsChange,
                label = "Prep lead",
                modifier = Modifier.weight(1f),
            )

            SecondsTextField(
                value = expectedWorkSeconds,
                onValueChange = onExpectedWorkSecondsChange,
                label = "Work",
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SecondsTextField(
                value = expectedRestSeconds,
                onValueChange = onExpectedRestSecondsChange,
                label = "Rest",
                modifier = Modifier.weight(1f),
            )

            SecondsTextField(
                value = nextSetWarningSeconds,
                onValueChange = onNextSetWarningSecondsChange,
                label = "Warning",
                modifier = Modifier.weight(1f),
            )
        }

        SecondsTextField(
            value = idleReminderIntervalSeconds,
            onValueChange = onIdleReminderIntervalSecondsChange,
            label = "Idle reminder interval",
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SecondsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("$label seconds") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
        ),
        modifier = modifier,
    )
}

@Composable
private fun PaceSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
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

private fun buildPaceSummaryText(
    paceProfiles: List<ExercisePaceProfileEntity>,
): String? {
    if (paceProfiles.isEmpty()) {
        return null
    }

    val defaultProfile = paceProfiles.firstOrNull { profile ->
        profile.isDefault
    }

    return if (defaultProfile != null) {
        "Pace: ${defaultProfile.name}"
    } else {
        "Pace profiles: ${paceProfiles.size} · no default set"
    }
}

private fun buildProfileTimingSummary(
    profile: ExercisePaceProfileEntity,
): String {
    val parts = buildList {
        if (profile.prepLeadSeconds > 0) {
            add("prep ${profile.prepLeadSeconds}s")
        }

        if (profile.expectedWorkSeconds > 0) {
            add("work ${profile.expectedWorkSeconds}s")
        }

        if (profile.expectedRestSeconds > 0) {
            add("rest ${profile.expectedRestSeconds}s")
        }

        if (profile.nextSetWarningSeconds > 0) {
            add("warning ${profile.nextSetWarningSeconds}s")
        }

        if (profile.idleReminderEnabled && profile.idleReminderIntervalSeconds > 0) {
            add("idle every ${profile.idleReminderIntervalSeconds}s")
        }

        if (profile.etiquetteReminderEnabled) {
            add("etiquette")
        }
    }

    return if (parts.isEmpty()) {
        "No timed nudges"
    } else {
        parts.joinToString(" · ")
    }
}

private fun secondsFromText(
    value: String,
): Int {
    return value
        .trim()
        .toIntOrNull()
        ?.coerceAtLeast(0)
        ?: 0
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