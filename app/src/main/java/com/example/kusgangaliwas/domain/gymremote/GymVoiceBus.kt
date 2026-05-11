package com.example.kusgangaliwas.domain.gymremote

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Foreground-only bridge for gym voice/TTS output.
 *
 * ViewModels emit plain speech text.
 * MainActivity collects the text and performs Android TextToSpeech.
 *
 * This keeps:
 * - ViewModels independent from Android TextToSpeech
 * - MainActivity responsible for platform audio/TTS
 * - future GymRemoteService migration easier
 */
@Singleton
class GymVoiceBus @Inject constructor() {

    private val _messages = MutableSharedFlow<String>(
        extraBufferCapacity = 16,
    )

    val messages: SharedFlow<String> = _messages

    fun speak(text: String) {
        _messages.tryEmit(text)
    }
}