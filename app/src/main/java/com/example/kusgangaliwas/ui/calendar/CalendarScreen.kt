package com.example.kusgangaliwas.ui.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
                .padding(12.dp),
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
                modifier = Modifier.fillMaxSize(),
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
    SharpCard(
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "This week",
                style = MaterialTheme.typography.titleMedium,
            )

            progress.days.forEach { day ->

                val date = LocalDate.ofEpochDay(day.epochDay)

                val strengthText =
                    if (day.strengthVolume > 0.0) {
                        "${day.strengthVolume}"
                    } else {
                        "—"
                    }

                val cardioText =
                    if (day.cardioDistance > 0.0) {
                        "${day.cardioDistance} mi (${day.completedCardioEntries})"
                    } else {
                        "—"
                    }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = date.dayOfWeek.shortLabel(),
                        modifier = Modifier.weight(1f),
                    )

                    Text(
                        text = strengthText,
                        modifier = Modifier.weight(1f),
                    )

                    Text(
                        text = cardioText,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
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

    return List(42) { index ->
        val date = firstVisibleDate.plusDays(index.toLong())

        CalendarDayCellState(
            date = date,
            isInCurrentMonth = YearMonth.from(date) == month,
            status = dayStatusByEpochDay[date.toEpochDay()]
                ?: CalendarDayStatus.NEUTRAL,
            isToday = date == LocalDate.now(),
        )
    }
}

data class CalendarDayCellState(
    val date: LocalDate,
    val isInCurrentMonth: Boolean,
    val status: CalendarDayStatus,
    val isToday: Boolean = false,
)

internal fun DayOfWeek.shortLabel(): String {
    return getDisplayName(TextStyle.SHORT, Locale.getDefault())
}