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
        onSaveActualOneRepMax = viewModel::saveActualOneRepMax,
        onDeleteActualOneRepMax = viewModel::deleteActualOneRepMax,
        modifier = modifier,
        onToggleMuscleGroupForExercise = viewModel::toggleMuscleGroupForExercise,
        selectedFilterMuscleGroupIds = uiState.selectedFilterMuscleGroupIds,
        onToggleFilterMuscleGroup = viewModel::toggleFilterMuscleGroup,
        onClearMuscleGroupFilters = viewModel::clearMuscleGroupFilters,
        searchQuery = uiState.searchQuery,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onCreateMuscleGroup = viewModel::createMuscleGroup,
        onRenameMuscleGroup = viewModel::renameMuscleGroup,
        onDeleteMuscleGroup = viewModel::deleteMuscleGroup,
        onRenameExercise = viewModel::renameExercise,
        onCreateMotivationalGoal = viewModel::createMotivationalGoal,
        onDeactivateMotivationalGoal = viewModel::deactivateMotivationalGoal,
        onRestoreMotivationalGoal = viewModel::restoreMotivationalGoal,
        onDeleteMotivationalGoal = viewModel::deleteMotivationalGoal,
        onCreatePaceProfile = viewModel::createPaceProfile,
        onUpdatePaceProfile = viewModel::updatePaceProfile,
        onSetPaceProfileAsDefault = viewModel::setPaceProfileAsDefault,
        onTogglePaceProfileEnabled = viewModel::togglePaceProfileEnabled,
        onDeletePaceProfile = viewModel::deletePaceProfile,
    )
}