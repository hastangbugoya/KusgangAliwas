package com.example.kusgangaliwas.ui.session

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.kusgangaliwas.ui.common.KusgangTopBar
import com.example.kusgangaliwas.ui.common.SectionHeader
import com.example.kusgangaliwas.ui.common.SharpCard

@Composable
fun SessionDayScreen(
    uiState: SessionDayUiState,
    onBackClick: () -> Unit,
    onOverflowClick: () -> Unit,
    modifier: Modifier = Modifier,
    onStartQuickSession: () -> Unit,
    onActualSessionClick: (Long) -> Unit,
    onStartSplitSession: (Long) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            KusgangTopBar(
                title = uiState.title,
                onBackClick = onBackClick,
                onOverflowClick = onOverflowClick,
            )
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
                SharpCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader("Planned")

                        if (uiState.plannedSessions.isEmpty()) {
                            Text("No planned sessions for this day.")
                        } else {
                            uiState.plannedSessions.forEach { session ->
                                Text(session.title)
                            }
                        }
                    }
                }
            }

            item {
                SharpCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader("Logged")

                        if (uiState.actualSessions.isEmpty()) {
                            Text("No sessions logged yet.")
                        } else {
                            uiState.actualSessions.forEach { session ->
                                SharpCard(
                                    modifier = Modifier.clickable {
                                        onActualSessionClick(session.id)
                                    }
                                ) {
                                    Text(session.title)
                                }
                            }
                        }
                    }
                }
            }

            item {
                SharpCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader("Actions")
                        OutlinedButton(
                            onClick = {
                                onStartQuickSession()
                            },
                        ) {
                            Text("Start quick session")
                        }

                        if (uiState.availableSplits.isEmpty()) {

                            Text("No saved splits yet.")

                        } else {

                            Text("Start from saved split")

                            uiState.availableSplits.forEach { split ->

                                OutlinedButton(
                                    onClick = {
                                        onStartSplitSession(split.id)
                                    },
                                ) {
                                    Text(split.name)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}