package com.example.kusgangaliwas.ui.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.kusgangaliwas.ui.common.SharpCard
import com.example.kusgangaliwas.R
import com.example.kusgangaliwas.ui.theme.MissedSessionsRed
import com.example.kusgangaliwas.ui.theme.PartialYellow
import com.example.kusgangaliwas.ui.theme.SuccessGreen

@Composable
fun DayCell(
    day: CalendarDayCellState,
    onClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    SharpCard(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable {
                onClick(day.date.toEpochDay())
            },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    tint = iconTint(day.status),
                    painter = painterResource(day.status.tempDrawableRes),
                    contentDescription = day.status.contentDescription,
                )
            }
        }
    }
}

private val CalendarDayStatus.tempDrawableRes: Int
    get() = when (this) {
        CalendarDayStatus.GREEN -> R.drawable.check_circle
        CalendarDayStatus.YELLOW -> R.drawable.interrogation
        CalendarDayStatus.RED -> R.drawable.exclamation
        CalendarDayStatus.NEUTRAL -> R.drawable.empty_set
    }

private val CalendarDayStatus.contentDescription: String
    get() = when (this) {
        CalendarDayStatus.GREEN -> "Complete"
        CalendarDayStatus.YELLOW -> "Partially complete"
        CalendarDayStatus.RED -> "Missed planned session"
        CalendarDayStatus.NEUTRAL -> "No planned or logged session"
    }

@Composable
private fun iconTint(status: CalendarDayStatus) : Color
{
    return when (status) {
        CalendarDayStatus.GREEN -> SuccessGreen
        CalendarDayStatus.YELLOW -> PartialYellow
        CalendarDayStatus.RED -> MissedSessionsRed
        else -> MaterialTheme.colorScheme.primary
    }
}

