package com.example.kusgangaliwas.ui.cycle

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
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            KusgangTopBar(
                title = "Training Cycles",
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
                        SectionHeader("Cycles")

                        if (uiState.cycles.isEmpty()) {
                            Text("No cycles yet.")
                        } else {
                            uiState.cycles.forEach { cycle ->
                                SharpCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSelectCycle(cycle.id)
                                        },
                                ) {
                                    Column(
                                        verticalArrangement =
                                            Arrangement.spacedBy(4.dp),
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

                                        if (!cycle.isActive) {
                                            Text("Inactive")
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                onCreateCycleExpandedChange(
                                    !uiState.isCreateCycleExpanded
                                )
                            },
                        ) {
                            Text("New cycle")
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
                            Text(uiState.selectedCycleName)

                            if (uiState.selectedCycleNotes.isNotBlank()) {
                                Text(uiState.selectedCycleNotes)
                            }

                            OutlinedButton(
                                onClick = onDeleteSelectedCycleClick,
                            ) {
                                Text("Archive cycle")
                            }
                        }
                    }
                }
            }

            item {
                SharpCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader("Cycle Order")

                        if (uiState.selectedCycleId == null) {
                            Text("No cycle selected.")
                        } else if (uiState.steps.isEmpty()) {
                            Text("No splits in this cycle yet.")
                        } else {
                            uiState.steps.forEachIndexed { index, step ->
                                SharpCard {
                                    Column(
                                        verticalArrangement =
                                            Arrangement.spacedBy(6.dp),
                                    ) {
                                        Text(
                                            "${index + 1}. ${step.splitName}"
                                        )

                                        Row(
                                            horizontalArrangement =
                                                Arrangement.spacedBy(8.dp),
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    onMoveStepUpClick(step.id)
                                                },
                                                enabled = index > 0,
                                            ) {
                                                Text("Up")
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    onMoveStepDownClick(step.id)
                                                },
                                                enabled =
                                                    index < uiState.steps.lastIndex,
                                            ) {
                                                Text("Down")
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    onRemoveStepClick(step.id)
                                                },
                                            ) {
                                                Text("Remove")
                                            }
                                        }

                                        Row(
                                            horizontalArrangement =
                                                Arrangement.spacedBy(8.dp),
                                        ) {
                                            Checkbox(
                                                checked =
                                                    step.warnBeforeMarkDone,
                                                onCheckedChange = {
                                                    onToggleWarnBeforeMarkDoneClick(
                                                        step.id
                                                    )
                                                },
                                            )

                                            Text(
                                                "Warn before mark done"
                                            )
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
                        SectionHeader("Add Splits")

                        if (uiState.selectedCycleId == null) {
                            Text("Select or create a cycle first.")
                        } else if (uiState.availableSplits.isEmpty()) {
                            Text("No saved splits available.")
                        } else {
                            uiState.availableSplits.forEach { split ->
                                OutlinedButton(
                                    onClick = {
                                        onAddSplitClick(split.splitTemplateId)
                                    },
                                    enabled =
                                        !split.alreadyInSelectedCycle,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(
                                        if (split.alreadyInSelectedCycle) {
                                            "${split.splitName} already added"
                                        } else {
                                            "Add ${split.splitName}"
                                        }
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