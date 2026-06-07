package com.example.kusgangaliwas.ui.split

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun SplitListRoute(
    onBackClick: () -> Unit,
    onOverflowClick: () -> Unit,
    onSplitClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplitListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    SplitListScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onOverflowClick = onOverflowClick,
        onCreateSplit = viewModel::createSplit,
        onDeleteSplit = viewModel::deleteSplit,
        onRestoreSplit = viewModel::restoreSplit,
        onSplitClick = onSplitClick,
        modifier = modifier,
    )
}