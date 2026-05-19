package com.example.kusgangaliwas.ui.cycle

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.kusgangaliwas.R
import com.example.kusgangaliwas.ui.common.SectionHeader
import com.example.kusgangaliwas.ui.common.SharpCard
import com.example.kusgangaliwas.ui.common.selection.SplitPickerScreen

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
        topBar = {
            TopAppBar(
                title = {
                    Text("Training Cycle")
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
                }
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
                        SectionHeader("Cycles")

                        if (uiState.cycles.isEmpty()) {
                            Text("No cycles yet.")
                        } else {
                            uiState.cycles.forEach { cycle ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSelectCycle(cycle.id)
                                        },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Text(
                                            if (cycle.id == uiState.selectedCycleId) {
                                                "Selected: ${cycle.name}"
                                            } else {
                                                cycle.name
                                            }
                                        )

                                        if (!cycle.notes.isNullOrBlank()) {
                                            Text(cycle.notes)
                                        }

                                        cycle.startedDateText?.let { text ->
                                            Text(
                                                text = text,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.secondary,
                                            )
                                        }

                                        cycle.lastLoggedSessionDateText?.let { text ->
                                            Text(
                                                text = text,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.secondary,
                                            )
                                        }
                                    }

                                    Switch(
                                        checked = cycle.isActive,
                                        onCheckedChange = { checked ->
                                            onSetCycleActive(cycle.id, checked)
                                        },
                                    )
                                }
                            }
                        }

                        if (uiState.isCreateCycleExpanded) {
                            OutlinedTextField(
                                value = uiState.newCycleName,
                                onValueChange = onNewCycleNameChange,
                                label = {
                                    Text("Cycle name")
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )

                            OutlinedTextField(
                                value = uiState.newCycleNotes,
                                onValueChange = onNewCycleNotesChange,
                                label = {
                                    Text("Notes")
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )

                            OutlinedButton(
                                onClick = onCreateCycleClick,
                            ) {
                                Text("Create")
                            }
                        }
                    }
                }
            }

            item {
                SharpCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader("Selected Cycle")

                        if (uiState.selectedCycleId == null) {
                            Text("Select or create a cycle.")
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(uiState.selectedCycleName)

                                    if (uiState.selectedCycleNotes.isNotBlank()) {
                                        Text(uiState.selectedCycleNotes)
                                    }
                                }

                                IconButton(
                                    onClick = onDeleteSelectedCycleClick,
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.folder_xmark),
                                        contentDescription = "Archive cycle",
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                SharpCard {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            SectionHeader("Cycle Order")

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
                                )
                            }
                        }

                        if (uiState.selectedCycleId == null) {
                            Text("No cycle selected.")
                        } else if (uiState.steps.isEmpty()) {
                            Text("No splits in this cycle yet.")
                        } else {
                            uiState.steps.forEachIndexed { index, step ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp),
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            Text("${index + 1}. ${step.splitName}")

                                            IconButton(
                                                onClick = {
                                                    onRemoveStepClick(step.id)
                                                },
                                                modifier = Modifier.size(28.dp),
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.trash),
                                                    contentDescription = "Remove split",
                                                    modifier = Modifier.size(18.dp),
                                                )
                                            }
                                        }

                                        if (step.muscleGroupsText.isNotBlank()) {
                                            Text(
                                                text = step.muscleGroupsText.replace(",", ", "),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.secondary,
                                            )
                                        }

                                        Text(
                                            text = "${step.strengthExerciseCount} strength • " +
                                                    "${step.cardioExerciseCount} cardio • " +
                                                    "${step.totalExerciseCount} total",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary,
                                        )

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Checkbox(
                                                checked = step.warnBeforeMarkDone,
                                                onCheckedChange = {
                                                    onToggleWarnBeforeMarkDoneClick(step.id)
                                                },
                                            )

                                            Text(
                                                "Warn before mark done",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.secondary,
                                            )
                                        }
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(-6.dp),
                                    ) {
                                        IconButton(
                                            onClick = {
                                                onMoveStepUpClick(step.id)
                                            },
                                            enabled = index > 0,
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.angle_up),
                                                contentDescription = "Move split up",
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                onMoveStepDownClick(step.id)
                                            },
                                            enabled = index < uiState.steps.lastIndex,
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
                    }
                }
            }
        }
    }

}