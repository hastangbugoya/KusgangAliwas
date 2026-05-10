package com.example.kusgangaliwas

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.kusgangaliwas.domain.usecase.planning.RefreshPlannedSessionsUseCase
import com.example.kusgangaliwas.ui.theme.KusgangAliwasTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var refreshPlannedSessionsUseCase: RefreshPlannedSessionsUseCase

    private var remoteCaptureEnabled: Boolean = true

    private var tts: TextToSpeech? = null
    private var ttsReady: Boolean = false

    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        audioManager = getSystemService(AudioManager::class.java)

        initializeTextToSpeech()
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

    override fun onDestroy() {
        tts?.shutdown()
        tts = null

        abandonGymAudioFocus()

        super.onDestroy()
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            logRemoteKeyEvent(event)

            if (event.keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                speakForGym("Remote detected")
            }
        }

        if (!remoteCaptureEnabled) {
            return super.dispatchKeyEvent(event)
        }

        return when (event.keyCode) {
            KeyEvent.KEYCODE_MEDIA_PREVIOUS,
            KeyEvent.KEYCODE_MEDIA_NEXT,
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_SPACE -> {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    Log.d(
                        REMOTE_LOG_TAG,
                        "Captured gym remote key: ${KeyEvent.keyCodeToString(event.keyCode)}",
                    )
                }
                true
            }

            else -> super.dispatchKeyEvent(event)
        }
    }

    private fun initializeTextToSpeech() {
        tts = TextToSpeech(this) { status ->
            ttsReady = status == TextToSpeech.SUCCESS

            if (ttsReady) {
                tts?.language = Locale.getDefault()

                tts?.setOnUtteranceProgressListener(
                    object : UtteranceProgressListener() {

                        override fun onStart(utteranceId: String?) = Unit

                        override fun onDone(utteranceId: String?) {
                            abandonGymAudioFocus()
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            abandonGymAudioFocus()
                        }
                    }
                )

                Log.d(TTS_LOG_TAG, "TTS ready")
            } else {
                Log.w(TTS_LOG_TAG, "TTS initialization failed")
            }
        }
    }

    private fun speakForGym(text: String) {
        if (!ttsReady) return

        requestGymAudioFocus()

        tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "ka_gym_voice_${System.currentTimeMillis()}",
        )
    }

    private fun requestGymAudioFocus() {
        audioFocusRequest =
            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAcceptsDelayedFocusGain(false)
                .setOnAudioFocusChangeListener { }
                .build()

        audioFocusRequest?.let {
            audioManager.requestAudioFocus(it)
        }
    }

    private fun abandonGymAudioFocus() {
        audioFocusRequest?.let {
            audioManager.abandonAudioFocusRequest(it)
        }
    }

    private fun logRemoteKeyEvent(event: KeyEvent) {
        Log.d(
            REMOTE_LOG_TAG,
            "keyCode=${event.keyCode}, " +
                    "keyName=${KeyEvent.keyCodeToString(event.keyCode)}, " +
                    "scanCode=${event.scanCode}, " +
                    "deviceId=${event.deviceId}",
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

    private companion object {
        const val REMOTE_LOG_TAG = "KA_REMOTE"
        const val TTS_LOG_TAG = "KA_TTS"
    }
}