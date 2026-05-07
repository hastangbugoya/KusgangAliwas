package com.example.kusgangaliwas.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    data object Calendar : Destination(
        route = "calendar",
        label = "Calendar",
        icon = Icons.Default.DateRange,
    )

    data object Splits : Destination(
        route = "splits",
        label = "Splits",
        icon = Icons.Default.List,
    )

    data object Exercises : Destination(
        route = "exercises",
        label = "Exercises",
        icon = Icons.Default.Star,
    )

    data object SplitRoadmap : Destination(
        route = "split_roadmap/{splitId}",
        label = "Split Roadmap",
        icon = Icons.Default.List,
    ) {
        fun createRoute(splitId: Long): String = "split_roadmap/$splitId"
    }

    data object SessionDay : Destination(
        route = "session_day/{epochDay}",
        label = "Session Day",
        icon = Icons.Default.Build,
    ) {
        fun createRoute(epochDay: Long): String = "session_day/$epochDay"
    }

    data object SessionDetail : Destination(
        route = "session_detail/{actualSessionId}",
        label = "Session Detail",
        icon = Icons.Default.Face,
    ) {
        fun createRoute(actualSessionId: Long): String = "session_detail/$actualSessionId"
    }


}

val bottomNavDestinations = listOf(
    Destination.Calendar,
    Destination.Splits,
    Destination.Exercises,
)