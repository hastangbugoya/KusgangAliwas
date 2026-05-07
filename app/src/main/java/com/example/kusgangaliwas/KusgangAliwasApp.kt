package com.example.kusgangaliwas

import androidx.compose.runtime.Composable
import com.example.kusgangaliwas.ui.navigation.NavShell

/**
 * Root composable for Kusgang Aliwas.
 *
 * Temporary UI-1 shell:
 * - Starts directly on ExerciseListRoute
 * - Navigation graph will be added after the first screen builds cleanly
 */
@Composable
fun KusgangAliwasApp() {
    NavShell()
}