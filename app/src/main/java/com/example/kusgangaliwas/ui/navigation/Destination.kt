package com.example.kusgangaliwas.ui.navigation

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import com.example.kusgangaliwas.R
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource

sealed class Destination(
    val route: String,
    val label: String,
    @DrawableRes val icon: Int
) {
    data object Calendar : Destination(
        route = "calendar",
        label = "Calendar",
        icon = R.drawable.calendar_day,
    )

    data object Splits : Destination(
        route = "splits",
        label = "Splits",
        icon = R.drawable.list_tree,
    )

    data object Exercises : Destination(
        route = "exercises",
        label = "Exercises",
        icon = R.drawable.gym,
    )

    data object SplitRoadmap : Destination(
        route = "split_roadmap/{splitId}",
        label = "Split Roadmap",
        icon = R.drawable.list,
    ) {
        fun createRoute(splitId: Long): String = "split_roadmap/$splitId"
    }

    data object SessionDay : Destination(
        route = "session_day/{epochDay}",
        label = "Session Day",
        icon = R.drawable.calendar_day,
    ) {
        fun createRoute(epochDay: Long): String = "session_day/$epochDay"
    }

    data object SessionDetail : Destination(
        route = "session_detail/{actualSessionId}",
        label = "Session Detail",
        icon = R.drawable.list,
    ) {
        fun createRoute(actualSessionId: Long): String = "session_detail/$actualSessionId"
    }

    data object TrainingCycles : Destination(
        route = "training_cycles",
        label = "Cycles",
        icon = R.drawable.arrows_retweet__1_,
    )

    data object ExercisePicker : Destination(
        route = "exercise_picker/{splitId}",
        label = "Exercise Picker",
        icon = R.drawable.list,
    ) {
        fun createRoute(splitId: Long): String =
            "exercise_picker/$splitId"
    }
}

val bottomNavDestinations = listOf(
    Destination.Calendar,
    Destination.Splits,
    Destination.TrainingCycles,
    Destination.Exercises,
)