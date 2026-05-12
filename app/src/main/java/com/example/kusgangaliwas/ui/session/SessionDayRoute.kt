package com.example.kusgangaliwas.ui.session

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun SessionDayRoute(
    onBackClick: () -> Unit,
    onOverflowClick: () -> Unit,
    onActualSessionClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SessionDayViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    SessionDayScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onOverflowClick = onOverflowClick,
        onStartQuickSession = viewModel::startQuickSession,
        onActualSessionClick = onActualSessionClick,
        modifier = modifier,
        onStartSplitSession = viewModel::startSessionFromSplit,
    )
}