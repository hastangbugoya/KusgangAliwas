package com.example.kusgangaliwas.domain.gymremote

/**
 * Pure state for the gym remote control flow.
 *
 * This state is intentionally independent from:
 * - Android KeyEvent
 * - TextToSpeech
 * - Room entities
 * - Compose UI
 *
 * It should be safe to unit test with simple:
 * state + input -> next state + effects.
 */
data class GymRemoteState(
    val phase: GymRemotePhase = GymRemotePhase.IDLE,
    val focusedField: GymRemoteFocusedField = GymRemoteFocusedField.WEIGHT,
    val exerciseName: String = "",
    val setNumber: Int = 1,
    val weight: Double? = null,
    val reps: Int? = null,
)

/**
 * High-level phase of the hands-free gym flow.
 */
enum class GymRemotePhase {
    IDLE,
    REVIEWING_SET,
    EDITING,
    PERFORMING_SET,
}

/**
 * Which value +/- should currently adjust.
 */
enum class GymRemoteFocusedField {
    WEIGHT,
    REPS,
}