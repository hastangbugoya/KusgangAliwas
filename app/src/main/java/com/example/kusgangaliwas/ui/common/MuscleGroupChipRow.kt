package com.example.kusgangaliwas.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kusgangaliwas.data.local.entity.MuscleGroupEntity
import com.example.kusgangaliwas.ui.theme.KaPalette

@Composable
fun MuscleGroupChipCard(
    muscleGroups: List<MuscleGroupEntity>,
    selectedMuscleGroupIds: Set<Long>,
    onToggleMuscleGroup: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Muscle groups",
    emptyText: String = "No muscle groups yet.",
    accentColor: Color = KaPalette.SteelBlue,
    allLabel: String? = null,
    onClearSelection: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = accentColor.copy(alpha = 0.18f),
        ),
        shape = RoundedCornerShape(10.dp),
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
                        .height(34.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(accentColor.copy(alpha = 0.9f)),
                )

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )

                if (muscleGroups.isNotEmpty()) {
                    Text(
                        text = selectedMuscleGroupIds.size.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (muscleGroups.isEmpty()) {
                Text(
                    text = emptyText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                MuscleGroupChipRow(
                    muscleGroups = muscleGroups,
                    selectedMuscleGroupIds = selectedMuscleGroupIds,
                    onToggleMuscleGroup = onToggleMuscleGroup,
                    allLabel = allLabel,
                    onClearSelection = onClearSelection,
                    accentColor = accentColor,
                )
            }
        }
    }
}

@Composable
fun MuscleGroupChipRow(
    muscleGroups: List<MuscleGroupEntity>,
    selectedMuscleGroupIds: Set<Long>,
    onToggleMuscleGroup: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    allLabel: String? = null,
    onClearSelection: (() -> Unit)? = null,
    accentColor: Color = KaPalette.SteelBlue,
) {
    if (muscleGroups.isEmpty()) {
        return
    }

    val sortedMuscleGroups = muscleGroups.sortedWith(
        compareByDescending<MuscleGroupEntity> { muscleGroup ->
            selectedMuscleGroupIds.contains(muscleGroup.id)
        }.thenBy { muscleGroup ->
            muscleGroup.name.lowercase()
        },
    )

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (allLabel != null && onClearSelection != null) {
            val selected = selectedMuscleGroupIds.isEmpty()

            StandardMuscleGroupChip(
                selected = selected,
                onClick = onClearSelection,
                label = allLabel,
                accentColor = accentColor,
            )
        }

        sortedMuscleGroups.forEach { muscleGroup ->
            val selected = selectedMuscleGroupIds.contains(muscleGroup.id)

            StandardMuscleGroupChip(
                selected = selected,
                onClick = {
                    onToggleMuscleGroup(
                        muscleGroup.id,
                        selected,
                    )
                },
                label = muscleGroup.name,
                accentColor = accentColor,
            )
        }
    }
}

@Composable
private fun StandardMuscleGroupChip(
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