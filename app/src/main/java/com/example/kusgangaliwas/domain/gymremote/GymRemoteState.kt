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
    val exerciseName: String = "",
    val focus: GymRemoteFocus = GymRemoteFocus.None,
    val sets: List<GymRemoteSetState> = emptyList(),
)

/**
 * Focus target for the remote.
 *
 * V1 intentionally skips a set-summary focus.
 *
 * Traversal:
 * None <-> Set 1 weight <-> Set 1 reps <-> Set 2 weight <-> Set 2 reps <-> None
 */
sealed interface GymRemoteFocus {

    data object None : GymRemoteFocus

    data class Weight(
        val setIndex: Int,
    ) : GymRemoteFocus

    data class Reps(
        val setIndex: Int,
    ) : GymRemoteFocus
}

/**
 * Lightweight set state used by the pure remote reducer.
 *
 * setIndex is zero-based.
 */
data class GymRemoteSetState(
    val setIndex: Int,
    val weight: Double? = null,
    val reps: Int? = null,
)

/**
 * Debug-friendly state text for Logcat tracing.
 */
fun GymRemoteState.debugLabel(): String {
    val focusText = when (focus) {
        GymRemoteFocus.None -> "None"
        is GymRemoteFocus.Weight -> "Set ${focus.setIndex + 1} Weight"
        is GymRemoteFocus.Reps -> "Set ${focus.setIndex + 1} Reps"
    }

    val setsText = sets.joinToString(
        separator = ", ",
        prefix = "[",
        postfix = "]",
    ) { set ->
        "set=${set.setIndex + 1}, weight=${set.weight}, reps=${set.reps}"
    }

    return "exercise=$exerciseName, focus=$focusText, sets=$setsText"
}