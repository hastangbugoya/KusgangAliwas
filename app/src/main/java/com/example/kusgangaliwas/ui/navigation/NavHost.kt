package com.example.kusgangaliwas.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.kusgangaliwas.ui.calendar.CalendarRoute
import com.example.kusgangaliwas.ui.exercise.ExerciseListRoute
import com.example.kusgangaliwas.ui.session.SessionDayRoute
import com.example.kusgangaliwas.ui.session.SessionDetailRoute
import com.example.kusgangaliwas.ui.split.SplitListRoute
import com.example.kusgangaliwas.ui.split.SplitRoadmapRoute

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
            CalendarRoute(
                onBackClick = {},
                onOverflowClick = {},
                onDayClick = { epochDay ->
                    navController.navigate(Destination.SessionDay.createRoute(epochDay))
                },
            )
        }

        composable(Destination.Splits.route) {
            SplitListRoute(
                onBackClick = {},
                onOverflowClick = {},
                onSplitClick = { splitId ->
                    navController.navigate(Destination.SplitRoadmap.createRoute(splitId))
                },
            )
        }

        composable(Destination.Exercises.route) {
            ExerciseListRoute(
                onBackClick = {},
                onOverflowClick = {},
            )
        }

        composable(
                route = Destination.SplitRoadmap.route,
            arguments = listOf(
            navArgument("splitId") {
                type = NavType.LongType
            }
        )
        ) {
            SplitRoadmapRoute(
                onBackClick = {
                    navController.popBackStack()
                },
                onOverflowClick = {},
            )
        }

        composable(
            route = Destination.SessionDay.route,
            arguments = listOf(
                navArgument("epochDay") {
                    type = NavType.LongType
                }
            )
        ) {
            SessionDayRoute(
                onBackClick = {
                    navController.popBackStack()
                },
                onOverflowClick = {},
                onActualSessionClick = { actualSessionId ->
                    navController.navigate(Destination.SessionDetail.createRoute(actualSessionId))
                },
            )
        }

        composable(
                route = Destination.SessionDetail.route,
        arguments = listOf(
            navArgument("actualSessionId") {
                type = NavType.LongType
            }
        )
        ) {
        SessionDetailRoute(
            onBackClick = {
                navController.popBackStack()
            },
            onOverflowClick = {},
        )
    }



    }
}