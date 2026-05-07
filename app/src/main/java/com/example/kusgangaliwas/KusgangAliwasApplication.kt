package com.example.kusgangaliwas

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class that owns the app-wide dependency container.
 *
 * This keeps dependency access simple while the app is still young.
 * Hilt can replace this later if needed.
 */
@HiltAndroidApp
class KusgangAliwasApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}