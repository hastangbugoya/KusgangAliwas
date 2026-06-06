package com.example.kusgangaliwas.ui.session

import android.inputmethodservice.Keyboard
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kusgangaliwas.R
import com.example.kusgangaliwas.data.local.entity.ActualSessionEntity
import com.example.kusgangaliwas.domain.model.session.ActualSessionStatus
import com.example.kusgangaliwas.ui.common.KusgangTopBar
import com.example.kusgangaliwas.ui.theme.KaPalette
import androidx.compose.foundation.layout.RowScope

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
    onCompleteActualSession: (Long) -> Unit,
    onStartPlannedSession: (Long) -> Unit,
    onResumeCompletedSession: (Long) -> Unit,
) {
    val inProgressSessions = uiState.actualSessions.filter {
        it.status == ActualSessionStatus.IN_PROGRESS
    }

    val loggedSessions = uiState.actualSessions.filter {
        it.status != ActualSessionStatus.IN_PROGRESS
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
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
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                KaDaySection(
                    title = "In Progress",
                    count = inProgressSessions.size,
                    accentColor = KaPalette.Ember,
                ) {
                    if (inProgressSessions.isEmpty()) {
                        CompactEmptyText("No active workout sessions.")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            inProgressSessions.forEach { session ->
                                CompactSessionRow(
                                    session = session,
                                    accentColor = KaPalette.Ember,
                                    modifier = Modifier.clickable {
                                        onActualSessionClick(session.id)
                                    },
                                    trailingContent = {
                                        AccentIconButton(
                                            drawableResId = R.drawable.stop,
                                            contentDescription = "Complete session",
                                            tint = KaPalette.Ember,
                                            onClick = {
                                                onCompleteActualSession(session.id)
                                            },
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }

            item {
                KaDaySection(
                    title = "Planned",
                    count = uiState.plannedSessions.size,
                    accentColor = KaPalette.SteelBlue,
                ) {
                    if (uiState.plannedSessions.isEmpty()) {
                        CompactEmptyText("No planned sessions for this day.")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            uiState.plannedSessions.forEach { session ->
                                CompactTitleActionRow(
                                    title = session.title,
                                    detail = "Planned split",
                                    accentColor = KaPalette.SteelBlue,
                                    trailingContent = {
                                        AccentIconButton(
                                            drawableResId = R.drawable.play,
                                            contentDescription = "Start planned session",
                                            tint = MaterialTheme.colorScheme.primary,
                                            onClick = {
                                                onStartPlannedSession(session.id)
                                            },
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }

            item {
                KaDaySection(
                    title = "Cycle Queue",
                    count = uiState.activeCycleContexts.size,
                    accentColor = KaPalette.Purple,
                ) {
                    if (uiState.activeCycleContexts.isEmpty()) {
                        CompactEmptyText("No active cycles.")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            uiState.activeCycleContexts.forEach { cycleContext ->
                                CompactTitleActionRow(
                                    title = cycleContext.trainingCycleName,
                                    detail = buildCycleDetailText(
                                        lastStep = cycleContext.lastCompletedStepName,
                                        nextStep = cycleContext.nextStepName,
                                    ),
                                    accentColor = KaPalette.Purple,
                                    trailingContent = {
                                        if (cycleContext.nextStepName != null) {
                                            AccentIconButton(
                                                drawableResId = R.drawable.play,
                                                contentDescription = "Start cycle split",
                                                tint = MaterialTheme.colorScheme.primary,
                                                onClick = {
                                                    onStartCycleSession(
                                                        cycleContext.trainingCycleId
                                                    )
                                                },
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }

            item {
                KaDaySection(
                    title = "Logged",
                    count = loggedSessions.size,
                    accentColor = KaPalette.Success,
                ) {
                    if (loggedSessions.isEmpty()) {
                        CompactEmptyText("No completed sessions yet.")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            loggedSessions.forEach { session ->
                                CompactSessionRow(
                                    session = session,
                                    accentColor = KaPalette.Success,
                                    modifier = Modifier.clickable {
                                        onActualSessionClick(session.id)
                                    },
                                    trailingContent = {
                                        if (session.status == ActualSessionStatus.COMPLETED) {
                                            AccentIconButton(
                                                drawableResId = R.drawable.play_pause,
                                                contentDescription = "Resume session",
                                                tint = MaterialTheme.colorScheme.primary,
                                                onClick = {
                                                    onResumeCompletedSession(session.id)
                                                },
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }

            item {
                KaDaySection(
                    title = "Quick Actions",
                    accentColor = MaterialTheme.colorScheme.primary,
                ) {
                    Button(
                        onClick = onStartQuickSession,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        contentPadding = PaddingValues(
                            horizontal = 14.dp,
                            vertical = 10.dp,
                        ),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.play),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text("Start quick session")
                    }
                }
            }

            item {
                KaDaySection(
                    title = "Choose Split",
                    count = uiState.availableSplits.size,
                    accentColor = KaPalette.Amber,
                ) {
                    if (uiState.availableSplits.isEmpty()) {
                        CompactEmptyText("No saved splits yet.")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            uiState.availableSplits.forEach { split ->
                                CompactTitleActionRow(
                                    title = split.name,
                                    detail = "Saved split",
                                    accentColor = KaPalette.Amber,
                                    trailingContent = {
                                        AccentIconButton(
                                            drawableResId = R.drawable.plus,
                                            contentDescription = "Start split session",
                                            tint = MaterialTheme.colorScheme.primary,
                                            onClick = {
                                                onStartSplitSession(split.id)
                                            },
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KaDaySection(
    title: String,
    modifier: Modifier = Modifier,
    count: Int? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CompactSectionHeader(
                title = title,
                count = count,
                accentColor = accentColor,
            )

            content()
        }
    }
}

@Composable
private fun CompactSectionHeader(
    title: String,
    count: Int?,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title.uppercase(),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = MaterialTheme.typography.labelLarge.letterSpacing,
        )

        if (count != null) {
            Card(
                shape = RoundedCornerShape(999.dp),
                colors = CardDefaults.cardColors(
                    containerColor = accentColor.copy(alpha = 0.16f),
                    contentColor = accentColor,
                ),
            ) {
                Text(
                    text = count.toString(),
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun CompactTitleActionRow(
    title: String,
    detail: String,
    accentColor: Color,
    trailingContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    CompactRowCard(
        modifier = modifier,
        accentColor = accentColor,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        trailingContent()
    }
}

@Composable
private fun CompactSessionRow(
    session: ActualSessionEntity,
    accentColor: Color,
    trailingContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    CompactRowCard(
        modifier = modifier,
        accentColor = accentColor,
    ) {
        SessionSourceRow(
            session = session,
            modifier = Modifier.weight(1f),
        )

        trailingContent()
    }
}

@Composable
private fun CompactRowCard(
    accentColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = accentColor.copy(alpha = 0.32f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 11.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@Composable
private fun CompactEmptyText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun AccentIconButton(
    drawableResId: Int,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(40.dp),
    ) {
        Icon(
            painter = painterResource(drawableResId),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(23.dp),
        )
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SessionSourceIcon(session = session)

        Text(
            text = session.title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun SessionSourceIcon(
    session: ActualSessionEntity,
    modifier: Modifier = Modifier,
) {
    val iconResId =
        when {
            session.plannedSessionId != null -> R.drawable.daily_calendar
            session.trainingCycleId != null -> R.drawable.arrows_retweet__1_
            else -> R.drawable.time_fast
        }

    val tint =
        when {
            session.plannedSessionId != null -> KaPalette.SteelBlue
            session.trainingCycleId != null -> KaPalette.Purple
            else -> KaPalette.Amber
        }

    Card(
        modifier = modifier.size(30.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = tint.copy(alpha = 0.14f),
            contentColor = tint,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(iconResId),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(17.dp),
            )
        }
    }
}

private fun buildCycleDetailText(
    lastStep: String?,
    nextStep: String?,
): String {
    return when {
        lastStep != null && nextStep != null -> "Last: $lastStep  •  Next: $nextStep"
        lastStep != null -> "Last: $lastStep  •  No cycle split available"
        nextStep != null -> "Next: $nextStep"
        else -> "No cycle split available."
    }
}
