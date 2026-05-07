package com.example.kusgangaliwas.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.kusgangaliwas.ui.exercise.ExerciseListRoute
import com.example.kusgangaliwas.ui.split.SplitListRoute

@Composable
fun NavHost(
    navController: NavHostController,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Destination.Calendar.route,
        modifier = modifier.padding(contentPadding),
    ) {
        composable(Destination.Calendar.route) {
            Text("Calendar placeholder")
        }

        composable(Destination.Splits.route) {
            SplitListRoute(
                onBackClick = {},
                onOverflowClick = {},
            )
        }

        composable(Destination.Exercises.route) {
            ExerciseListRoute(
                onBackClick = {},
                onOverflowClick = {},
            )
        }
    }
}