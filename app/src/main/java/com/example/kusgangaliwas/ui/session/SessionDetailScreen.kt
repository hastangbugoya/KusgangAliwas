package com.example.kusgangaliwas.ui.session

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import com.example.kusgangaliwas.ui.theme.KaPalette
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.kusgangaliwas.R
import com.example.kusgangaliwas.data.local.entity.ActualCardioLogEntity
import com.example.kusgangaliwas.data.local.entity.ActualExerciseSetLogEntity
import com.example.kusgangaliwas.data.local.entity.ExerciseType
import com.example.kusgangaliwas.ui.common.KusgangTopBar
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem

@Composable
fun SessionDetailScreen(
    uiState: SessionDetailUiState,
    onBackClick: () -> Unit,
    onOverflowClick: () -> Unit,
    onUpdateCardioLog: (ActualCardioLogEntity) -> Unit,
    onDeleteCardioLog: (Long) -> Unit,
    onAddSet: (Long) -> Unit,
    onUpdateSet: (ActualExerciseSetLogEntity, Double?, Int?) -> Unit,
    onDeleteSet: (Long) -> Unit,
    onDuplicateSet: (ActualExerciseSetLogEntity) -> Unit,
    modifier: Modifier = Modifier,
    onRatingChange: (Int?) -> Unit,
    onDeleteExerciseLogIfEmpty: (Long) -> Unit,
    onDeleteSession: () -> Unit,
    onMoveSessionItemUp: (SessionDetailItemUiState) -> Unit,
    onMoveSessionItemDown: (SessionDetailItemUiState) -> Unit,
    onToggleRemoteFocus: (Long) -> Unit,
    onUpdateSavedSplit: () -> Unit,
    onCreateSavedSplit: (String, String?) -> Unit,
    onOpenExercisePicker: () -> Unit,
) {
    var reorderMode by rememberSaveable {
        mutableStateOf(false)
    }

    var showSessionActions by rememberSaveable {
        mutableStateOf(false)
    }

    var showSaveAsSplitDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var newSplitName by rememberSaveable {
        mutableStateOf(uiState.session?.title ?: "")
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Box {
                KusgangTopBar(
                    title = uiState.titleText,
                    onBackClick = onBackClick,
                    onOverflowClick = {
                        showSessionActions = true
                    },
                )

                DropdownMenu(
                    expanded = showSessionActions,
                    onDismissRequest = {
                        showSessionActions = false
                    },
                ) {
                    DropdownMenuItem(
                        text = {
                            Text("Update saved split")
                        },
                        enabled = uiState.session?.splitTemplateId != null,
                        onClick = {
                            showSessionActions = false
                            onUpdateSavedSplit()
                        },
                    )

                    DropdownMenuItem(
                        text = {
                            Text("Save as new split")
                        },
                        onClick = {
                            showSessionActions = false
                            newSplitName = uiState.session?.title ?: ""
                            showSaveAsSplitDialog = true
                        },
                    )
                }
            }
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
                KaSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                KaSectionHeader("Session timeline")
                            }

                            IconButton(
                                onClick = onOpenExercisePicker,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.plus),
                                    contentDescription = "Add session item",
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }

                            TextButton(
                                onClick = { reorderMode = !reorderMode },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor =
                                        if (reorderMode) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.secondary
                                        },
                                ),
                            ) {
                                Text(if (reorderMode) "Done" else "Reorder")
                            }
                        }

                        if (uiState.sessionItems.isEmpty()) {
                            Text("No session items logged yet.")
                        } else {
                            uiState.sessionItems.forEachIndexed { index, item ->
                                when (item) {
                                    is SessionDetailItemUiState.Strength -> {
                                        StrengthTimelineCard(
                                            index = index,
                                            item = item.item,
                                            sessionItem = item,
                                            reorderMode = reorderMode,
                                            onMoveSessionItemUp = onMoveSessionItemUp,
                                            onMoveSessionItemDown = onMoveSessionItemDown,
                                            onAddSet = onAddSet,
                                            onUpdateSet = onUpdateSet,
                                            onDeleteSet = onDeleteSet,
                                            onDuplicateSet = onDuplicateSet,
                                            onDeleteExerciseLogIfEmpty =
                                                onDeleteExerciseLogIfEmpty,
                                            focusedExerciseLogId = uiState.focusedExerciseLogId,
                                            onToggleRemoteFocus = onToggleRemoteFocus,
                                        )
                                    }

                                    is SessionDetailItemUiState.Cardio -> {
                                        CardioTimelineCard(
                                            index = index,
                                            item = item.item,
                                            sessionItem = item,
                                            reorderMode = reorderMode,
                                            onMoveSessionItemUp = onMoveSessionItemUp,
                                            onMoveSessionItemDown = onMoveSessionItemDown,
                                            onUpdateCardioLog = onUpdateCardioLog,
                                            onDeleteCardioLog = onDeleteCardioLog,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                KaSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        KaSectionHeader("Session rating")

                        StarRatingRow(
                            rating = uiState.session?.rating,
                            onRatingChange = onRatingChange,
                        )
                    }
                }
            }

            item {
                KaDangerCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        KaSectionHeader("Danger zone")

                        Text(
                            text = "Delete this workout session permanently.",
                            style = MaterialTheme.typography.bodySmall,
                        )

                        Button(
                            onClick = onDeleteSession,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = KaPalette.Danger,
                                contentColor = MaterialTheme.colorScheme.onError,
                            ),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.trash),
                                contentDescription = null,
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text("Delete session")
                        }
                    }
                }
            }
        }
    }

    if (showSaveAsSplitDialog) {
        AlertDialog(
            onDismissRequest = {
                showSaveAsSplitDialog = false
            },
            title = {
                Text("Save as new split")
            },
            text = {
                OutlinedTextField(
                    value = newSplitName,
                    onValueChange = {
                        newSplitName = it
                    },
                    label = {
                        Text("Split name")
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmedName = newSplitName.trim()

                        if (trimmedName.isNotBlank()) {
                            onCreateSavedSplit(trimmedName, null)
                            showSaveAsSplitDialog = false
                        }
                    },
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSaveAsSplitDialog = false
                    },
                ) {
                    Text("Cancel")
                }
            },
        )
    }
}


@Composable
private fun KaSectionCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun KaTimelineItemCard(
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
    isRemoteFocused: Boolean = false,
    content: @Composable () -> Unit,
) {
    val borderColor =
        when {
            isRemoteFocused -> MaterialTheme.colorScheme.primary
            isExpanded -> MaterialTheme.colorScheme.secondary
            else -> MaterialTheme.colorScheme.outlineVariant
        }

    val containerColor =
        if (isExpanded) {
            MaterialTheme.colorScheme.surfaceContainerHigh
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun KaDangerCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, KaPalette.Danger),
        colors = CardDefaults.cardColors(
            containerColor = KaPalette.DangerContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun KaSectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.16f)
                .height(3.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(percent = 100),
                ),
        )
    }
}

@Composable
private fun StrengthTimelineCard(
    index: Int,
    item: SessionExerciseLogUiState,
    onAddSet: (Long) -> Unit,
    onUpdateSet: (ActualExerciseSetLogEntity, Double?, Int?) -> Unit,
    onDeleteSet: (Long) -> Unit,
    onDuplicateSet: (ActualExerciseSetLogEntity) -> Unit,
    onDeleteExerciseLogIfEmpty: (Long) -> Unit,
    sessionItem: SessionDetailItemUiState,
    reorderMode: Boolean,
    onMoveSessionItemUp: (SessionDetailItemUiState) -> Unit,
    onMoveSessionItemDown: (SessionDetailItemUiState) -> Unit,
    focusedExerciseLogId: Long?,
    onToggleRemoteFocus: (Long) -> Unit,
) {
    var expanded by rememberSaveable(item.log.id) {
        mutableStateOf(false)
    }

    KaTimelineItemCard(
        isExpanded = expanded,
        isRemoteFocused = focusedExerciseLogId == item.log.id,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "${index + 1}. 🏋 ${item.exerciseName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )

                    Text(
                        text = buildSetSummary(item.sets),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                CompactSessionItemControls(
                    sessionItem = sessionItem,
                    reorderMode = reorderMode,
                    onMoveSessionItemUp = onMoveSessionItemUp,
                    onMoveSessionItemDown = onMoveSessionItemDown,
                    isRemoteFocused = focusedExerciseLogId == item.log.id,
                    onToggleRemoteFocus = { onToggleRemoteFocus(item.log.id) },
                    expanded = expanded,
                    onToggleExpanded = { expanded = !expanded },
                )
            }

            item.previousMaxText?.let { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (!reorderMode && expanded) {
                if (item.sets.isEmpty()) {
                    Text(
                        "No sets yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    item.sets.forEach { set ->
                        SetEditorRow(
                            set = set,
                            onUpdateSet = onUpdateSet,
                            onDeleteSet = onDeleteSet,
                            onDuplicateSet = onDuplicateSet,
                        )
                    }
                }

//                OutlinedButton(
//                    onClick = { onAddSet(item.log.id) },
//                ) {
//                    Text("Add set")
//                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Button(
                        onClick = { onAddSet(item.log.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.add_document),
                            contentDescription = null,
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text("Add set")
                    }
                }

                if (item.sets.isEmpty()) {
                    TextButton(
                        onClick = {
                            onDeleteExerciseLogIfEmpty(item.log.id)
                        },
                    ) {
                        Text("Remove exercise")
                    }
                }
            }
        }
    }
}

@Composable
private fun CardioTimelineCard(
    index: Int,
    item: SessionCardioLogUiState,
    onUpdateCardioLog: (ActualCardioLogEntity) -> Unit,
    onDeleteCardioLog: (Long) -> Unit,
    sessionItem: SessionDetailItemUiState,
    reorderMode: Boolean,
    onMoveSessionItemUp: (SessionDetailItemUiState) -> Unit,
    onMoveSessionItemDown: (SessionDetailItemUiState) -> Unit,
) {
    var expanded by rememberSaveable(item.log.id) {
        mutableStateOf(false)
    }

    var distanceText by rememberSaveable(item.log.id) {
        mutableStateOf(item.log.distance?.let(::formatDistance) ?: "")
    }
    var durationMinutesText by rememberSaveable(item.log.id) {
        mutableStateOf(item.log.durationSeconds?.let { (it / 60).toString() } ?: "")
    }
    var notesText by rememberSaveable(item.log.id) {
        mutableStateOf(item.log.notes ?: "")
    }

    LaunchedEffect(item.log.id, item.log.distance) {
        val next = item.log.distance?.let(::formatDistance) ?: ""
        if (distanceText != next) {
            distanceText = next
        }
    }

    LaunchedEffect(item.log.id, item.log.durationSeconds) {
        val next = item.log.durationSeconds?.let { (it / 60).toString() } ?: ""
        if (durationMinutesText != next) {
            durationMinutesText = next
        }
    }

    LaunchedEffect(item.log.id, item.log.notes) {
        val next = item.log.notes ?: ""
        if (notesText != next) {
            notesText = next
        }
    }

    KaTimelineItemCard(
        isExpanded = expanded,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "${index + 1}. 🏃 ${item.cardioName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )

                    val details = buildCardioDetails(item.log)

                    if (details.isNotEmpty()) {
                        Text(
                            text = details.joinToString(" • "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Text(
                            text = "No cardio details yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                CompactSessionItemControls(
                    sessionItem = sessionItem,
                    reorderMode = reorderMode,
                    onMoveSessionItemUp = onMoveSessionItemUp,
                    onMoveSessionItemDown = onMoveSessionItemDown,
                    isRemoteFocused = false,
                    onToggleRemoteFocus = null,
                    expanded = expanded,
                    onToggleExpanded = { expanded = !expanded },
                )
            }

            item.previousCardioText?.let { text ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )

                    if (
                        item.previousDistance != null ||
                        item.previousDurationSeconds != null ||
                        item.previousIncline != null ||
                        item.previousResistance != null
                    ) {
                        TextButton(
                            onClick = {
                                onUpdateCardioLog(
                                    item.log.copy(
                                        distance = item.previousDistance,
                                        distanceUnit = item.previousDistanceUnit
                                            ?: item.log.distanceUnit
                                            ?: "mi",
                                        durationSeconds = item.previousDurationSeconds,
                                        averageInclinePercent = item.previousIncline,
                                        averageResistance = item.previousResistance,
                                        updatedAtEpochMillis = System.currentTimeMillis(),
                                    )
                                )
                            },
                        ) {
                            Text("Use")
                        }
                    }
                }
            }

            if (!reorderMode && expanded) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = distanceText,
                        onValueChange = { value ->
                            distanceText = value
                            onUpdateCardioLog(
                                item.log.copy(
                                    distance = value.toDoubleOrNull(),
                                    distanceUnit = item.log.distanceUnit ?: "mi",
                                    updatedAtEpochMillis = System.currentTimeMillis(),
                                )
                            )
                        },
                        label = { Text("Distance") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                        ),
                        modifier = Modifier.weight(1f),
                    )

                    Text(item.log.distanceUnit ?: "mi")
                }

                OutlinedTextField(
                    value = durationMinutesText,
                    onValueChange = { value ->
                        durationMinutesText = value
                        onUpdateCardioLog(
                            item.log.copy(
                                durationSeconds = value.toLongOrNull()?.times(60),
                                updatedAtEpochMillis = System.currentTimeMillis(),
                            )
                        )
                    },
                    label = { Text("Duration minutes") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                    ),
                )

                IntensitySelectorRow(
                    intensityLevel = item.log.intensityLevel,
                    onIntensitySelected = { level ->

                        val durationSeconds = item.log.durationSeconds

                        val estimatedDistance =
                            if (
                                item.log.distance == null &&
                                durationSeconds != null
                            ) {
                                estimateDistanceMiles(
                                    durationSeconds = durationSeconds,
                                    intensityLevel = level,
                                )
                            } else {
                                item.log.distance
                            }

                        onUpdateCardioLog(
                            item.log.copy(
                                intensityLevel = level,
                                distance = estimatedDistance,
                                isEstimatedDistance =
                                    estimatedDistance != null &&
                                            item.log.distance == null,
                                updatedAtEpochMillis = System.currentTimeMillis(),
                            )
                        )
                    },
                )

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { value ->
                        notesText = value
                        onUpdateCardioLog(
                            item.log.copy(
                                notes = value.ifBlank { null },
                                updatedAtEpochMillis = System.currentTimeMillis(),
                            )
                        )
                    },
                    label = { Text("Notes") },
                )

                TextButton(
                    onClick = { onDeleteCardioLog(item.log.id) },
                ) {
                    Text("Delete cardio")
                }
            }
        }
    }
}

@Composable
private fun CompactSessionItemControls(
    sessionItem: SessionDetailItemUiState,
    reorderMode: Boolean,
    onMoveSessionItemUp: (SessionDetailItemUiState) -> Unit,
    onMoveSessionItemDown: (SessionDetailItemUiState) -> Unit,
    isRemoteFocused: Boolean,
    onToggleRemoteFocus: (() -> Unit)?,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (reorderMode) {
            ControlIconButton(
                drawableResId = R.drawable.angle_up,
                contentDescription = "Move item up",
                onClick = { onMoveSessionItemUp(sessionItem) },
            )

            ControlIconButton(
                drawableResId = R.drawable.angle_down,
                contentDescription = "Move item down",
                onClick = { onMoveSessionItemDown(sessionItem) },
            )
        } else {
            onToggleRemoteFocus?.let { toggleRemoteFocus ->
                IconButton(
                    onClick = toggleRemoteFocus,
                ) {
                    Icon(
                        painter = painterResource(
                            id =
                                if (isRemoteFocused) {
                                    R.drawable.remote_control
                                } else {
                                    R.drawable.signal_stream_slash
                                }
                        ),
                        contentDescription =
                            if (isRemoteFocused) {
                                "Remote focused"
                            } else {
                                "Focus for remote"
                            },
                        tint =
                            if (isRemoteFocused) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    )
                }
            }
            ControlIconButton(
                drawableResId =
                    if (expanded) {
                        R.drawable.chevron_double_up
                    } else {
                        R.drawable.chevron_double_down
                    },
                contentDescription =
                    if (expanded) {
                        "Collapse item"
                    } else {
                        "Expand item"
                    },
                tint =
                    if (expanded) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                onClick = onToggleExpanded,
            )
        }
    }
}

@Composable
private fun ControlIconButton(
    drawableResId: Int,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color? = null,
) {
    val iconTint = tint ?: MaterialTheme.colorScheme.onSurfaceVariant

    IconButton(
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(id = drawableResId),
            contentDescription = contentDescription,
            tint = iconTint,
        )
    }
}

@Composable
private fun IntensitySelectorRow(
    intensityLevel: Int?,
    onIntensitySelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Intensity",
            style = MaterialTheme.typography.bodyMedium,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            (1..5).forEach { level ->

                val selected = intensityLevel == level

                OutlinedButton(
                    onClick = { onIntensitySelected(level) },
                ) {
                    Text(
                        text =
                            if (selected) {
                                "[$level]"
                            } else {
                                level.toString()
                            }
                    )
                }
            }
        }

        Text(
            text = "1=slow walk · 3=brisk walk · 5=run",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StarRatingRow(
    rating: Int?,
    onRatingChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        (1..5).forEach { star ->
            val filled = (rating ?: 0) >= star

            Text(
                text = if (filled) "★" else "☆",
                style = MaterialTheme.typography.headlineMedium,
                color = if (filled) {
                    KaPalette.Amber
                } else {
                    MaterialTheme.colorScheme.outline
                },
                modifier = Modifier
                    .padding(4.dp)
                    .clickable {
                        if (rating == star) {
                            onRatingChange(null)
                        } else {
                            onRatingChange(star)
                        }
                    },
            )
        }
    }
}

@Composable
private fun SetEditorRow(
    set: ActualExerciseSetLogEntity,
    onUpdateSet: (ActualExerciseSetLogEntity, Double?, Int?) -> Unit,
    onDeleteSet: (Long) -> Unit,
    onDuplicateSet: (ActualExerciseSetLogEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Set ${set.setOrder}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )

                SetActionIconButton(
                    drawableResId = R.drawable.copy,
                    contentDescription = "Copy set",
                    tint = MaterialTheme.colorScheme.secondary,
                    onClick = { onDuplicateSet(set) },
                )

                SetActionIconButton(
                    drawableResId = R.drawable.trash,
                    contentDescription = "Delete set",
                    tint = KaPalette.Danger,
                    onClick = { onDeleteSet(set.id) },
                )
            }

            WeightRepsInputRow(
                set = set,
                onUpdateSet = onUpdateSet,
            )

            set.notes
                ?.takeIf { it.isNotBlank() }
                ?.let { note ->
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
        }
    }
}

@Composable
private fun SetActionIconButton(
    drawableResId: Int,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(id = drawableResId),
            contentDescription = contentDescription,
            tint = tint,
        )
    }
}

@Composable
private fun WeightRepsInputRow(
    set: ActualExerciseSetLogEntity,
    onUpdateSet: (ActualExerciseSetLogEntity, Double?, Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var weightText by rememberSaveable(set.id) {
        mutableStateOf(set.weight?.let(::formatWeight) ?: "")
    }
    var repsText by rememberSaveable(set.id) {
        mutableStateOf(set.reps?.toString() ?: "")
    }

    LaunchedEffect(set.id, set.weight) {
        val entityWeightText = set.weight?.let(::formatWeight) ?: ""
        if (weightText != entityWeightText) {
            weightText = entityWeightText
        }
    }

    LaunchedEffect(set.id, set.reps) {
        val entityRepsText = set.reps?.toString() ?: ""
        if (repsText != entityRepsText) {
            repsText = entityRepsText
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NumberInputWithStepper(
            label = "Weight",
            value = weightText,
            onValueChange = { value ->
                weightText = value
                onUpdateSet(
                    set,
                    value.toDoubleOrNull(),
                    repsText.toIntOrNull(),
                )
            },
            onDecrement = {
                val newWeight = ((weightText.toDoubleOrNull() ?: set.weight ?: 0.0) - 2.5)
                    .coerceAtLeast(0.0)
                weightText = formatWeight(newWeight)
                onUpdateSet(
                    set,
                    newWeight,
                    repsText.toIntOrNull() ?: set.reps,
                )
            },
            onIncrement = {
                val newWeight = (weightText.toDoubleOrNull() ?: set.weight ?: 0.0) + 2.5
                weightText = formatWeight(newWeight)
                onUpdateSet(
                    set,
                    newWeight,
                    repsText.toIntOrNull() ?: set.reps,
                )
            },
            keyboardType = KeyboardType.Decimal,
            modifier = Modifier.weight(1f),
        )

        NumberInputWithStepper(
            label = "Reps",
            value = repsText,
            onValueChange = { value ->
                repsText = value
                onUpdateSet(
                    set,
                    weightText.toDoubleOrNull() ?: set.weight,
                    value.toIntOrNull(),
                )
            },
            onDecrement = {
                val newReps = ((repsText.toIntOrNull() ?: set.reps ?: 0) - 1)
                    .coerceAtLeast(0)
                repsText = newReps.toString()
                onUpdateSet(
                    set,
                    weightText.toDoubleOrNull() ?: set.weight,
                    newReps,
                )
            },
            onIncrement = {
                val newReps = (repsText.toIntOrNull() ?: set.reps ?: 0) + 1
                repsText = newReps.toString()
                onUpdateSet(
                    set,
                    weightText.toDoubleOrNull() ?: set.weight,
                    newReps,
                )
            },
            keyboardType = KeyboardType.Number,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun NumberInputWithStepper(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
            ),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            OutlinedButton(
                onClick = onDecrement,
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    painter = painterResource(R.drawable.minus),
                    contentDescription = "Decrease",
                )
            }

            OutlinedButton(
                onClick = onIncrement,
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    painter = painterResource(R.drawable.plus),
                    contentDescription = "Increase",
                )
            }
        }
    }
}

private fun buildSetSummary(
    sets: List<ActualExerciseSetLogEntity>,
): String {
    val weights = sets.mapNotNull { it.weight }

    if (sets.isEmpty()) {
        return "Sets: 0"
    }

    if (weights.isEmpty()) {
        return "Sets: ${sets.size}"
    }

    val minWeight = weights.minOrNull()
    val maxWeight = weights.maxOrNull()

    val weightText = if (minWeight == maxWeight) {
        formatWeight(minWeight ?: 0.0)
    } else {
        "${formatWeight(minWeight ?: 0.0)}–${formatWeight(maxWeight ?: 0.0)}"
    }

    return "Sets: ${sets.size} | Weight: $weightText"
}

private fun buildCardioDetails(
    log: ActualCardioLogEntity,
): List<String> {
    return buildList {
        log.distance?.let { distance ->
            log.distanceUnit?.let { unit ->
                add(
                    buildString {
                        append(formatDistance(distance))
                        append(" ")
                        append(unit)

                        if (log.isEstimatedDistance) {
                            append(" estimated")
                        }
                    }
                )
            }
        }

        log.durationSeconds?.let { seconds ->
            add(formatDuration(seconds))
        }

        log.averageInclinePercent?.let { incline ->
            add("${formatDistance(incline)}% incline")
        }
    }
}

private fun formatDuration(
    seconds: Long,
): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60

    return if (remainingSeconds == 0L) {
        "${minutes}m"
    } else {
        "${minutes}m ${remainingSeconds}s"
    }
}

private fun formatDistance(
    value: Double,
): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        value.toString()
    }
}

private fun estimateDistanceMiles(
    durationSeconds: Long,
    intensityLevel: Int,
): Double {
    val mph = when (intensityLevel) {
        1 -> 2.0
        2 -> 3.0
        3 -> 4.0
        4 -> 5.5
        5 -> 7.0
        else -> 3.0
    }

    val hours = durationSeconds / 3600.0

    return ((mph * hours) * 100).toInt() / 100.0
}

internal fun formatWeight(
    value: Double,
): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        value.toString()
    }
}