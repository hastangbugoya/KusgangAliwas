package com.example.kusgangaliwas.ui.calendar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.time.YearMonth

@Composable
fun CalendarRoute(
    onBackClick: () -> Unit,
    onOverflowClick: () -> Unit,
    onDayClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    CalendarScreen(
        month = YearMonth.now(),
        onBackClick = onBackClick,
        onOverflowClick = onOverflowClick,
        modifier = modifier,
        onDayClick = onDayClick,
    )
}