package com.example.kusgangaliwas.ui.session

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.kusgangaliwas.ui.split.ExercisePickerScreen

@Composable
fun SessionExercisePickerRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SessionExercisePickerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    ExercisePickerScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onOverflowClick = {},
        onSearchTextChange = viewModel::onSearchTextChange,
        onToggleExercise = viewModel::toggleExercise,
        onAddSelectedClick = {
            viewModel.addSelectedExercisesToSession(
                onDone = onBackClick,
            )
        },
        modifier = modifier,
    )
}