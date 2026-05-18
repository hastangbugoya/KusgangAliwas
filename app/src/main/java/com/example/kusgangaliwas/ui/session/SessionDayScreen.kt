package com.example.kusgangaliwas.ui.session

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kusgangaliwas.R
import com.example.kusgangaliwas.data.local.entity.ActualSessionEntity
import com.example.kusgangaliwas.domain.model.session.ActualSessionStatus
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
    onStartCycleSession: (Long) -> Unit,
    onMarkCycleSplitDone: (Long) -> Unit,
    onStartPlannedSession: (Long) -> Unit,
) {

    val inProgressSessions = uiState.actualSessions.filter {
        it.status == ActualSessionStatus.IN_PROGRESS
    }

    val loggedSessions = uiState.actualSessions.filter {
        it.status != ActualSessionStatus.IN_PROGRESS
    }

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
                        SectionHeader("In Progress")

                        if (inProgressSessions.isEmpty()) {
                            Text("No active workout sessions.")
                        } else {
                            inProgressSessions.forEach { session ->
                                SharpCard(
                                    modifier = Modifier.clickable {
                                        onActualSessionClick(session.id)
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement =
                                            Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        SessionSourceRow(
                                            session = session,
                                            modifier = Modifier.weight(1f),
                                        )

                                        if (session.trainingCycleId != null) {
                                            IconButton(
                                                onClick = {
                                                    onMarkCycleSplitDone(session.id)
                                                },
                                            ) {
                                                Icon(
                                                    painter = painterResource(
                                                        R.drawable.stop
                                                    ),
                                                    contentDescription =
                                                        "Mark cycle split done",
                                                )
                                            }
                                        }
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
                                        Text(
                                            text = session.title,
                                            modifier = Modifier.weight(1f),
                                        )

                                        IconButton(
                                            onClick = {
                                                onStartPlannedSession(session.id)
                                            },
                                        ) {
                                            Icon(
                                                painter = painterResource(
                                                    R.drawable.play
                                                ),
                                                contentDescription =
                                                    "Start planned session",
                                            )
                                        }
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
                        SectionHeader("Cycle Queue")

                        if (uiState.activeCycleContexts.isEmpty()) {
                            Text("No active cycles.")
                        } else {
                            uiState.activeCycleContexts.forEach { cycleContext ->
                                SharpCard {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement =
                                                Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                text = cycleContext.trainingCycleName,
                                                modifier = Modifier.weight(1f),
                                            )

                                            if (cycleContext.nextStepName != null) {
                                                IconButton(
                                                    onClick = {
                                                        onStartCycleSession(
                                                            cycleContext.trainingCycleId
                                                        )
                                                    },
                                                ) {
                                                    Icon(
                                                        painter = painterResource(
                                                            R.drawable.play
                                                        ),
                                                        contentDescription =
                                                            "Start cycle split",
                                                    )
                                                }
                                            }
                                        }

                                        cycleContext.lastCompletedStepName?.let { last ->
                                            Text("Last: $last")
                                        }

                                        if (cycleContext.nextStepName != null) {
                                            Text("Next: ${cycleContext.nextStepName}")
                                        } else {
                                            Text("No cycle split available.")
                                        }
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
                        SectionHeader("Logged")

                        if (loggedSessions.isEmpty()) {
                            Text("No completed sessions yet.")
                        } else {
                            loggedSessions.forEach { session ->
                                SharpCard(
                                    modifier = Modifier.clickable {
                                        onActualSessionClick(session.id)
                                    }
                                ) {
                                    SessionSourceRow(
                                        session = session,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                SharpCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            SectionHeader("Quick Actions")

                            IconButton(
                                onClick = onStartQuickSession,
                            ) {
                                Icon(
                                    painter = painterResource(
                                        R.drawable.play
                                    ),
                                    contentDescription =
                                        "Start quick session",
                                )
                            }
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

@Composable
private fun SessionSourceRow(
    session: ActualSessionEntity,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
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
                    R.drawable.time_fast
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