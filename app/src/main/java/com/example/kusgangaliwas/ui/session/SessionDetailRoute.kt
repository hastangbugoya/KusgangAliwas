package com.example.kusgangaliwas.ui.session

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun SessionDetailRoute(
    onBackClick: () -> Unit,
    onOverflowClick: () -> Unit,
    onOpenExercisePicker: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SessionDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    SessionDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onOverflowClick = onOverflowClick,
//        onAddExercise = viewModel::addExercise,
//        onAddCardio = viewModel::addCardio,
        onUpdateCardioLog = viewModel::updateCardioLog,
        onDeleteCardioLog = viewModel::deleteCardioLog,
        onAddSet = viewModel::addSet,
        onUpdateSet = viewModel::updateSet,
        onDeleteSet = viewModel::deleteSet,
        onDuplicateSet = viewModel::duplicateSet,
        modifier = modifier,
        onRatingChange = viewModel::updateSessionRating,
        onDeleteExerciseLogIfEmpty = viewModel::deleteExerciseLogIfEmpty,
        onDeleteSession = {
            viewModel.deleteSession()
            onBackClick()
        },
        onMoveSessionItemUp = viewModel::moveSessionItemUp,
        onMoveSessionItemDown = viewModel::moveSessionItemDown,
        onToggleRemoteFocus = viewModel::toggleRemoteFocus,
        onUpdateSavedSplit = viewModel::updateSavedSplitFromSession,
        onCreateSavedSplit = viewModel::createSavedSplitFromSession,
        onOpenExercisePicker = {
            onOpenExercisePicker(viewModel.actualSessionId)
        },
    )
}