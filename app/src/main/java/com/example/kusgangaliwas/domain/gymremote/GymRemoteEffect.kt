package com.example.kusgangaliwas.domain.gymremote

/**
 * Side-effect descriptions produced by gym remote reducers.
 *
 * IMPORTANT:
 * These are NOT the side effects themselves.
 *
 * They are descriptions of work that the outer system should perform:
 * - TTS speech
 * - DB updates
 * - set creation/duplication
 * - remote focus/highlight updates
 * - media-session handoff behavior
 *
 * This keeps reducers pure and easily unit-testable.
 */
sealed interface GymRemoteEffect {

    /**
     * Speak text to the user through TTS.
     */
    data class Speak(
        val text: String,
    ) : GymRemoteEffect

    /**
     * Persist updated weight value.
     *
     * V1 reducer effect.
     * The set is identified by sorted set index within the active exercise.
     */
    data class UpdateWeight(
        val setIndex: Int,
        val weight: Double,
    ) : GymRemoteEffect

    /**
     * Persist updated reps value.
     *
     * V1 reducer effect.
     * The set is identified by sorted set index within the active exercise.
     */
    data class UpdateReps(
        val setIndex: Int,
        val reps: Int,
    ) : GymRemoteEffect

    /**
     * Duplicate the currently focused set and append it as the next set.
     *
     * V1 reducer effect.
     * The set is identified by sorted set index within the active exercise.
     */
    data class DuplicateSet(
        val sourceSetIndex: Int,
    ) : GymRemoteEffect

    /**
     * Create a new set for the given exercise log.
     *
     * V2 tree reducer effect.
     *
     * Used by:
     * - empty exercise prompt
     * - end-of-sets add prompt
     */
    data class AddSetToExercise(
        val exerciseLogId: Long,
    ) : GymRemoteEffect

    /**
     * Update the UI-visible remote-selected exercise.
     *
     * This is not Compose focus and not text-field focus.
     * It is the highlighted exercise/card that corresponds to the remote cursor.
     *
     * Use null to clear the visible remote selection.
     */
    data class SelectRemoteExercise(
        val exerciseLogId: Long?,
    ) : GymRemoteEffect

    /**
     * Yield Bluetooth/media-button ownership back toward the OS or external
     * media player after KA starts a set.
     *
     * Expected outer behavior:
     * - say the start-set prompt first if needed,
     * - deactivate/release KA media capture,
     * - optionally send a media play command later if the service supports it.
     *
     * The reducer emits this when the user presses confirm while positioned on
     * an editable set field such as weight or reps.
     */
    data class YieldMediaControl(
        val exerciseLogId: Long,
        val setLogId: Long,
    ) : GymRemoteEffect

    /**
     * Read current V1 focus/state aloud.
     */
    data class AnnounceFocus(
        val focus: GymRemoteFocus,
    ) : GymRemoteEffect

    /**
     * Debug/logging effect for tracing reducer transitions.
     */
    data class DebugLog(
        val message: String,
    ) : GymRemoteEffect
}