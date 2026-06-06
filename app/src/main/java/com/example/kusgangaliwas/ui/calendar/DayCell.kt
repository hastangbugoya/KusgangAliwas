package com.example.kusgangaliwas.ui.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.kusgangaliwas.R
import com.example.kusgangaliwas.ui.theme.KaPalette

@Composable
fun DayCell(
    day: CalendarDayCellState,
    onClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isMutedMonthDay = !day.isInCurrentMonth

    val borderWidth = when {
        day.isToday -> 2.dp
        day.status == CalendarDayStatus.IN_PROGRESS -> 1.dp
        day.status == CalendarDayStatus.PLANNED -> 1.dp
        day.status == CalendarDayStatus.LOGGED -> 1.dp
        day.isCurrentWeek -> 1.dp
        else -> 0.dp
    }

    val borderColor = when {
        day.isToday -> KaPalette.Amber.copy(alpha = 0.95f)
        day.status == CalendarDayStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
        day.status == CalendarDayStatus.PLANNED -> KaPalette.SteelBlue.copy(alpha = 0.35f)
        day.status == CalendarDayStatus.LOGGED -> KaPalette.Success.copy(alpha = 0.35f)
        day.isCurrentWeek -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        else -> Color.Transparent
    }

    val containerColor =
        if (isMutedMonthDay) {
            MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.75f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .clickable {
                onClick(day.date.toEpochDay())
            },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = BorderStroke(
            width = borderWidth,
            color = borderColor,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight =
                    if (day.isToday) {
                        FontWeight.Bold
                    } else {
                        FontWeight.SemiBold
                    },
                color =
                    when {
                        day.isToday -> KaPalette.Amber
                        isMutedMonthDay -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )

            Icon(
                tint = iconTint(day.status),
                painter = painterResource(day.status.tempDrawableRes),
                contentDescription = day.status.contentDescription,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

private val CalendarDayStatus.tempDrawableRes: Int
    get() = when (this) {
        CalendarDayStatus.LOGGED -> R.drawable.check_circle
        CalendarDayStatus.PLANNED -> R.drawable.daily_calendar
        CalendarDayStatus.NEUTRAL -> R.drawable.empty_set
        CalendarDayStatus.IN_PROGRESS -> R.drawable.play
        CalendarDayStatus.TODAY -> R.drawable.target
    }

private val CalendarDayStatus.contentDescription: String
    get() = when (this) {
        CalendarDayStatus.LOGGED -> "Session logged"
        CalendarDayStatus.PLANNED -> "Planned session"
        CalendarDayStatus.NEUTRAL -> "No planned or logged session"
        CalendarDayStatus.IN_PROGRESS -> "Session in progress"
        CalendarDayStatus.TODAY -> "Today"
    }

@Composable
private fun iconTint(status: CalendarDayStatus): Color {
    return when (status) {
        CalendarDayStatus.LOGGED -> KaPalette.Success.copy(alpha = 0.85f)
        CalendarDayStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
        CalendarDayStatus.PLANNED -> KaPalette.SteelBlue.copy(alpha = 0.85f)
        CalendarDayStatus.TODAY -> KaPalette.Amber.copy(alpha = 0.95f)
        CalendarDayStatus.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    }
}
