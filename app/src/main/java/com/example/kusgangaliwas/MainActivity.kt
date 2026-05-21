package com.example.kusgangaliwas

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.kusgangaliwas.domain.gymremote.GymRemoteInput
import com.example.kusgangaliwas.domain.gymremote.GymRemoteInputBus
import com.example.kusgangaliwas.domain.usecase.planning.RefreshPlannedSessionsUseCase
import com.example.kusgangaliwas.service.GymRemoteService
import com.example.kusgangaliwas.ui.navigation.NavShell
import com.example.kusgangaliwas.ui.theme.KusgangAliwasTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var refreshPlannedSessionsUseCase: RefreshPlannedSessionsUseCase

    @Inject
    lateinit var gymRemoteInputBus: GymRemoteInputBus

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Log.d(REMOTE_LOG_TAG, "POST_NOTIFICATIONS granted=$granted")
            startGymRemoteService()
        }

    private var remoteCaptureEnabled: Boolean = true

    private var initialSessionDetailId by mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        initialSessionDetailId = intent.extractInitialSessionDetailId()

        requestNotificationPermissionThenStartGymRemoteService()
        refreshPlannedSessions()

        setContent {
            KusgangAliwasTheme {
                NavShell(
                    initialSessionDetailId = initialSessionDetailId,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        setIntent(intent)
        initialSessionDetailId = intent.extractInitialSessionDetailId()
    }

    override fun onResume() {
        super.onResume()
        refreshPlannedSessions()
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val input = event.toGymRemoteInput()

        if (input != null && remoteCaptureEnabled) {
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                logRemoteKeyEvent(event)
                Log.d(REMOTE_LOG_TAG, "Emitting gym remote input from activity: $input")
                gymRemoteInputBus.emit(input)
            }

            return true
        }

        return super.dispatchKeyEvent(event)
    }

    private fun Intent.extractInitialSessionDetailId(): Long? {
        if (action != ACTION_OPEN_SESSION_DETAIL_FROM_WIDGET) {
            return null
        }

        val sessionId = getLongExtra(
            EXTRA_ACTUAL_SESSION_ID,
            INVALID_SESSION_ID,
        )

        return sessionId.takeIf { it != INVALID_SESSION_ID }
    }

    private fun requestNotificationPermissionThenStartGymRemoteService() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            startGymRemoteService()
            return
        }

        val permission = Manifest.permission.POST_NOTIFICATIONS

        val hasPermission = ContextCompat.checkSelfPermission(
            this,
            permission,
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            startGymRemoteService()
            return
        }

        notificationPermissionLauncher.launch(permission)
    }

    private fun startGymRemoteService() {
        val intent = GymRemoteService.startIntent(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun KeyEvent.toGymRemoteInput(): GymRemoteInput? {
        return when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_SPACE -> GymRemoteInput.Confirm

            KeyEvent.KEYCODE_MEDIA_NEXT,
            KeyEvent.KEYCODE_DPAD_RIGHT -> GymRemoteInput.Next

            KeyEvent.KEYCODE_MEDIA_PREVIOUS,
            KeyEvent.KEYCODE_DPAD_LEFT -> GymRemoteInput.Previous

            KeyEvent.KEYCODE_VOLUME_UP -> GymRemoteInput.Increment

            KeyEvent.KEYCODE_VOLUME_DOWN -> GymRemoteInput.Decrement

            else -> null
        }
    }

    private fun logRemoteKeyEvent(event: KeyEvent) {
        Log.d(
            REMOTE_LOG_TAG,
            "activity keyCode=${event.keyCode}, " +
                    "keyName=${KeyEvent.keyCodeToString(event.keyCode)}, " +
                    "scanCode=${event.scanCode}, " +
                    "deviceId=${event.deviceId}, " +
                    "repeatCount=${event.repeatCount}",
        )
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

    companion object {
        const val ACTION_OPEN_SESSION_DETAIL_FROM_WIDGET =
            "com.example.kusgangaliwas.action.OPEN_SESSION_DETAIL_FROM_WIDGET"

        const val EXTRA_ACTUAL_SESSION_ID =
            "com.example.kusgangaliwas.extra.ACTUAL_SESSION_ID"

        private const val INVALID_SESSION_ID = -1L
        private const val REMOTE_LOG_TAG = "KA_REMOTE"
    }
}