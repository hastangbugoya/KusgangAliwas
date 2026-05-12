package com.example.kusgangaliwas.ui.calendar

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import java.time.DayOfWeek
import java.time.temporal.WeekFields
import java.util.Locale

@Composable
fun WeekdayHeaderRow(
    modifier: Modifier = Modifier,
) {
    val firstDayOfWeek = WeekFields
        .of(Locale.getDefault())
        .firstDayOfWeek

    val days = List(7) { index ->
        firstDayOfWeek.plusDays(index.toLong())
    }

    Row(
        modifier = modifier.fillMaxWidth(),
    ) {
        days.forEach { day ->
            Text(
                text = day.shortLabel(),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private fun DayOfWeek.plusDays(days: Long): DayOfWeek {
    val zeroBased = (value - 1 + days).mod(7)
    return DayOfWeek.of(zeroBased + 1)
}