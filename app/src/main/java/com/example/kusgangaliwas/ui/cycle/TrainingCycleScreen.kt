package com.example.kusgangaliwas.ui.cycle

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kusgangaliwas.R
import com.example.kusgangaliwas.ui.common.selection.SplitPickerScreen
import com.example.kusgangaliwas.ui.theme.KaPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingCycleScreen(
    uiState: TrainingCycleUiState,
    onBackClick: () -> Unit,
    onOverflowClick: () -> Unit,
    onSelectCycle: (Long) -> Unit,
    onCreateCycleExpandedChange: (Boolean) -> Unit,
    onNewCycleNameChange: (String) -> Unit,
    onNewCycleNotesChange: (String) -> Unit,
    onCreateCycleClick: () -> Unit,
    onAddSplitClick: (Long) -> Unit,
    onRemoveStepClick: (Long) -> Unit,
    onMoveStepUpClick: (Long) -> Unit,
    onMoveStepDownClick: (Long) -> Unit,
    onToggleWarnBeforeMarkDoneClick: (Long) -> Unit,
    onDeleteSelectedCycleClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSetCycleActive: (Long, Boolean) -> Unit,
) {
    var showSplitPicker by rememberSaveable {
        mutableStateOf(false)
    }

    var selectedSplitIdsToAdd by rememberSaveable {
        mutableStateOf(setOf<Long>())
    }

    if (showSplitPicker) {
        SplitPickerScreen(
            title = "Add splits",
            splits = uiState.availableSplits,
            selectedSplitIds = selectedSplitIdsToAdd,
            onToggleSplit = { splitId ->
                selectedSplitIdsToAdd =
                    if (splitId in selectedSplitIdsToAdd) {
                        selectedSplitIdsToAdd - splitId
                    } else {
                        selectedSplitIdsToAdd + splitId
                    }
            },
            onBackClick = {
                selectedSplitIdsToAdd = emptySet()
                showSplitPicker = false
            },
            onConfirmClick = {
                selectedSplitIdsToAdd.forEach { splitTemplateId ->
                    onAddSplitClick(splitTemplateId)
                }

                selectedSplitIdsToAdd = emptySet()
                showSplitPicker = false
            },
        )
        return
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cycles",
                        fontWeight = FontWeight.Bold,
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            onCreateCycleExpandedChange(
                                !uiState.isCreateCycleExpanded
                            )
                        },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.plus),
                            contentDescription = "New cycle",
                            tint = KaPalette.Purple,
                        )
                    }

                    IconButton(
                        onClick = onOverflowClick,
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                KaRootSectionCard(
                    title = "Cycles",
                    count = uiState.cycles.size,
                    accentColor = KaPalette.Purple,
                ) {
                    if (uiState.cycles.isEmpty()) {
                        EmptyText("No cycles yet.")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.cycles.forEach { cycle ->
                                CycleRowCard(
                                    name = cycle.name,
                                    notes = cycle.notes,
                                    startedDateText = cycle.startedDateText,
                                    lastLoggedSessionDateText = cycle.lastLoggedSessionDateText,
                                    selected = cycle.id == uiState.selectedCycleId,
                                    active = cycle.isActive,
                                    onClick = {
                                        onSelectCycle(cycle.id)
                                    },
                                    onActiveChange = { checked ->
                                        onSetCycleActive(cycle.id, checked)
                                    },
                                )
                            }
                        }
                    }

                    if (uiState.isCreateCycleExpanded) {
                        NewCycleCard(
                            name = uiState.newCycleName,
                            notes = uiState.newCycleNotes,
                            onNameChange = onNewCycleNameChange,
                            onNotesChange = onNewCycleNotesChange,
                            onCreateClick = onCreateCycleClick,
                        )
                    }
                }
            }

            item {
                KaRootSectionCard(
                    title = "Selected cycle",
                    accentColor = KaPalette.SteelBlue,
                ) {
                    if (uiState.selectedCycleId == null) {
                        EmptyText("Select or create a cycle.")
                    } else {
                        SelectedCycleCard(
                            name = uiState.selectedCycleName,
                            notes = uiState.selectedCycleNotes,
                            onDeleteClick = onDeleteSelectedCycleClick,
                        )
                    }
                }
            }

            item {
                KaRootSectionCard(
                    title = "Cycle order",
                    count = uiState.steps.size.takeIf { uiState.selectedCycleId != null },
                    accentColor = KaPalette.Amber,
                    trailing = {
                        IconButton(
                            onClick = {
                                selectedSplitIdsToAdd = emptySet()
                                showSplitPicker = true
                            },
                            enabled = uiState.selectedCycleId != null,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.plus),
                                contentDescription = "Add split to cycle",
                                tint =
                                    if (uiState.selectedCycleId != null) {
                                        KaPalette.Amber
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                            )
                        }
                    },
                ) {
                    if (uiState.selectedCycleId == null) {
                        EmptyText("No cycle selected.")
                    } else if (uiState.steps.isEmpty()) {
                        EmptyText("No splits in this cycle yet.")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.steps.forEachIndexed { index, step ->
                                CycleStepCard(
                                    index = index,
                                    isFirst = index == 0,
                                    isLast = index == uiState.steps.lastIndex,
                                    splitName = step.splitName,
                                    muscleGroupsText = step.muscleGroupsText,
                                    strengthExerciseCount = step.strengthExerciseCount,
                                    cardioExerciseCount = step.cardioExerciseCount,
                                    totalExerciseCount = step.totalExerciseCount,
                                    warnBeforeMarkDone = step.warnBeforeMarkDone,
                                    onRemoveClick = {
                                        onRemoveStepClick(step.id)
                                    },
                                    onMoveUpClick = {
                                        onMoveStepUpClick(step.id)
                                    },
                                    onMoveDownClick = {
                                        onMoveStepDownClick(step.id)
                                    },
                                    onToggleWarnClick = {
                                        onToggleWarnBeforeMarkDoneClick(step.id)
                                    },
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
private fun KaRootSectionCard(
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

                trailing?.invoke()
            }

            content()
        }
    }
}

@Composable
private fun CycleRowCard(
    name: String,
    notes: String?,
    startedDateText: String?,
    lastLoggedSessionDateText: String?,
    selected: Boolean,
    active: Boolean,
    onClick: () -> Unit,
    onActiveChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor =
        if (selected) {
            KaPalette.Purple.copy(alpha = 0.65f)
        } else {
            MaterialTheme.colorScheme.outlineVariant
        }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = borderColor,
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (selected) {
                        Text(
                            text = "Selected",
                            style = MaterialTheme.typography.labelSmall,
                            color = KaPalette.Purple,
                        )
                    }

                    if (active) {
                        Text(
                            text = "Active",
                            style = MaterialTheme.typography.labelSmall,
                            color = KaPalette.Success,
                        )
                    }
                }

                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                if (!notes.isNullOrBlank()) {
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                startedDateText?.let { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                lastLoggedSessionDateText?.let { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Switch(
                checked = active,
                onCheckedChange = onActiveChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = KaPalette.Purple,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                ),
            )
        }
    }
}

@Composable
private fun NewCycleCard(
    name: String,
    notes: String,
    onNameChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    KaInnerCard(
        modifier = modifier,
        borderColor = KaPalette.Purple.copy(alpha = 0.45f),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "New cycle",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = {
                    Text("Cycle name")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = {
                    Text("Notes")
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = onCreateClick,
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

                Text("Create cycle")
            }
        }
    }
}

@Composable
private fun SelectedCycleCard(
    name: String,
    notes: String,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    KaInnerCard(
        modifier = modifier,
        borderColor = MaterialTheme.colorScheme.outlineVariant,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                if (notes.isNotBlank()) {
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            IconButton(
                onClick = onDeleteClick,
            ) {
                Icon(
                    painter = painterResource(R.drawable.folder_xmark),
                    contentDescription = "Archive cycle",
                    tint = KaPalette.Danger,
                )
            }
        }
    }
}

@Composable
private fun CycleStepCard(
    index: Int,
    isFirst: Boolean,
    isLast: Boolean,
    splitName: String,
    muscleGroupsText: String,
    strengthExerciseCount: Int,
    cardioExerciseCount: Int,
    totalExerciseCount: Int,
    warnBeforeMarkDone: Boolean,
    onRemoveClick: () -> Unit,
    onMoveUpClick: () -> Unit,
    onMoveDownClick: () -> Unit,
    onToggleWarnClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    KaInnerCard(
        modifier = modifier,
        borderColor = MaterialTheme.colorScheme.outlineVariant,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = "${index + 1}",
                style = MaterialTheme.typography.labelLarge,
                color = KaPalette.Amber,
                modifier = Modifier.padding(top = 2.dp),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = splitName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )

                    IconButton(
                        onClick = onRemoveClick,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.trash),
                            contentDescription = "Remove split",
                            tint = KaPalette.Danger,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                if (muscleGroupsText.isNotBlank()) {
                    Text(
                        text = muscleGroupsText.replace(",", ", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Text(
                    text = "$strengthExerciseCount strength • " +
                            "$cardioExerciseCount cardio • " +
                            "$totalExerciseCount total",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = warnBeforeMarkDone,
                        onCheckedChange = {
                            onToggleWarnClick()
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = KaPalette.Amber,
                            uncheckedColor = MaterialTheme.colorScheme.outline,
                        ),
                    )

                    Text(
                        text = "Warn before mark done",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(-8.dp),
            ) {
                IconButton(
                    onClick = onMoveUpClick,
                    enabled = !isFirst,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.angle_up),
                        contentDescription = "Move split up",
                    )
                }

                IconButton(
                    onClick = onMoveDownClick,
                    enabled = !isLast,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.angle_down),
                        contentDescription = "Move split down",
                    )
                }
            }
        }
    }
}

@Composable
private fun KaInnerCard(
    modifier: Modifier = Modifier,
    borderColor: Color,
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
