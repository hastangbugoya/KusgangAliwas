package com.example.kusgangaliwas.domain.gymremote

/**
 * Side-effect descriptions produced by the gym remote reducer.
 *
 * IMPORTANT:
 * These are NOT the side effects themselves.
 *
 * They are descriptions of work that the outer system should perform:
 * - TTS speech
 * - DB updates
 * - set duplication
 * - session navigation
 *
 * This keeps the reducer pure and easily unit-testable.
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
     */
    data class UpdateWeight(
        val setIndex: Int,
        val weight: Double,
    ) : GymRemoteEffect

    /**
     * Persist updated reps value.
     */
    data class UpdateReps(
        val setIndex: Int,
        val reps: Int,
    ) : GymRemoteEffect

    /**
     * Duplicate the currently focused set and append it as the next set.
     */
    data class DuplicateSet(
        val sourceSetIndex: Int,
    ) : GymRemoteEffect

    /**
     * Read current focus/state aloud.
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