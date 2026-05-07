package com.example.kusgangaliwas.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
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
}

val bottomNavDestinations = listOf(
    Destination.Calendar,
    Destination.Splits,
    Destination.Exercises,
)