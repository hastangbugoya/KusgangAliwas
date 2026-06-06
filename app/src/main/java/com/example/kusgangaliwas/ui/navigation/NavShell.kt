package com.example.kusgangaliwas.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.kusgangaliwas.ui.theme.KaPalette

/**
 * Temporary navigation shell.
 *
 * initialSessionDetailId allows external entry points such as the gym widget
 * to open directly into a session detail screen.
 */
@Composable
fun NavShell(
    initialSessionDetailId: Long? = null,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    LaunchedEffect(initialSessionDetailId) {
        initialSessionDetailId?.let { sessionId ->
            navController.navigate(
                Destination.SessionDetail.createRoute(sessionId)
            )
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar(
                containerColor = KaPalette.IronSurface,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                tonalElevation = 0.dp,
            ) {
                bottomNavDestinations.forEach { destination ->
                    val selected = currentRoute == destination.route

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(destination.icon),
                                contentDescription = destination.label,
                            )
                        },
                        label = {
                            Text(destination.label)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = KaPalette.Amber,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            indicatorColor = KaPalette.IronSurfaceHighest,
                            unselectedIconColor =
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                            unselectedTextColor =
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            contentPadding = innerPadding,
        )
    }
}
