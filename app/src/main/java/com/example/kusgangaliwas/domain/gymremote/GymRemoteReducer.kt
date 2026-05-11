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
        val result = when (input) {
            GymRemoteInput.Confirm -> onConfirm(state)
            GymRemoteInput.Next -> onNext(state)
            GymRemoteInput.Previous -> onPrevious(state)
            GymRemoteInput.Increment -> onIncrement(state)
            GymRemoteInput.Decrement -> onDecrement(state)
        }

        return result.copy(
            effects = buildList {
                add(
                    GymRemoteEffect.DebugLog(
                        buildString {
                            append("INPUT=")
                            append(input)
                            append(" | BEFORE=")
                            append(state.debugLabel())
                            append(" | AFTER=")
                            append(result.nextState.debugLabel())
                        }
                    )
                )

                addAll(result.effects)
            }
        )
    }

    private fun onConfirm(
        state: GymRemoteState,
    ): GymRemoteResult {
        return when (val focus = state.focus) {
            GymRemoteFocus.None -> {
                GymRemoteResult(
                    nextState = state,
                    effects = listOf(
                        GymRemoteEffect.Speak("No set selected."),
                    ),
                )
            }

            is GymRemoteFocus.Weight -> {
                completeFocusedSet(
                    state = state,
                    setIndex = focus.setIndex,
                )
            }

            is GymRemoteFocus.Reps -> {
                completeFocusedSet(
                    state = state,
                    setIndex = focus.setIndex,
                )
            }
        }
    }

    private fun completeFocusedSet(
        state: GymRemoteState,
        setIndex: Int,
    ): GymRemoteResult {
        val currentSet = state.sets.getOrNull(setIndex)
            ?: return GymRemoteResult(nextState = state)

        val newSetIndex = state.sets.size

        val duplicatedSet = currentSet.copy(
            setIndex = newSetIndex,
        )

        val nextSets = state.sets + duplicatedSet

        val nextState = state.copy(
            focus = GymRemoteFocus.Weight(newSetIndex),
            sets = nextSets,
        )

        return GymRemoteResult(
            nextState = nextState,
            effects = listOf(
                GymRemoteEffect.DuplicateSet(setIndex),
                GymRemoteEffect.Speak("Set ${setIndex + 1} complete."),
                GymRemoteEffect.Speak("New set created as set ${newSetIndex + 1}."),
                GymRemoteEffect.Speak(
                    "Set ${newSetIndex + 1}, ${formatWeightForSpeech(duplicatedSet.weight)} pounds."
                ),
            ),
        )
    }

    private fun onNext(
        state: GymRemoteState,
    ): GymRemoteResult {
        val nextFocus = when (val focus = state.focus) {
            GymRemoteFocus.None -> {
                if (state.sets.isEmpty()) {
                    GymRemoteFocus.None
                } else {
                    GymRemoteFocus.Weight(0)
                }
            }

            is GymRemoteFocus.Weight -> {
                GymRemoteFocus.Reps(focus.setIndex)
            }

            is GymRemoteFocus.Reps -> {
                val nextIndex = focus.setIndex + 1

                if (nextIndex >= state.sets.size) {
                    GymRemoteFocus.None
                } else {
                    GymRemoteFocus.Weight(nextIndex)
                }
            }
        }

        return GymRemoteResult(
            nextState = state.copy(
                focus = nextFocus,
            ),
            effects = listOf(
                GymRemoteEffect.AnnounceFocus(nextFocus),
            ),
        )
    }

    private fun onPrevious(
        state: GymRemoteState,
    ): GymRemoteResult {
        val previousFocus = when (val focus = state.focus) {
            GymRemoteFocus.None -> {
                if (state.sets.isEmpty()) {
                    GymRemoteFocus.None
                } else {
                    GymRemoteFocus.Reps(state.sets.lastIndex)
                }
            }

            is GymRemoteFocus.Weight -> {
                if (focus.setIndex == 0) {
                    GymRemoteFocus.None
                } else {
                    GymRemoteFocus.Reps(focus.setIndex - 1)
                }
            }

            is GymRemoteFocus.Reps -> {
                GymRemoteFocus.Weight(focus.setIndex)
            }
        }

        return GymRemoteResult(
            nextState = state.copy(
                focus = previousFocus,
            ),
            effects = listOf(
                GymRemoteEffect.AnnounceFocus(previousFocus),
            ),
        )
    }

    private fun onIncrement(
        state: GymRemoteState,
    ): GymRemoteResult {
        return when (val focus = state.focus) {
            GymRemoteFocus.None -> {
                GymRemoteResult(nextState = state)
            }

            is GymRemoteFocus.Weight -> {
                val updatedSets = state.sets.toMutableList()
                val current = updatedSets[focus.setIndex]

                val newWeight =
                    ((current.weight ?: 0.0) + WEIGHT_STEP)
                        .coerceAtLeast(0.0)

                updatedSets[focus.setIndex] =
                    current.copy(weight = newWeight)

                GymRemoteResult(
                    nextState = state.copy(
                        sets = updatedSets,
                    ),
                    effects = listOf(
                        GymRemoteEffect.UpdateWeight(
                            setIndex = focus.setIndex,
                            weight = newWeight,
                        ),
                        GymRemoteEffect.Speak(
                            "${formatNumber(newWeight)} pounds"
                        ),
                    ),
                )
            }

            is GymRemoteFocus.Reps -> {
                val updatedSets = state.sets.toMutableList()
                val current = updatedSets[focus.setIndex]

                val newReps =
                    ((current.reps ?: 0) + 1)
                        .coerceAtLeast(0)

                updatedSets[focus.setIndex] =
                    current.copy(reps = newReps)

                GymRemoteResult(
                    nextState = state.copy(
                        sets = updatedSets,
                    ),
                    effects = listOf(
                        GymRemoteEffect.UpdateReps(
                            setIndex = focus.setIndex,
                            reps = newReps,
                        ),
                        GymRemoteEffect.Speak("$newReps reps"),
                    ),
                )
            }
        }
    }

    private fun onDecrement(
        state: GymRemoteState,
    ): GymRemoteResult {
        return when (val focus = state.focus) {
            GymRemoteFocus.None -> {
                GymRemoteResult(nextState = state)
            }

            is GymRemoteFocus.Weight -> {
                val updatedSets = state.sets.toMutableList()
                val current = updatedSets[focus.setIndex]

                val newWeight =
                    ((current.weight ?: 0.0) - WEIGHT_STEP)
                        .coerceAtLeast(0.0)

                updatedSets[focus.setIndex] =
                    current.copy(weight = newWeight)

                GymRemoteResult(
                    nextState = state.copy(
                        sets = updatedSets,
                    ),
                    effects = listOf(
                        GymRemoteEffect.UpdateWeight(
                            setIndex = focus.setIndex,
                            weight = newWeight,
                        ),
                        GymRemoteEffect.Speak(
                            "${formatNumber(newWeight)} pounds"
                        ),
                    ),
                )
            }

            is GymRemoteFocus.Reps -> {
                val updatedSets = state.sets.toMutableList()
                val current = updatedSets[focus.setIndex]

                val newReps =
                    ((current.reps ?: 0) - 1)
                        .coerceAtLeast(0)

                updatedSets[focus.setIndex] =
                    current.copy(reps = newReps)

                GymRemoteResult(
                    nextState = state.copy(
                        sets = updatedSets,
                    ),
                    effects = listOf(
                        GymRemoteEffect.UpdateReps(
                            setIndex = focus.setIndex,
                            reps = newReps,
                        ),
                        GymRemoteEffect.Speak("$newReps reps"),
                    ),
                )
            }
        }
    }

    private fun formatWeightForSpeech(
        value: Double?,
    ): String {
        return value?.let(::formatNumber) ?: "no"
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