package com.example.kusgangaliwas.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.kusgangaliwas.domain.model.WeeklyTrainingProgress
import com.example.kusgangaliwas.ui.common.KusgangTopBar
import com.example.kusgangaliwas.ui.common.SharpCard
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import java.time.temporal.WeekFields

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
        topBar = {
            KusgangTopBar(
                title = "Calendar",
                onBackClick = onBackClick,
                onOverflowClick = onOverflowClick,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MonthHeader(
                month = uiState.month,
            )

            uiState.weeklyProgress?.let { progress ->
                WeeklyProgressCard(
                    progress = progress,
                )
            }

            WeekdayHeaderRow()

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(days) { day ->
                    DayCell(
                        day = day,
                        onClick = onDayClick,
                    )
                }
            }
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

    SharpCard(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
            ),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
//            Text(
//                text = "Weekly progress",
//                style = MaterialTheme.typography.titleMedium,
//            )

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
}

@Composable
private fun WeeklyMetricBarRow(
    label: String,
    dayLabels: List<String>,
    values: List<Double>,
    maxValue: Double,
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
                                .background(MaterialTheme.colorScheme.primary)
                                .align(Alignment.BottomCenter)
                        )
                    }

                    Text(
                        text = valueText(value),
                        style = MaterialTheme.typography.bodySmall,
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