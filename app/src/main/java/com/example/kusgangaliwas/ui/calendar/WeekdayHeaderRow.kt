package com.example.kusgangaliwas.ui.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import java.time.DayOfWeek

@Composable
fun WeekdayHeaderRow(
    modifier: Modifier = Modifier,
) {
    val days = listOf(
        DayOfWeek.SUNDAY,
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
    )

    Row(
        modifier = modifier.fillMaxWidth(),
    ) {
        days.forEach { day ->
            Text(
                text = day.shortLabel(),
                modifier = Modifier
                    .weight(1f),   // 👈 key: match grid column width
                textAlign = TextAlign.Center, // 👈 center text inside cell
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}