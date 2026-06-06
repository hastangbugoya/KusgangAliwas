package com.example.kusgangaliwas.ui.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.SwitchDefaults
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
import com.example.kusgangaliwas.data.local.entity.ExerciseMotivationalGoalEntity
import com.example.kusgangaliwas.data.local.entity.ExerciseMotivationalGoalType
import com.example.kusgangaliwas.data.local.entity.ExercisePaceProfileEntity
import com.example.kusgangaliwas.data.local.entity.ExerciseType
import com.example.kusgangaliwas.data.local.entity.MuscleGroupEntity
import com.example.kusgangaliwas.ui.common.MuscleGroupChipRow
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.example.kusgangaliwas.ui.theme.KaPalette

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
    onCreateMotivationalGoal: (
        Long,
        ExerciseMotivationalGoalType,
        String,
        Double?,
        Int?,
        Double?,
        Double?,
        String?,
        Int?,
        String?,
    ) -> Unit,
    onDeactivateMotivationalGoal: (ExerciseMotivationalGoalEntity) -> Unit,
    onRestoreMotivationalGoal: (ExerciseMotivationalGoalEntity) -> Unit,
    onDeleteMotivationalGoal: (ExerciseMotivationalGoalEntity) -> Unit,
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
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
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
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            ExerciseDetailSheetContent(
                item = selectedExerciseItem,
                onSaveActualOneRepMax = onSaveActualOneRepMax,
                onDeleteActualOneRepMax = onDeleteActualOneRepMax,
                onClose = { selectedExerciseId = 0L },
                availableMuscleGroups = uiState.availableMuscleGroups,
                onToggleMuscleGroupForExercise = onToggleMuscleGroupForExercise,
                onRenameExercise = onRenameExercise,
                onCreateMotivationalGoal = onCreateMotivationalGoal,
                onDeactivateMotivationalGoal = onDeactivateMotivationalGoal,
                onRestoreMotivationalGoal = onRestoreMotivationalGoal,
                onDeleteMotivationalGoal = onDeleteMotivationalGoal,
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
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Exercises",
                        fontWeight = FontWeight.Bold,
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            showAddExerciseSheet = true
                        },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.plus),
                            contentDescription = "Add exercise",
                            tint = KaPalette.SteelBlue,
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
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
                KaErrorCard(message = message)
            }

            KaRootSectionCard(
                title = "Exercise library",
                count = uiState.exercises.size,
                accentColor = KaPalette.SteelBlue,
                modifier = Modifier.weight(1f),
            ) {
                if (uiState.exercises.isEmpty()) {
                    EmptyExerciseState(
                        onAddClick = {
                            showAddExerciseSheet = true
                        },
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(
                            items = uiState.exercises,
                            key = { item -> item.exercise.id },
                        ) { item ->
                            ExerciseRowCard(
                                item = item,
                                onClick = {
                                    selectedExerciseId = item.exercise.id
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KaRootSectionCard(
    title: String,
    count: Int? = null,
    accentColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
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
                        .height(32.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(accentColor.copy(alpha = 0.85f)),
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
            }

            content()
        }
    }
}

@Composable
private fun ExerciseRowCard(
    item: ExerciseListItemUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val exercise = item.exercise
    val accentColor = exercise.exerciseType.accentColor()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
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

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TypePill(
                            text = exercise.exerciseType.displayText(),
                            color = accentColor,
                        )

                        item.lastLogDateText?.let { text ->
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Text(
                    text = "Details",
                    style = MaterialTheme.typography.labelLarge,
                    color = KaPalette.SteelBlue,
                )
            }

            item.lastSetSummaryText?.let { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            val goalSummary = buildMotivationalGoalListSummary(
                item.motivationalGoals,
            )

            if (goalSummary != null) {
                Text(
                    text = goalSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = KaPalette.Purple,
                )
            }

            val paceSummary = buildPaceSummaryText(item.paceProfiles)
            if (paceSummary != null) {
                Text(
                    text = paceSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = KaPalette.Amber,
                )
            }

            if (!exercise.notes.isNullOrBlank()) {
                Text(
                    text = exercise.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
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
private fun EmptyExerciseState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "No exercises yet.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Icon(
                painter = painterResource(R.drawable.plus),
                contentDescription = null,
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text("Add exercise")
        }
    }
}

@Composable
private fun KaErrorCard(
    message: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = KaPalette.DangerContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = KaPalette.Danger.copy(alpha = 0.45f),
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

@Composable
private fun ExerciseType.accentColor(): Color {
    return when (this) {
        ExerciseType.STRENGTH -> KaPalette.SteelBlue
        ExerciseType.CARDIO -> KaPalette.Success
        ExerciseType.MOBILITY -> KaPalette.Purple
        ExerciseType.OTHER -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}



@Composable
private fun KaDetailSectionCard(
    title: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    count: Int? = null,
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
            color = accentColor.copy(alpha = 0.20f),
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
                        .height(32.dp)
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
            }

            content()
        }
    }
}

@Composable
private fun KaDetailInnerCard(
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
private fun KaDetailFilterChip(
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
private fun DetailInfoText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun DetailMetricLine(
    text: String,
    accentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        color = accentColor,
        fontWeight = FontWeight.SemiBold,
    )
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
                val selected = selectedExerciseType == type
                val accentColor = type.accentColor()

                FilterChip(
                    selected = selected,
                    onClick = {
                        onExerciseTypeSelected(type)
                    },
                    label = {
                        Text(type.displayText())
                    },
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
        }

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Icon(
                painter = painterResource(R.drawable.plus),
                contentDescription = null,
            )

            Spacer(modifier = Modifier.width(8.dp))

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
    onCreateMotivationalGoal: (
        Long,
        ExerciseMotivationalGoalType,
        String,
        Double?,
        Int?,
        Double?,
        Double?,
        String?,
        Int?,
        String?,
    ) -> Unit,
    onDeactivateMotivationalGoal: (ExerciseMotivationalGoalEntity) -> Unit,
    onRestoreMotivationalGoal: (ExerciseMotivationalGoalEntity) -> Unit,
    onDeleteMotivationalGoal: (ExerciseMotivationalGoalEntity) -> Unit,
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
    val typeAccentColor = exercise.exerciseType.accentColor()
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
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        KaDetailSectionCard(
            title = "Exercise",
            accentColor = typeAccentColor,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                            tint = typeAccentColor,
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TypePill(
                        text = exercise.exerciseType.displayText(),
                        color = typeAccentColor,
                    )

                    DetailInfoText("Exercise detail")
                }
            }
        }

        KaDetailSectionCard(
            title = "Muscle groups",
            count = item.selectedMuscleGroupIds.size.takeIf { availableMuscleGroups.isNotEmpty() },
            accentColor = KaPalette.SteelBlue,
        ) {
            if (availableMuscleGroups.isEmpty()) {
                DetailInfoText("No muscle groups yet.")
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
        }

        MotivationalGoalsSection(
            exerciseId = exercise.id,
            motivationalGoals = item.motivationalGoals,
            hiddenMotivationalGoals = item.hiddenMotivationalGoals,
            onCreateMotivationalGoal = onCreateMotivationalGoal,
            onDeactivateMotivationalGoal = onDeactivateMotivationalGoal,
            onRestoreMotivationalGoal = onRestoreMotivationalGoal,
            onDeleteMotivationalGoal = onDeleteMotivationalGoal,
            errorMessage = errorMessage,
        )

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

        KaDetailSectionCard(
            title = "Recent performance",
            accentColor = KaPalette.Success,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                DetailMetricLine(
                    text = item.lastLogDateText ?: "No logged sessions yet.",
                    accentColor = MaterialTheme.colorScheme.onSurface,
                )
                DetailInfoText(item.lastSetSummaryText ?: "No set summary yet.")
            }
        }

        if (exercise.exerciseType == ExerciseType.STRENGTH) {
            KaDetailSectionCard(
                title = "Strength records",
                accentColor = KaPalette.SteelBlue,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    KaDetailInnerCard(
                        borderColor = KaPalette.SteelBlue.copy(alpha = 0.28f),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            DetailInfoText(item.latestMaxWeightText ?: "Latest max weight: —")
                            DetailInfoText(item.estimatedOneRepMaxText ?: "Estimated 1RM: —")
                            DetailInfoText(item.actualOneRepMaxText ?: "Actual 1RM: —")
                            DetailInfoText(item.actualOneRepMaxDateText ?: "Actual 1RM date: —")
                        }
                    }

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
                        Button(
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
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
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
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = KaPalette.Danger,
                            ),
                        ) {
                            Text("Delete 1RM")
                        }
                    }
                }
            }
        }

        KaDetailSectionCard(
            title = "Last 10 trend",
            accentColor = KaPalette.Amber,
        ) {
            ExerciseHistoryGraph(
                historyPoints = item.historyPoints,
                historyTrendText = item.historyTrendText,
            )
        }

        if (!exercise.notes.isNullOrBlank()) {
            KaDetailSectionCard(
                title = "Notes",
                accentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ) {
                DetailInfoText(exercise.notes)
            }
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
private fun MotivationalGoalsSection(
    exerciseId: Long,
    motivationalGoals: List<ExerciseMotivationalGoalEntity>,
    hiddenMotivationalGoals: List<ExerciseMotivationalGoalEntity>,
    errorMessage: String?,
    onCreateMotivationalGoal: (
        Long,
        ExerciseMotivationalGoalType,
        String,
        Double?,
        Int?,
        Double?,
        Double?,
        String?,
        Int?,
        String?,
    ) -> Unit,
    onDeactivateMotivationalGoal: (ExerciseMotivationalGoalEntity) -> Unit,
    onRestoreMotivationalGoal: (ExerciseMotivationalGoalEntity) -> Unit,
    onDeleteMotivationalGoal: (ExerciseMotivationalGoalEntity) -> Unit,
) {
    var showAddGoalForm by remember(exerciseId) { mutableStateOf(false) }
    var selectedGoalType by remember(exerciseId) {
        mutableStateOf(ExerciseMotivationalGoalType.WEIGHT_REPS)
    }
    var titleText by remember(exerciseId) { mutableStateOf("") }
    var targetWeightText by remember(exerciseId) { mutableStateOf("") }
    var targetRepsText by remember(exerciseId) { mutableStateOf("") }
    var targetOneRepMaxText by remember(exerciseId) { mutableStateOf("") }
    var targetDistanceText by remember(exerciseId) { mutableStateOf("") }
    var targetDistanceUnitText by remember(exerciseId) { mutableStateOf("mi") }
    var targetDurationMinutesText by remember(exerciseId) { mutableStateOf("") }
    var notesText by remember(exerciseId) { mutableStateOf("") }
    var previousGoalCount by remember(exerciseId) {
        mutableStateOf(motivationalGoals.size)
    }

    LaunchedEffect(exerciseId, motivationalGoals.size) {
        val goalWasAdded = motivationalGoals.size > previousGoalCount

        if (goalWasAdded) {
            selectedGoalType = ExerciseMotivationalGoalType.WEIGHT_REPS
            titleText = ""
            targetWeightText = ""
            targetRepsText = ""
            targetOneRepMaxText = ""
            targetDistanceText = ""
            targetDistanceUnitText = "mi"
            targetDurationMinutesText = ""
            notesText = ""
            showAddGoalForm = false
        }

        previousGoalCount = motivationalGoals.size
    }

    KaDetailSectionCard(
        title = "Motivational goals",
        count = motivationalGoals.size + hiddenMotivationalGoals.size,
        accentColor = KaPalette.Purple,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            DetailInfoText(
                text = "Optional targets to keep in mind. These do not score or judge the workout.",
            )

            errorMessage?.let { message ->
                KaErrorCard(message = message)
            }

            if (motivationalGoals.isEmpty()) {
                DetailInfoText("No active motivational goals.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    motivationalGoals.forEach { goal ->
                        MotivationalGoalCard(
                            goal = goal,
                            onDeactivateMotivationalGoal = onDeactivateMotivationalGoal,
                        )
                    }
                }
            }

            if (hiddenMotivationalGoals.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Text(
                    text = "Hidden goals",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )

                DetailInfoText("Hidden goals stay available here if you want to restore them later.")

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    hiddenMotivationalGoals.forEach { goal ->
                        HiddenMotivationalGoalCard(
                            goal = goal,
                            onRestoreMotivationalGoal = onRestoreMotivationalGoal,
                            onDeleteMotivationalGoal = onDeleteMotivationalGoal,
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            if (!showAddGoalForm) {
                Button(
                    onClick = {
                        showAddGoalForm = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text("Add motivational goal")
                }
            } else {
                KaDetailInnerCard(
                    borderColor = KaPalette.Purple.copy(alpha = 0.35f),
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "Add motivational goal",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )

                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            ExerciseMotivationalGoalType.entries.forEach { goalType ->
                                KaDetailFilterChip(
                                    selected = selectedGoalType == goalType,
                                    onClick = {
                                        selectedGoalType = goalType
                                    },
                                    label = goalType.displayText(),
                                    accentColor = KaPalette.Purple,
                                )
                            }
                        }

                        OutlinedTextField(
                            value = titleText,
                            onValueChange = { titleText = it },
                            label = { Text("Title optional") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        when (selectedGoalType) {
                            ExerciseMotivationalGoalType.WEIGHT_REPS -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    GoalNumberField(
                                        value = targetWeightText,
                                        onValueChange = { targetWeightText = it },
                                        label = "Weight lb",
                                        allowDecimal = true,
                                        modifier = Modifier.weight(1f),
                                    )

                                    GoalNumberField(
                                        value = targetRepsText,
                                        onValueChange = { targetRepsText = it },
                                        label = "Reps",
                                        allowDecimal = false,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }

                            ExerciseMotivationalGoalType.ESTIMATED_1RM,
                            ExerciseMotivationalGoalType.ACTUAL_1RM -> {
                                GoalNumberField(
                                    value = targetOneRepMaxText,
                                    onValueChange = { targetOneRepMaxText = it },
                                    label = "Target 1RM lb",
                                    allowDecimal = true,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }

                            ExerciseMotivationalGoalType.CARDIO_DISTANCE -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    GoalNumberField(
                                        value = targetDistanceText,
                                        onValueChange = { targetDistanceText = it },
                                        label = "Distance",
                                        allowDecimal = true,
                                        modifier = Modifier.weight(1f),
                                    )

                                    OutlinedTextField(
                                        value = targetDistanceUnitText,
                                        onValueChange = { targetDistanceUnitText = it },
                                        label = { Text("Unit") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }

                            ExerciseMotivationalGoalType.CARDIO_DURATION -> {
                                GoalNumberField(
                                    value = targetDurationMinutesText,
                                    onValueChange = { targetDurationMinutesText = it },
                                    label = "Duration minutes",
                                    allowDecimal = false,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }

                            ExerciseMotivationalGoalType.CARDIO_DISTANCE_DURATION -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    GoalNumberField(
                                        value = targetDistanceText,
                                        onValueChange = { targetDistanceText = it },
                                        label = "Distance",
                                        allowDecimal = true,
                                        modifier = Modifier.weight(1f),
                                    )

                                    OutlinedTextField(
                                        value = targetDistanceUnitText,
                                        onValueChange = { targetDistanceUnitText = it },
                                        label = { Text("Unit") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                    )
                                }

                                GoalNumberField(
                                    value = targetDurationMinutesText,
                                    onValueChange = { targetDurationMinutesText = it },
                                    label = "Duration minutes",
                                    allowDecimal = false,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }

                        OutlinedTextField(
                            value = notesText,
                            onValueChange = { notesText = it },
                            label = { Text("Notes optional") },
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedButton(
                                onClick = {
                                    showAddGoalForm = false
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("Cancel")
                            }

                            Button(
                                onClick = {
                                    onCreateMotivationalGoal(
                                        exerciseId,
                                        selectedGoalType,
                                        titleText,
                                        positiveDoubleFromText(targetWeightText),
                                        positiveIntFromText(targetRepsText),
                                        positiveDoubleFromText(targetOneRepMaxText),
                                        positiveDoubleFromText(targetDistanceText),
                                        targetDistanceUnitText.trim().takeIf { it.isNotBlank() },
                                        durationSecondsFromMinutesText(targetDurationMinutesText),
                                        notesText,
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                            ) {
                                Text("Save")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MotivationalGoalCard(
    goal: ExerciseMotivationalGoalEntity,
    onDeactivateMotivationalGoal: (ExerciseMotivationalGoalEntity) -> Unit,
) {
    val displayTitle = buildMotivationalGoalDisplayTitle(goal)
    val summary = buildMotivationalGoalSummary(goal)
        ?.takeUnless { detail ->
            detail.equals(displayTitle, ignoreCase = true)
        }

    KaDetailInnerCard(
        borderColor = KaPalette.Purple.copy(alpha = 0.30f),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = displayTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = goal.goalType.displayText(),
                style = MaterialTheme.typography.bodySmall,
                color = KaPalette.Purple,
                fontWeight = FontWeight.SemiBold,
            )

            if (summary != null) {
                DetailMetricLine(
                    text = summary,
                    accentColor = MaterialTheme.colorScheme.onSurface,
                )
            }

            if (!goal.notes.isNullOrBlank()) {
                DetailInfoText(goal.notes)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(
                    onClick = {
                        onDeactivateMotivationalGoal(goal)
                    },
                ) {
                    Text(
                        text = "Hide goal",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun HiddenMotivationalGoalCard(
    goal: ExerciseMotivationalGoalEntity,
    onRestoreMotivationalGoal: (ExerciseMotivationalGoalEntity) -> Unit,
    onDeleteMotivationalGoal: (ExerciseMotivationalGoalEntity) -> Unit,
) {
    val displayTitle = buildMotivationalGoalDisplayTitle(goal)
    val summary = buildMotivationalGoalSummary(goal)
        ?.takeUnless { detail ->
            detail.equals(displayTitle, ignoreCase = true)
        }

    KaDetailInnerCard(
        borderColor = MaterialTheme.colorScheme.outlineVariant,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = displayTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = "Hidden · ${goal.goalType.displayText()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (summary != null) {
                DetailMetricLine(
                    text = summary,
                    accentColor = MaterialTheme.colorScheme.onSurface,
                )
            }

            if (!goal.notes.isNullOrBlank()) {
                DetailInfoText(goal.notes)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(
                    onClick = {
                        onRestoreMotivationalGoal(goal)
                    },
                ) {
                    Text(
                        text = "Restore",
                        color = KaPalette.Success,
                    )
                }

                TextButton(
                    onClick = {
                        onDeleteMotivationalGoal(goal)
                    },
                ) {
                    Text(
                        text = "Delete",
                        color = KaPalette.Danger,
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    allowDecimal: Boolean,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (allowDecimal) {
                KeyboardType.Decimal
            } else {
                KeyboardType.Number
            },
        ),
        modifier = modifier,
    )
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

    KaDetailSectionCard(
        title = "Pace profiles",
        count = paceProfiles.size,
        accentColor = KaPalette.Amber,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            DetailInfoText(
                text = "Optional gentle timing nudges. Leave disabled or use 0 seconds for no nudge.",
            )

            errorMessage?.let { message ->
                KaErrorCard(message = message)
            }

            if (paceProfiles.isEmpty()) {
                DetailInfoText("No pace profiles yet.")
            } else {
                val hasDefaultProfile = paceProfiles.any { profile ->
                    profile.isDefault
                }

                if (!hasDefaultProfile) {
                    DetailInfoText("This exercise has pace profiles, but no default is set.")
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            if (!showAddPaceProfileForm) {
                Button(
                    onClick = {
                        showAddPaceProfileForm = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text("Add pace profile")
                }
            } else {
                KaDetailInnerCard(
                    borderColor = KaPalette.Amber.copy(alpha = 0.35f),
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
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

                            Button(
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
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                            ) {
                                Text("Save")
                            }
                        }
                    }
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

    KaDetailInnerCard(
        borderColor =
            if (profile.isDefault) {
                KaPalette.Amber.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (profile.isDefault) {
                            Text(
                                text = "Default",
                                style = MaterialTheme.typography.labelSmall,
                                color = KaPalette.Amber,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        Text(
                            text = if (profile.isEnabled) {
                                "Enabled"
                            } else {
                                "Disabled"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (profile.isEnabled) {
                                KaPalette.Success
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

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
                        tint = KaPalette.Amber,
                    )
                }
            }

            DetailInfoText(
                text = if (profile.isEnabled) {
                    buildProfileTimingSummary(profile)
                } else {
                    "Disabled"
                },
            )

            if (expanded) {
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
                    Button(
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
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Text("Save changes")
                    }

                    OutlinedButton(
                        onClick = {
                            onSetPaceProfileAsDefault(profile)
                            isDefault = true
                        },
                        enabled = !profile.isDefault,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = KaPalette.Amber,
                        ),
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
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = KaPalette.Danger,
                        ),
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
            color = MaterialTheme.colorScheme.onSurface,
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = KaPalette.Amber,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            ),
        )
    }
}

@Composable
private fun ExerciseHistoryGraph(
    historyPoints: List<ExerciseHistoryPointUiState>,
    historyTrendText: String?,
) {
    if (historyPoints.isEmpty()) {
        DetailInfoText("No history yet.")
        return
    }

    val maxWeight = historyPoints
        .maxOfOrNull { point -> point.maxWeight }
        ?.takeIf { it > 0.0 }
        ?: 1.0

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        historyTrendText?.let { text ->
            DetailInfoText(text)
        }

        KaDetailInnerCard(
            borderColor = KaPalette.Amber.copy(alpha = 0.25f),
        ) {
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
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height(barHeight)
                                .clip(RoundedCornerShape(6.dp))
                                .background(KaPalette.Amber),
                        )

                        Text(
                            text = point.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
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

        Button(
            onClick = {
                onCreateMuscleGroup(newName)
                newName = ""
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Icon(
                painter = painterResource(R.drawable.plus),
                contentDescription = null,
            )

            Spacer(modifier = Modifier.width(8.dp))

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

private fun buildMotivationalGoalListSummary(
    motivationalGoals: List<ExerciseMotivationalGoalEntity>,
): String? {
    if (motivationalGoals.isEmpty()) {
        return null
    }

    val firstGoal = motivationalGoals.first()
    val firstGoalTitle = buildMotivationalGoalDisplayTitle(firstGoal)

    return if (motivationalGoals.size == 1) {
        "Goal: $firstGoalTitle"
    } else {
        "Goals: $firstGoalTitle + ${motivationalGoals.size - 1} more"
    }
}

private fun buildMotivationalGoalDisplayTitle(
    goal: ExerciseMotivationalGoalEntity,
): String {
    val summary = buildMotivationalGoalSummary(goal)

    return if (summary != null && goal.title.equals(summary, ignoreCase = true)) {
        goal.goalType.defaultTitle()
    } else {
        goal.title
    }
}

private fun buildMotivationalGoalSummary(
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

private fun positiveDoubleFromText(
    value: String,
): Double? {
    return value
        .trim()
        .toDoubleOrNull()
        ?.takeIf { it > 0.0 }
}

private fun positiveIntFromText(
    value: String,
): Int? {
    return value
        .trim()
        .toIntOrNull()
        ?.takeIf { it > 0 }
}

private fun durationSecondsFromMinutesText(
    value: String,
): Int? {
    return value
        .trim()
        .toIntOrNull()
        ?.takeIf { it > 0 }
        ?.times(60)
}

private fun ExerciseMotivationalGoalType.defaultTitle(): String {
    return when (this) {
        ExerciseMotivationalGoalType.WEIGHT_REPS -> "Weight x reps goal"
        ExerciseMotivationalGoalType.ESTIMATED_1RM -> "Estimated 1RM goal"
        ExerciseMotivationalGoalType.ACTUAL_1RM -> "Actual 1RM goal"
        ExerciseMotivationalGoalType.CARDIO_DISTANCE -> "Cardio distance goal"
        ExerciseMotivationalGoalType.CARDIO_DURATION -> "Cardio duration goal"
        ExerciseMotivationalGoalType.CARDIO_DISTANCE_DURATION ->
            "Cardio distance and duration goal"
    }
}

private fun ExerciseMotivationalGoalType.displayText(): String {
    return when (this) {
        ExerciseMotivationalGoalType.WEIGHT_REPS -> "Weight x reps"
        ExerciseMotivationalGoalType.ESTIMATED_1RM -> "Estimated 1RM"
        ExerciseMotivationalGoalType.ACTUAL_1RM -> "Actual 1RM"
        ExerciseMotivationalGoalType.CARDIO_DISTANCE -> "Distance"
        ExerciseMotivationalGoalType.CARDIO_DURATION -> "Duration"
        ExerciseMotivationalGoalType.CARDIO_DISTANCE_DURATION -> "Distance + duration"
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
