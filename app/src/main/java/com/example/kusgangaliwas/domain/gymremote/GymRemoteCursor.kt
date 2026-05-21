package com.example.kusgangaliwas.domain.gymremote

/**
 * V2 remote navigation tree/cursor model.
 *
 * This replaces the older:
 * - GymRemoteFocus
 * reducer-style linear traversal model.
 *
 * IMPORTANT:
 * This cursor is intentionally:
 * - UI independent
 * - Room independent
 * - Compose independent
 * - Android independent
 *
 * It represents ONLY:
 * - where the remote currently is,
 * - what the user is navigating,
 * - what actions are currently valid.
 *
 * The screen UI is expected to OBSERVE this state,
 * not own it.
 */
sealed interface GymRemoteCursor {

    /**
     * Initial/root navigation state.
     *
     * Example speech:
     * "Press play to go to exercises."
     */
    data object SessionRoot : GymRemoteCursor

    /**
     * Traversing exercises in the session.
     *
     * selectedExerciseLogId:
     * - actual exercise log currently highlighted by remote.
     */
    data class ExerciseList(
        val selectedExerciseLogId: Long,
    ) : GymRemoteCursor

    /**
     * Exercise selected but not yet editing fields.
     *
     * User traverses sets here.
     */
    data class SetList(
        val exerciseLogId: Long,
        val selectedSetLogId: Long?,
    ) : GymRemoteCursor

    /**
     * Editing a specific field in a specific set.
     */
    data class SetField(
        val exerciseLogId: Long,
        val setLogId: Long,
        val field: GymRemoteField,
    ) : GymRemoteCursor

    /**
     * Traversed beyond the final set.
     *
     * Example speech:
     * "Press play to add next set."
     */
    data class AddSetPrompt(
        val exerciseLogId: Long,
    ) : GymRemoteCursor

    /**
     * KA intentionally yields media control
     * back to external music playback.
     *
     * Example:
     * - user starts set
     * - music resumes
     * - KA waits for media pause to regain control.
     */
    data class MusicYield(
        val exerciseLogId: Long,
        val setLogId: Long,
    ) : GymRemoteCursor
}

/**
 * Field currently targeted by remote editing.
 */
enum class GymRemoteField {
    WEIGHT,
    REPS,
    DURATION,
    DISTANCE,
    REST_TIMER,
}

/**
 * Helpful debug label for Logcat.
 */
fun GymRemoteCursor.debugLabel(): String {
    return when (this) {
        GymRemoteCursor.SessionRoot -> {
            "SessionRoot"
        }

        is GymRemoteCursor.ExerciseList -> {
            "ExerciseList(exerciseLogId=$selectedExerciseLogId)"
        }

        is GymRemoteCursor.SetList -> {
            "SetList(exerciseLogId=$exerciseLogId, setLogId=$selectedSetLogId)"
        }

        is GymRemoteCursor.SetField -> {
            "SetField(exerciseLogId=$exerciseLogId, setLogId=$setLogId, field=$field)"
        }

        is GymRemoteCursor.AddSetPrompt -> {
            "AddSetPrompt(exerciseLogId=$exerciseLogId)"
        }

        is GymRemoteCursor.MusicYield -> {
            "MusicYield(exerciseLogId=$exerciseLogId, setLogId=$setLogId)"
        }
    }
}