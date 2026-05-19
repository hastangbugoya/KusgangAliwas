package com.example.kusgangaliwas.ui.common.selection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kusgangaliwas.ui.common.KusgangTopBar
import com.example.kusgangaliwas.ui.common.SectionHeader
import com.example.kusgangaliwas.ui.common.SharpCard
import com.example.kusgangaliwas.ui.cycle.TrainingCycleSplitOption

@Composable
fun SplitPickerScreen(
    title: String,
    splits: List<TrainingCycleSplitOption>,
    selectedSplitIds: Set<Long>,
    onToggleSplit: (Long) -> Unit,
    onBackClick: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            KusgangTopBar(
                title = title,
                onBackClick = onBackClick,
                onOverflowClick = {},
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
            OutlinedButton(
                onClick = onConfirmClick,
                enabled = selectedSplitIds.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Add selected (${selectedSplitIds.size})")
            }

            SharpCard(
                modifier = Modifier.weight(1f),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader("Splits")

                    if (splits.isEmpty()) {
                        Text("No saved splits available.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(0.dp),
                        ) {
                            items(splits) { split ->
                                SplitPickerRow(
                                    split = split,
                                    checked =
                                        split.alreadyInSelectedCycle ||
                                                split.splitTemplateId in selectedSplitIds,
                                    onToggleSplit = onToggleSplit,
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
private fun SplitPickerRow(
    split: TrainingCycleSplitOption,
    checked: Boolean,
    onToggleSplit: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !split.alreadyInSelectedCycle) {
                onToggleSplit(split.splitTemplateId)
            }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            enabled = !split.alreadyInSelectedCycle,
            onCheckedChange = {
                onToggleSplit(split.splitTemplateId)
            },
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(split.splitName)

            if (split.muscleGroupsText.isNotBlank()) {
                Text(
                    text = split.muscleGroupsText.replace(",", ", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }

            Text(
                text = "${split.strengthExerciseCount} strength • " +
                        "${split.cardioExerciseCount} cardio • " +
                        "${split.totalExerciseCount} total",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )

            if (split.alreadyInSelectedCycle) {
                Text(
                    text = "Already in cycle",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}