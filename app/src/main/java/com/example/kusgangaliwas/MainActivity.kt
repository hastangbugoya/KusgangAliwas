package com.example.kusgangaliwas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.kusgangaliwas.domain.usecase.planning.RefreshPlannedSessionsUseCase
import com.example.kusgangaliwas.ui.theme.KusgangAliwasTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var refreshPlannedSessionsUseCase: RefreshPlannedSessionsUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        refreshPlannedSessions()

        setContent {
            KusgangAliwasTheme {
                KusgangAliwasApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPlannedSessions()
    }

    private fun refreshPlannedSessions() {
        lifecycleScope.launch {
            runCatching {
                refreshPlannedSessionsUseCase()
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }
}