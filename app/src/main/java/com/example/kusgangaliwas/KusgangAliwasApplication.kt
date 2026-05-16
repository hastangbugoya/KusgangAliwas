package com.example.kusgangaliwas

import android.app.Application
import com.example.kusgangaliwas.domain.usecase.exercise.SeedDefaultMuscleGroupsUseCase
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application class that owns the app-wide dependency container.
 *
 * This keeps dependency access simple while the app is still young.
 * Hilt can replace this later if needed.
 */
@HiltAndroidApp
class KusgangAliwasApplication : Application() {

    @Inject
    lateinit var seedDefaultMuscleGroupsUseCase: SeedDefaultMuscleGroupsUseCase

    private val applicationScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        applicationScope.launch {
            seedDefaultMuscleGroupsUseCase()
        }
    }
}