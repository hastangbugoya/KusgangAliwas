package com.example.kusgangaliwas.domain.gymremote

/**
 * Pure reducer for the hands-free gym remote flow.
 *
 * This class owns no Android, Room, TTS, or Compose dependencies.
 *
 * Test pattern:
 * state + input -> result(nextState, effects)
 */
class GymRemoteReducer {

    fun reduce(
        state: GymRemoteState,
        input: GymRemoteInput,
    ): GymRemoteResult {
        return when (input) {
            GymRemoteInput.Confirm -> onConfirm(state)
            GymRemoteInput.Next -> onNext(state)
            GymRemoteInput.Previous -> onPrevious(state)
            GymRemoteInput.Increment -> onIncrement(state)
            GymRemoteInput.Decrement -> onDecrement(state)
        }
    }

    private fun onConfirm(
        state: GymRemoteState,
    ): GymRemoteResult {
        return when (state.phase) {
            GymRemotePhase.IDLE -> {
                GymRemoteResult(
                    nextState = state.copy(
                        phase = GymRemotePhase.REVIEWING_SET,
                    ),
                    effects = listOf(
                        GymRemoteEffect.Speak(state.describeCurrentSet()),
                        GymRemoteEffect.Speak("Adjust weight. Next for reps."),
                    ),
                )
            }

            GymRemotePhase.REVIEWING_SET,
            GymRemotePhase.EDITING -> {
                GymRemoteResult(
                    nextState = state.copy(
                        phase = GymRemotePhase.PERFORMING_SET,
                    ),
                    effects = listOf(
                        GymRemoteEffect.StartSet,
                        GymRemoteEffect.Speak("Start set ${state.setNumber}."),
                    ),
                )
            }

            GymRemotePhase.PERFORMING_SET -> {
                GymRemoteResult(
                    nextState = state.copy(
                        phase = GymRemotePhase.REVIEWING_SET,
                        setNumber = state.setNumber + 1,
                    ),
                    effects = listOf(
                        GymRemoteEffect.CompleteSet,
                        GymRemoteEffect.PrepareNextSet,
                        GymRemoteEffect.Speak("Set ${state.setNumber} complete."),
                    ),
                )
            }
        }
    }

    private fun onNext(
        state: GymRemoteState,
    ): GymRemoteResult {
        val nextState = state.copy(
            phase = GymRemotePhase.EDITING,
            focusedField = GymRemoteFocusedField.REPS,
        )

        return GymRemoteResult(
            nextState = nextState,
            effects = listOf(
                GymRemoteEffect.Speak("Adjust reps."),
            ),
        )
    }

    private fun onPrevious(
        state: GymRemoteState,
    ): GymRemoteResult {
        val nextState = state.copy(
            phase = GymRemotePhase.EDITING,
            focusedField = GymRemoteFocusedField.WEIGHT,
        )

        return GymRemoteResult(
            nextState = nextState,
            effects = listOf(
                GymRemoteEffect.Speak("Adjust weight."),
            ),
        )
    }

    private fun onIncrement(
        state: GymRemoteState,
    ): GymRemoteResult {
        return when (state.focusedField) {
            GymRemoteFocusedField.WEIGHT -> {
                val newWeight = ((state.weight ?: 0.0) + WEIGHT_STEP)
                    .coerceAtLeast(0.0)

                GymRemoteResult(
                    nextState = state.copy(
                        phase = GymRemotePhase.EDITING,
                        weight = newWeight,
                    ),
                    effects = listOf(
                        GymRemoteEffect.UpdateWeight(newWeight),
                        GymRemoteEffect.Speak("${formatNumber(newWeight)} pounds"),
                    ),
                )
            }

            GymRemoteFocusedField.REPS -> {
                val newReps = ((state.reps ?: 0) + 1)
                    .coerceAtLeast(0)

                GymRemoteResult(
                    nextState = state.copy(
                        phase = GymRemotePhase.EDITING,
                        reps = newReps,
                    ),
                    effects = listOf(
                        GymRemoteEffect.UpdateReps(newReps),
                        GymRemoteEffect.Speak("$newReps reps"),
                    ),
                )
            }
        }
    }

    private fun onDecrement(
        state: GymRemoteState,
    ): GymRemoteResult {
        return when (state.focusedField) {
            GymRemoteFocusedField.WEIGHT -> {
                val newWeight = ((state.weight ?: 0.0) - WEIGHT_STEP)
                    .coerceAtLeast(0.0)

                GymRemoteResult(
                    nextState = state.copy(
                        phase = GymRemotePhase.EDITING,
                        weight = newWeight,
                    ),
                    effects = listOf(
                        GymRemoteEffect.UpdateWeight(newWeight),
                        GymRemoteEffect.Speak("${formatNumber(newWeight)} pounds"),
                    ),
                )
            }

            GymRemoteFocusedField.REPS -> {
                val newReps = ((state.reps ?: 0) - 1)
                    .coerceAtLeast(0)

                GymRemoteResult(
                    nextState = state.copy(
                        phase = GymRemotePhase.EDITING,
                        reps = newReps,
                    ),
                    effects = listOf(
                        GymRemoteEffect.UpdateReps(newReps),
                        GymRemoteEffect.Speak("$newReps reps"),
                    ),
                )
            }
        }
    }

    private fun GymRemoteState.describeCurrentSet(): String {
        val weightText = weight?.let {
            "${formatNumber(it)} pounds"
        } ?: "no weight"

        val repsText = reps?.let {
            "$it reps"
        } ?: "no reps"

        return "${exerciseName.ifBlank { "Exercise" }}, set $setNumber, $weightText by $repsText."
    }

    private fun formatNumber(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            value.toString()
        }
    }

    private companion object {
        const val WEIGHT_STEP = 2.5
    }
}

/**
 * Result of applying a remote input to a gym remote state.
 */
data class GymRemoteResult(
    val nextState: GymRemoteState,
    val effects: List<GymRemoteEffect> = emptyList(),
)