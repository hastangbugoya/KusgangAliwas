package com.example.kusgangaliwas.ui.split

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun ExercisePickerRoute(
    onBackClick: () -> Unit,
    onOverflowClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExercisePickerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    ExercisePickerScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onOverflowClick = onOverflowClick,
        onSearchTextChange = viewModel::onSearchTextChange,
        onToggleMuscleGroup = viewModel::toggleMuscleGroup,
        onClearMuscleGroups = viewModel::clearMuscleGroupSelection,
        onToggleExercise = viewModel::toggleExercise,
        onAddSelectedClick = {
            viewModel.addSelectedExercises(
                onDone = onBackClick,
            )
        },
        modifier = modifier,
    )
}