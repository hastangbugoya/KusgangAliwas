package com.example.kusgangaliwas.ui.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.kusgangaliwas.domain.model.WeeklyTrainingProgress
import com.example.kusgangaliwas.domain.model.cycle.CycleDayContext
import com.example.kusgangaliwas.ui.theme.KaPalette
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    uiState: CalendarUiState,
    onBackClick: () -> Unit,
    onOverflowClick: () -> Unit,
    onDayClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val days = rememberCalendarMonthCells(
        month = uiState.month,
        dayStatusByEpochDay = uiState.dayStatusByEpochDay,
    )

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Calendar",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                },
                actions = {
                    IconButton(
                        onClick = onOverflowClick,
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                MonthHeader(
                    month = uiState.month,
                )
            }

            item {
                WeekdayHeaderRow()
            }

            items(items = days.chunked(7)) { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    week.forEach { day ->
                        DayCell(
                            day = day,
                            onClick = onDayClick,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            item {
                ActiveCyclesCard(
                    cycles = uiState.activeCycleContexts,
                )
            }

            uiState.weeklyProgress?.let { progress ->
                item {
                    WeeklyProgressCard(
                        progress = progress,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveCyclesCard(
    cycles: List<CycleDayContext>,
    modifier: Modifier = Modifier,
) {
    RootSectionCard(
        title = "Active cycles",
        accentColor = KaPalette.Purple.copy(alpha = 0.75f),
        modifier = modifier,
    ) {
        if (cycles.isEmpty()) {
            Text(
                text = "No active cycles.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            cycles.forEach { cycle ->
                CycleSummaryRow(cycle = cycle)
            }
        }
    }
}

@Composable
private fun CycleSummaryRow(
    cycle: CycleDayContext,
    modifier: Modifier = Modifier,
) {
    val detailText = buildList {
        cycle.lastCompletedStepName?.let { last ->
            add("Last: $last")
        }

        cycle.nextStepName?.let { next ->
            add("Next: $next")
        }
    }.joinToString("  ·  ")
        .ifBlank { "No cycle split available." }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = cycle.trainingCycleName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = detailText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WeeklyProgressCard(
    progress: WeeklyTrainingProgress,
    modifier: Modifier = Modifier,
) {
    val maxStrength =
        progress.days.maxOfOrNull { it.strengthVolume }
            ?.takeIf { it > 0.0 }
            ?: 1.0

    val maxCardio =
        progress.days.maxOfOrNull { it.cardioDistance }
            ?.takeIf { it > 0.0 }
            ?: 1.0

    RootSectionCard(
        title = "Weekly progress",
        accentColor = KaPalette.SteelBlue.copy(alpha = 0.75f),
        modifier = modifier,
    ) {
        WeeklyMetricBarRow(
            label = "Strength",
            dayLabels = progress.days.map {
                LocalDate
                    .ofEpochDay(it.epochDay)
                    .dayOfWeek
                    .shortLabel()
            },
            values = progress.days.map { it.strengthVolume },
            maxValue = maxStrength,
            barColor = KaPalette.Amber.copy(alpha = 0.85f),
            valueText = { value ->
                if (value > 0.0) {
                    "${value.toInt()}"
                } else {
                    "—"
                }
            },
        )

        WeeklyMetricBarRow(
            label = "Cardio (miles)",
            dayLabels = progress.days.map {
                LocalDate
                    .ofEpochDay(it.epochDay)
                    .dayOfWeek
                    .shortLabel()
            },
            values = progress.days.map { it.cardioDistance },
            maxValue = maxCardio,
            barColor = KaPalette.SteelBlue.copy(alpha = 0.85f),
            valueText = { value ->
                if (value > 0.0) {
                    "${formatDistance(value)}"
                } else {
                    "—"
                }
            },
        )
    }
}

@Composable
private fun RootSectionCard(
    title: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            RootSectionHeader(
                title = title,
                accentColor = accentColor,
            )

            content()
        }
    }
}

@Composable
private fun RootSectionHeader(
    title: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(accentColor),
        )

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun WeeklyMetricBarRow(
    label: String,
    dayLabels: List<String>,
    values: List<Double>,
    maxValue: Double,
    barColor: Color,
    valueText: (Double) -> String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            values.forEachIndexed { index, value ->
                val ratio = (value / maxValue)
                    .toFloat()
                    .coerceIn(0f, 1f)

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = dayLabels[index],
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((36 * ratio).dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(barColor)
                                .align(Alignment.BottomCenter)
                        )
                    }

                    Text(
                        text = valueText(value),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

private fun formatDistance(
    value: Double,
): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        value.toString()
    }
}

private fun rememberCalendarMonthCells(
    month: YearMonth,
    dayStatusByEpochDay: Map<Long, CalendarDayStatus>,
): List<CalendarDayCellState> {
    val firstDay = month.atDay(1)
    val firstDayOfWeek = WeekFields
        .of(Locale.getDefault())
        .firstDayOfWeek

    val daysBeforeMonth = (
            firstDay.dayOfWeek.value -
                    firstDayOfWeek.value +
                    7
            ) % 7

    val firstVisibleDate = firstDay.minusDays(daysBeforeMonth.toLong())

    val today = LocalDate.now()
    val currentWeekStart = today.minusDays(
        (
                today.dayOfWeek.value -
                        firstDayOfWeek.value +
                        7
                ) % 7L
    )
    val currentWeekEndExclusive = currentWeekStart.plusDays(7)

    return List(42) { index ->
        val date = firstVisibleDate.plusDays(index.toLong())

        CalendarDayCellState(
            date = date,
            isInCurrentMonth = YearMonth.from(date) == month,
            status = dayStatusByEpochDay[date.toEpochDay()]
                ?: CalendarDayStatus.NEUTRAL,
            isToday = date == LocalDate.now(),
            isCurrentWeek = date >= currentWeekStart && date < currentWeekEndExclusive,
        )
    }
}

data class CalendarDayCellState(
    val date: LocalDate,
    val isInCurrentMonth: Boolean,
    val status: CalendarDayStatus,
    val isToday: Boolean = false,
    val isCurrentWeek: Boolean = false,
)

internal fun DayOfWeek.shortLabel(): String {
    return getDisplayName(TextStyle.SHORT, Locale.getDefault())
}
