package com.example.kusgangaliwas.ui.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kusgangaliwas.ui.common.KusgangTopBar
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    month: YearMonth,
    onBackClick: () -> Unit,
    onOverflowClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val days = rememberCalendarMonthCells(month)

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
                month = month,
            )

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
                    )
                }
            }
        }
    }
}

private fun rememberCalendarMonthCells(
    month: YearMonth,
): List<CalendarDayCellState> {
    val firstDay = month.atDay(1)
    val daysBeforeMonth = firstDay.dayOfWeek.value % 7
    val firstVisibleDate = firstDay.minusDays(daysBeforeMonth.toLong())

    return List(42) { index ->
        val date = firstVisibleDate.plusDays(index.toLong())
        CalendarDayCellState(
            date = date,
            isInCurrentMonth = YearMonth.from(date) == month,
            placeholderIcon = "🟡",
        )
    }
}

data class CalendarDayCellState(
    val date: LocalDate,
    val isInCurrentMonth: Boolean,
    val placeholderIcon: String,
)

internal fun DayOfWeek.shortLabel(): String {
    return getDisplayName(TextStyle.SHORT, Locale.getDefault())
}