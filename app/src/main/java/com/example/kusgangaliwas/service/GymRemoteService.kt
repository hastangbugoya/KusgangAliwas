package com.example.kusgangaliwas.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import com.example.kusgangaliwas.MainActivity
import com.example.kusgangaliwas.R
import com.example.kusgangaliwas.domain.gymremote.GymRemoteInput
import com.example.kusgangaliwas.domain.gymremote.GymRemoteInputBus
import com.example.kusgangaliwas.domain.gymremote.GymRemoteMediaControlBus
import com.example.kusgangaliwas.domain.gymremote.GymRemoteMediaControlCommand
import com.example.kusgangaliwas.domain.gymremote.GymVoiceBus
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GymRemoteService : Service() {

    @Inject
    lateinit var gymRemoteInputBus: GymRemoteInputBus

    @Inject
    lateinit var gymVoiceBus: GymVoiceBus

    @Inject
    lateinit var gymRemoteMediaControlBus: GymRemoteMediaControlBus

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var mediaSession: MediaSession? = null

    private var tts: TextToSpeech? = null
    private var ttsReady: Boolean = false

    private lateinit var audioManager: AudioManager

    private var gymModeAudioFocusRequest: AudioFocusRequest? = null
    private var speechAudioFocusRequest: AudioFocusRequest? = null

    private var silentAudioTrack: AudioTrack? = null
    private var silentAudioThread: Thread? = null
    private var silentAudioRunning: Boolean = false

    private var pendingYieldToExternalMedia: Boolean = false

    private var suppressNextSelfDispatchedPlay: Boolean = false

    override fun onCreate() {
        super.onCreate()

        audioManager = getSystemService(AudioManager::class.java)

        initializeTextToSpeech()
        initializeMediaSession()
        collectGymVoice()
        collectMediaControlCommands()

        Log.d(REMOTE_LOG_TAG, "GymRemoteService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                Log.d(REMOTE_LOG_TAG, "Stopping GymRemoteService")
                stopSelf()
                return START_NOT_STICKY
            }

            ACTION_START,
            null -> {
                Log.d(REMOTE_LOG_TAG, "Starting GymRemoteService foreground mode")
                startForeground(NOTIFICATION_ID, buildNotification())
                reclaimGymRemoteControl()
                return START_STICKY
            }

            else -> {
                Log.d(REMOTE_LOG_TAG, "Unknown action=${intent.action}; keeping service alive")
                startForeground(NOTIFICATION_ID, buildNotification())
                reclaimGymRemoteControl()
                return START_STICKY
            }
        }
    }

    override fun onDestroy() {
        Log.d(REMOTE_LOG_TAG, "GymRemoteService destroyed")

        stopSilentAudioAnchor()

        mediaSession?.isActive = false
        mediaSession?.release()
        mediaSession = null

        tts?.stop()
        tts?.shutdown()
        tts = null
        ttsReady = false

        abandonSpeechAudioFocus()
        abandonGymModeAudioFocus()

        serviceScope.cancel()

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun initializeMediaSession() {
        mediaSession = MediaSession(this, MEDIA_SESSION_TAG).apply {
            setFlags(
                MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or
                        MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS,
            )

            setCallback(
                object : MediaSession.Callback() {

                    override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
                        val keyEvent = mediaButtonIntent.getParcelableExtra<KeyEvent>(
                            Intent.EXTRA_KEY_EVENT,
                        )

                        if (keyEvent == null) {
                            Log.d(REMOTE_LOG_TAG, "Media button event had no KeyEvent")
                            return super.onMediaButtonEvent(mediaButtonIntent)
                        }

                        if (
                            suppressNextSelfDispatchedPlay &&
                            keyEvent.action == KeyEvent.ACTION_DOWN &&
                            keyEvent.repeatCount == 0 &&
                            (
                                    keyEvent.keyCode == KeyEvent.KEYCODE_MEDIA_PLAY ||
                                            keyEvent.keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                                    )
                        ) {
                            suppressNextSelfDispatchedPlay = false
                            Log.d(REMOTE_LOG_TAG, "Suppressing self-dispatched media play event")
                            return true
                        }

                        val input = keyEvent.toGymRemoteInput()

                        if (input == null) {
                            logRemoteKeyEvent(keyEvent, handled = false)
                            return super.onMediaButtonEvent(mediaButtonIntent)
                        }

                        if (keyEvent.action == KeyEvent.ACTION_DOWN && keyEvent.repeatCount == 0) {
                            logRemoteKeyEvent(keyEvent, handled = true)
                            emitRemoteInput(input, "onMediaButtonEvent")
                        }

                        return true
                    }

                    override fun onPlay() {
                        if (suppressNextSelfDispatchedPlay) {
                            suppressNextSelfDispatchedPlay = false
                            Log.d(REMOTE_LOG_TAG, "Suppressing self-dispatched onPlay")
                            return
                        }

                        emitRemoteInput(GymRemoteInput.Confirm, "onPlay")
                    }

                    override fun onPause() {
                        emitRemoteInput(GymRemoteInput.Confirm, "onPause")
                    }

                    override fun onSkipToNext() {
                        emitRemoteInput(GymRemoteInput.Next, "onSkipToNext")
                    }

                    override fun onSkipToPrevious() {
                        emitRemoteInput(GymRemoteInput.Previous, "onSkipToPrevious")
                    }
                },
            )

            setSessionActivity(buildOpenAppPendingIntent())

            setPlaybackToLocal(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
            )

            setMetadata(
                MediaMetadata.Builder()
                    .putString(MediaMetadata.METADATA_KEY_TITLE, "Kusgang Aliwas Gym Mode")
                    .putString(MediaMetadata.METADATA_KEY_ARTIST, "Remote controls active")
                    .build(),
            )

            isActive = true
        }
    }

    private fun activateMediaSession() {
        mediaSession?.apply {
            setPlaybackState(buildRemoteCapturePlaybackState())
            isActive = false
            isActive = true
        }

        Log.d(REMOTE_LOG_TAG, "MediaSession activated/reasserted")
    }

    private fun pauseMediaSession() {
        mediaSession?.apply {
            setPlaybackState(
                PlaybackState.Builder()
                    .setActions(
                        PlaybackState.ACTION_PLAY or
                                PlaybackState.ACTION_PAUSE or
                                PlaybackState.ACTION_PLAY_PAUSE or
                                PlaybackState.ACTION_SKIP_TO_NEXT or
                                PlaybackState.ACTION_SKIP_TO_PREVIOUS or
                                PlaybackState.ACTION_STOP,
                    )
                    .setState(
                        PlaybackState.STATE_PAUSED,
                        PlaybackState.PLAYBACK_POSITION_UNKNOWN,
                        0f,
                        System.currentTimeMillis(),
                    )
                    .build(),
            )
            isActive = false
        }

        Log.d(REMOTE_LOG_TAG, "MediaSession paused/deactivated")
    }

    private fun buildRemoteCapturePlaybackState(): PlaybackState {
        return PlaybackState.Builder()
            .setActions(
                PlaybackState.ACTION_PLAY or
                        PlaybackState.ACTION_PAUSE or
                        PlaybackState.ACTION_PLAY_PAUSE or
                        PlaybackState.ACTION_SKIP_TO_NEXT or
                        PlaybackState.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackState.ACTION_STOP,
            )
            .setState(
                PlaybackState.STATE_PLAYING,
                PlaybackState.PLAYBACK_POSITION_UNKNOWN,
                1f,
                System.currentTimeMillis(),
            )
            .build()
    }

    private fun emitRemoteInput(input: GymRemoteInput, source: String) {
        reclaimGymRemoteControl()

        Log.d(REMOTE_LOG_TAG, "Emitting gym remote input from $source: $input")
        gymRemoteInputBus.emit(input)
    }

    private fun collectGymVoice() {
        serviceScope.launch {
            gymVoiceBus.messages.collect { message ->
                Log.d(TTS_LOG_TAG, "Speaking from service: $message")
                speakForGym(message)
            }
        }
    }

    private fun collectMediaControlCommands() {
        serviceScope.launch {
            gymRemoteMediaControlBus.commands.collect { command ->
                when (command) {
                    GymRemoteMediaControlCommand.YieldToExternalMedia -> {
                        Log.d(REMOTE_LOG_TAG, "Received YieldToExternalMedia")
                        pendingYieldToExternalMedia = true

                        if (tts?.isSpeaking != true) {
                            yieldToExternalMedia()
                        }
                    }

                    GymRemoteMediaControlCommand.ReclaimRemoteControl -> {
                        Log.d(REMOTE_LOG_TAG, "Received ReclaimRemoteControl")
                        pendingYieldToExternalMedia = false
                        reclaimGymRemoteControl()
                    }
                }
            }
        }
    }

    private fun reclaimGymRemoteControl() {
        requestGymModeAudioFocus()
        startSilentAudioAnchor()
        activateMediaSession()
    }

    private fun yieldToExternalMedia() {
        pendingYieldToExternalMedia = false

        stopSilentAudioAnchor()
        pauseMediaSession()
        abandonGymModeAudioFocus()

//        dispatchMediaPlay()

        Log.d(REMOTE_LOG_TAG, "Yielded to external media")
    }

    private fun dispatchMediaPlay() {
        val downTime = System.currentTimeMillis()

        val downEvent = KeyEvent(
            downTime,
            downTime,
            KeyEvent.ACTION_DOWN,
            KeyEvent.KEYCODE_MEDIA_PLAY,
            0,
        )

        val upEvent = KeyEvent(
            downTime,
            System.currentTimeMillis(),
            KeyEvent.ACTION_UP,
            KeyEvent.KEYCODE_MEDIA_PLAY,
            0,
        )

        suppressNextSelfDispatchedPlay = true

        audioManager.dispatchMediaKeyEvent(downEvent)
        audioManager.dispatchMediaKeyEvent(upEvent)

        Log.d(REMOTE_LOG_TAG, "Dispatched MEDIA_PLAY")
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
                            abandonSpeechAudioFocus()

                            if (pendingYieldToExternalMedia) {
                                yieldToExternalMedia()
                            } else {
                                reclaimGymRemoteControl()
                            }
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            abandonSpeechAudioFocus()

                            if (pendingYieldToExternalMedia) {
                                yieldToExternalMedia()
                            } else {
                                reclaimGymRemoteControl()
                            }
                        }
                    },
                )

                Log.d(TTS_LOG_TAG, "Service TTS ready")
            } else {
                Log.w(TTS_LOG_TAG, "Service TTS initialization failed")
            }
        }
    }

    private fun speakForGym(text: String) {
        if (!ttsReady) {
            Log.d(TTS_LOG_TAG, "Service TTS not ready. Dropping speech: $text")
            return
        }

        requestSpeechAudioFocus()

        tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "ka_gym_remote_service_${System.currentTimeMillis()}",
        )
    }

    private fun requestGymModeAudioFocus() {
        if (gymModeAudioFocusRequest == null) {
            gymModeAudioFocusRequest =
                AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build(),
                    )
                    .setAcceptsDelayedFocusGain(false)
                    .setOnAudioFocusChangeListener { focusChange ->
                        Log.d(REMOTE_LOG_TAG, "Gym mode audio focus change=$focusChange")
                    }
                    .build()
        }

        val result = gymModeAudioFocusRequest?.let {
            audioManager.requestAudioFocus(it)
        }

        Log.d(REMOTE_LOG_TAG, "Gym mode audio focus result=$result")
    }

    private fun abandonGymModeAudioFocus() {
        gymModeAudioFocusRequest?.let {
            audioManager.abandonAudioFocusRequest(it)
        }
        gymModeAudioFocusRequest = null
    }

    private fun requestSpeechAudioFocus() {
        if (speechAudioFocusRequest == null) {
            speechAudioFocusRequest =
                AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build(),
                    )
                    .setAcceptsDelayedFocusGain(false)
                    .setOnAudioFocusChangeListener { focusChange ->
                        Log.d(TTS_LOG_TAG, "Speech audio focus change=$focusChange")
                    }
                    .build()
        }

        speechAudioFocusRequest?.let {
            audioManager.requestAudioFocus(it)
        }
    }

    private fun abandonSpeechAudioFocus() {
        speechAudioFocusRequest?.let {
            audioManager.abandonAudioFocusRequest(it)
        }
        speechAudioFocusRequest = null
    }

    private fun startSilentAudioAnchor() {
        if (silentAudioRunning) return

        val sampleRate = 8_000
        val channelConfig = AudioFormat.CHANNEL_OUT_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT

        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            channelConfig,
            audioFormat,
        )

        val bufferSize = maxOf(minBufferSize, 8_000)

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setChannelMask(channelConfig)
                    .setEncoding(audioFormat)
                    .build(),
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        silentAudioTrack = track
        silentAudioRunning = true

        silentAudioThread = Thread {
            val silence = ByteArray(bufferSize)

            try {
                track.play()

                while (silentAudioRunning) {
                    track.write(silence, 0, silence.size)
                }
            } catch (error: Throwable) {
                Log.w(REMOTE_LOG_TAG, "Silent audio anchor failed", error)
            }
        }.apply {
            name = "KA Silent Gym Remote Audio Anchor"
            isDaemon = true
            start()
        }

        Log.d(REMOTE_LOG_TAG, "Silent audio anchor started")
    }

    private fun stopSilentAudioAnchor() {
        silentAudioRunning = false

        runCatching {
            silentAudioThread?.interrupt()
        }

        silentAudioThread = null

        runCatching {
            silentAudioTrack?.pause()
            silentAudioTrack?.flush()
            silentAudioTrack?.stop()
            silentAudioTrack?.release()
        }.onFailure { error ->
            Log.w(REMOTE_LOG_TAG, "Silent audio anchor cleanup failed", error)
        }

        silentAudioTrack = null

        Log.d(REMOTE_LOG_TAG, "Silent audio anchor stopped")
    }

    private fun buildNotification(): Notification {
        ensureNotificationChannel()

        val stopIntent = Intent(this, GymRemoteService::class.java)
            .setAction(ACTION_STOP)

        val stopPendingIntent = PendingIntent.getService(
            this,
            REQUEST_CODE_STOP_SERVICE,
            stopIntent,
            pendingIntentFlags(),
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Gym mode active")
            .setContentText("Remote controls are listening.")
            .setContentIntent(buildOpenAppPendingIntent())
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Stop",
                stopPendingIntent,
            )
            .build()
    }

    private fun buildOpenAppPendingIntent(): PendingIntent {
        val openAppIntent = Intent(this, MainActivity::class.java)

        return PendingIntent.getActivity(
            this,
            REQUEST_CODE_OPEN_APP,
            openAppIntent,
            pendingIntentFlags(),
        )
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager = getSystemService(NotificationManager::class.java)

        val existingChannel = notificationManager.getNotificationChannel(
            NOTIFICATION_CHANNEL_ID,
        )

        if (existingChannel != null) return

        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Keeps gym remote controls active during pocket mode."
        }

        notificationManager.createNotificationChannel(channel)
    }

    private fun pendingIntentFlags(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    }

    private fun logRemoteKeyEvent(event: KeyEvent, handled: Boolean) {
        Log.d(
            REMOTE_LOG_TAG,
            "serviceHandled=$handled, " +
                    "keyCode=${event.keyCode}, " +
                    "keyName=${KeyEvent.keyCodeToString(event.keyCode)}, " +
                    "scanCode=${event.scanCode}, " +
                    "deviceId=${event.deviceId}, " +
                    "repeatCount=${event.repeatCount}",
        )
    }

    companion object {
        private const val REMOTE_LOG_TAG = "KA_REMOTE_SERVICE"
        private const val TTS_LOG_TAG = "KA_TTS_SERVICE"

        private const val MEDIA_SESSION_TAG = "KA_GYM_REMOTE_SESSION"

        private const val NOTIFICATION_CHANNEL_ID = "ka_gym_remote"
        private const val NOTIFICATION_CHANNEL_NAME = "Gym Remote"

        private const val NOTIFICATION_ID = 3101

        private const val REQUEST_CODE_OPEN_APP = 3102
        private const val REQUEST_CODE_STOP_SERVICE = 3103

        private const val ACTION_START = "com.example.kusgangaliwas.action.START_GYM_REMOTE"
        private const val ACTION_STOP = "com.example.kusgangaliwas.action.STOP_GYM_REMOTE"

        fun startIntent(context: Context): Intent {
            return Intent(context, GymRemoteService::class.java)
                .setAction(ACTION_START)
        }

        fun stopIntent(context: Context): Intent {
            return Intent(context, GymRemoteService::class.java)
                .setAction(ACTION_STOP)
        }
    }
}