package com.example.kusgangaliwas.ui.common

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kusgangaliwas.data.local.entity.MuscleGroupEntity

@Composable
fun MuscleGroupChipRow(
    muscleGroups: List<MuscleGroupEntity>,
    selectedMuscleGroupIds: Set<Long>,
    onToggleMuscleGroup: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    allLabel: String? = null,
    onClearSelection: (() -> Unit)? = null,
) {
    if (muscleGroups.isEmpty()) {
        return
    }

    val sortedMuscleGroups = muscleGroups.sortedWith(
        compareByDescending<MuscleGroupEntity> { muscleGroup ->
            selectedMuscleGroupIds.contains(muscleGroup.id)
        }.thenBy { muscleGroup ->
            muscleGroup.name.lowercase()
        }
    )

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (allLabel != null && onClearSelection != null) {
            FilterChip(
                selected = selectedMuscleGroupIds.isEmpty(),
                onClick = onClearSelection,
                label = {
                    Text(allLabel)
                },
            )
        }

        sortedMuscleGroups.forEach { muscleGroup ->
            val selected = selectedMuscleGroupIds.contains(muscleGroup.id)

            FilterChip(
                selected = selected,
                onClick = {
                    onToggleMuscleGroup(
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