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
        val weight: Double,
    ) : GymRemoteEffect

    /**
     * Persist updated reps value.
     */
    data class UpdateReps(
        val reps: Int,
    ) : GymRemoteEffect

    /**
     * Mark current set as started/performed.
     */
    data object StartSet : GymRemoteEffect

    /**
     * Mark current set as completed.
     */
    data object CompleteSet : GymRemoteEffect

    /**
     * Automatically prepare the next set.
     */
    data object PrepareNextSet : GymRemoteEffect
}