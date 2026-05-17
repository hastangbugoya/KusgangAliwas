package com.example.kusgangaliwas.ui.cycle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun TrainingCycleRoute(
    onBackClick: () -> Unit,
    onOverflowClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrainingCycleViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    TrainingCycleScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onOverflowClick = onOverflowClick,
        onSelectCycle = viewModel::selectCycle,
        onCreateCycleExpandedChange =
            viewModel::setCreateCycleExpanded,
        onNewCycleNameChange = viewModel::onNewCycleNameChange,
        onNewCycleNotesChange = viewModel::onNewCycleNotesChange,
        onCreateCycleClick = viewModel::createCycle,
        onAddSplitClick = viewModel::addSplitToSelectedCycle,
        onRemoveStepClick = viewModel::removeStep,
        onMoveStepUpClick = viewModel::moveStepUp,
        onMoveStepDownClick = viewModel::moveStepDown,
        onToggleWarnBeforeMarkDoneClick =
            viewModel::toggleWarnBeforeMarkDone,
        onDeleteSelectedCycleClick =
            viewModel::softDeleteSelectedCycle,
        modifier = modifier,
    )
}