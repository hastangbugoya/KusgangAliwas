package com.example.kusgangaliwas.ui.exercise

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseListRoute(
    onBackClick: () -> Unit,
    onOverflowClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExerciseListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    ExerciseListScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onOverflowClick = onOverflowClick,
        onCreateExercise = viewModel::createExercise,
        modifier = modifier,
    )
}
