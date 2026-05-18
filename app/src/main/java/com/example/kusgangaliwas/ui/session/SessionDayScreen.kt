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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kusgangaliwas.ui.common.KusgangTopBar
import com.example.kusgangaliwas.ui.common.SectionHeader
import com.example.kusgangaliwas.ui.common.SharpCard
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.example.kusgangaliwas.R

@Composable
fun SessionDayScreen(
    uiState: SessionDayUiState,
    onBackClick: () -> Unit,
    onOverflowClick: () -> Unit,
    modifier: Modifier = Modifier,
    onStartQuickSession: () -> Unit,
    onActualSessionClick: (Long) -> Unit,
    onStartSplitSession: (Long) -> Unit,
    onStartCycleSession: (Long) -> Unit,
    onMarkCycleSplitDone: (Long) -> Unit,
    onStartPlannedSession: (Long) -> Unit,
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
                                SharpCard {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement =
                                            Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Text(session.title)

                                        OutlinedButton(
                                            onClick = {
                                                onStartPlannedSession(session.id)
                                            },
                                        ) {
                                            Text("Start")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                if (uiState.activeCycleContexts.isEmpty()) {
                    Text("No active cycles.")
                } else {
                    uiState.activeCycleContexts.forEach { cycleContext ->
                        SharpCard {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(cycleContext.trainingCycleName)

                                cycleContext.lastCompletedStepName?.let { last ->
                                    Text("Last: $last")
                                }

                                if (cycleContext.nextStepName != null) {
                                    Text("Next: ${cycleContext.nextStepName}")

                                    OutlinedButton(
                                        onClick = {
                                            onStartCycleSession(cycleContext.trainingCycleId)
                                        },
                                    ) {
                                        Text("Start cycle split")
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            onMarkCycleSplitDone(cycleContext.trainingCycleId)
                                        },
                                    ) {
                                        Text("Mark done")
                                    }
                                } else {
                                    Text("No cycle split available.")
                                }
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
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement =
                                            Arrangement.spacedBy(6.dp),
                                    ) {

                                        if (session.plannedSessionId != null) {
                                            Icon(
                                                painter = painterResource(
                                                    R.drawable.daily_calendar
                                                ),
                                                contentDescription =
                                                    "Planned session",
                                                modifier = Modifier.size(16.dp),
                                            )
                                        }

                                        if (session.trainingCycleId != null) {
                                            Icon(
                                                painter = painterResource(
                                                    R.drawable.arrows_retweet__1_
                                                ),
                                                contentDescription =
                                                    "Cycle session",
                                                modifier = Modifier.size(16.dp),
                                            )
                                        }

                                        if (
                                            session.plannedSessionId == null &&
                                            session.trainingCycleId == null
                                        ) {
                                            Icon(
                                                painter = painterResource(
                                                    R.drawable.gym
                                                ),
                                                contentDescription =
                                                    "Quick session",
                                                modifier = Modifier.size(16.dp),
                                            )
                                        }

                                        Text(
                                            text = session.title,
                                            fontSize = 14.sp,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                SharpCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader("Quick Session")

                        OutlinedButton(
                            onClick = onStartQuickSession,
                        ) {
                            Text("Start quick session")
                        }
                    }
                }
            }

            item {
                SharpCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader("Choose Split")

                        if (uiState.availableSplits.isEmpty()) {
                            Text("No saved splits yet.")
                        } else {
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