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
import com.example.kusgangaliwas.domain.gymremote.GymRemoteInput
import com.example.kusgangaliwas.domain.gymremote.GymRemoteInputBus
import com.example.kusgangaliwas.domain.gymremote.GymVoiceBus
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

    @Inject
    lateinit var gymRemoteInputBus: GymRemoteInputBus

    @Inject
    lateinit var gymVoiceBus: GymVoiceBus

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
        collectGymVoice()
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
        tts?.stop()
        tts?.shutdown()
        tts = null

        abandonGymAudioFocus()

        super.onDestroy()
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val input = event.toGymRemoteInput()

        if (input != null && remoteCaptureEnabled) {
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                logRemoteKeyEvent(event)
                Log.d(REMOTE_LOG_TAG, "Emitting gym remote input: $input")
                gymRemoteInputBus.emit(input)
            }

            return true
        }

        return super.dispatchKeyEvent(event)
    }

    private fun collectGymVoice() {
        lifecycleScope.launch {
            gymVoiceBus.messages.collect { message ->
                Log.d(TTS_LOG_TAG, "Speaking: $message")
                speakForGym(message)
            }
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
        if (!ttsReady) {
            Log.d(TTS_LOG_TAG, "TTS not ready. Dropping speech: $text")
            return
        }

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

    private companion object {
        const val REMOTE_LOG_TAG = "KA_REMOTE"
        const val TTS_LOG_TAG = "KA_TTS"
    }
}