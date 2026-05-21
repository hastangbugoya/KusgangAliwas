package com.example.kusgangaliwas.domain.gymremote

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * App-internal bridge for Pocket Gym Mode media-control handoff commands.
 *
 * ViewModels should not know about:
 * - Android MediaSession
 * - AudioManager
 * - service lifecycle
 * - media-button dispatch
 *
 * Instead, ViewModels emit plain media handoff commands here.
 * GymRemoteService collects them and performs the platform-specific work.
 *
 * This mirrors the same bus style used by:
 * - GymVoiceBus
 * - GymRemoteInputBus
 *
 * See:
 * app/src/main/java/com/example/kusgangaliwas/KA_Pocket_Gym_Remote_Mode_Technical_Design.md
 */
@Singleton
class GymRemoteMediaControlBus @Inject constructor() {

    private val _commands = MutableSharedFlow<GymRemoteMediaControlCommand>(
        extraBufferCapacity = 16,
    )

    val commands: SharedFlow<GymRemoteMediaControlCommand> = _commands

    fun emit(command: GymRemoteMediaControlCommand) {
        _commands.tryEmit(command)
    }
}

/**
 * Platform-free media-control command emitted by the remote session logic.
 */
sealed interface GymRemoteMediaControlCommand {

    /**
     * KA should temporarily yield Bluetooth/media-button control so an external
     * media player can resume playback while the user performs a set.
     */
    data object YieldToExternalMedia : GymRemoteMediaControlCommand

    /**
     * KA should attempt to reclaim media-button handling.
     *
     * This is reserved for later. For now, KA usually regains control when the
     * user pauses external media from the remote.
     */
    data object ReclaimRemoteControl : GymRemoteMediaControlCommand
}