package com.example.kusgangaliwas.ui.split

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun SplitRoadmapRoute(
    onBackClick: () -> Unit,
    onOverflowClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplitRoadmapViewModel = hiltViewModel(),
    onOpenExercisePicker: (Long) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    SplitRoadmapScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onOverflowClick = onOverflowClick,
        onAddExercise = viewModel::addExerciseToSplit,
        onDeleteExercise = viewModel::deleteExerciseFromSplit,
        onUpdateExerciseTargets = viewModel::updateExerciseTargets,
        onScheduleEnabledChange = viewModel::setScheduleEnabled,
        onToggleScheduleDay = viewModel::toggleScheduleDay,
        onHorizonWeeksTextChange = viewModel::updateHorizonWeeksText,
        onScheduleTitleChange = viewModel::updateScheduleTitle,
        onSaveSchedule = viewModel::saveSchedule,
        modifier = modifier,
        onRenameSplit = viewModel::renameSplit,
        onToggleMuscleGroupForSplit = viewModel::toggleMuscleGroupForSplit,
        onUpdateCardioTargets = viewModel::updateCardioTargets,
        onOpenExercisePicker = onOpenExercisePicker,
    )
}