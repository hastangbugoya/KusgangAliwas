package com.example.kusgangaliwas.domain.gymremote

/**
 * V2 tree-based reducer for Pocket Gym Mode.
 *
 * This reducer replaces the earlier linear:
 *
 * None
 * -> Set Weight
 * -> Set Reps
 *
 * traversal model with a hierarchical session-navigation model:
 *
 * Session
 * -> Exercises
 * -> Sets
 * -> Fields
 * -> Add-set prompt
 * -> Music-yield state
 *
 * IMPORTANT:
 * This reducer intentionally owns NO:
 * - Android
 * - Room
 * - Compose
 * - TextToSpeech
 * - ViewModel
 * dependencies.
 *
 * Future developers and AI assistants should read:
 *
 * app/src/main/java/com/example/kusgangaliwas/KA_Pocket_Gym_Remote_Mode_Technical_Design.md
 *
 * before changing traversal behavior.
 *
 * The spoken flow IS the UI while the phone is in-pocket.
 * Small traversal changes materially affect usability.
 */
class GymRemoteTreeReducer {

    fun reduce(
        tree: GymRemoteSessionTree,
        cursor: GymRemoteCursor,
        input: GymRemoteInput,
    ): GymRemoteTreeResult {

        val reconciledCursor = reconcileCursor(
            tree = tree,
            cursor = cursor,
        )

        val result = when (input) {
            GymRemoteInput.Confirm -> {
                onConfirm(
                    tree = tree,
                    cursor = reconciledCursor,
                )
            }

            GymRemoteInput.Next -> {
                onNext(
                    tree = tree,
                    cursor = reconciledCursor,
                )
            }

            GymRemoteInput.Previous -> {
                onPrevious(
                    tree = tree,
                    cursor = reconciledCursor,
                )
            }

            GymRemoteInput.Increment -> {
                GymRemoteTreeResult(
                    nextCursor = reconciledCursor,
                )
            }

            GymRemoteInput.Decrement -> {
                GymRemoteTreeResult(
                    nextCursor = reconciledCursor,
                )
            }
        }

        return result.copy(
            effects = buildList {
                add(
                    GymRemoteEffect.DebugLog(
                        buildString {
                            append("TREE INPUT=")
                            append(input)
                            append(" | BEFORE=")
                            append(reconciledCursor.debugLabel())
                            append(" | AFTER=")
                            append(result.nextCursor.debugLabel())
                        }
                    )
                )

                addAll(result.effects)
            }
        )
    }

    /**
     * Reconcile cursor against latest session tree.
     *
     * This protects Pocket Gym Mode from:
     * - exercise deletion,
     * - set deletion,
     * - reordered sessions,
     * - stale remote selections.
     *
     * IMPORTANT:
     * The remote cursor must NEVER point at deleted entities.
     */
    private fun reconcileCursor(
        tree: GymRemoteSessionTree,
        cursor: GymRemoteCursor,
    ): GymRemoteCursor {

        return when (cursor) {

            GymRemoteCursor.SessionRoot -> {
                cursor
            }

            is GymRemoteCursor.ExerciseList -> {
                val existingExercise = tree.exerciseById(
                    cursor.selectedExerciseLogId
                )

                if (existingExercise != null) {
                    cursor
                } else {
                    tree.firstExerciseOrNull()?.let {
                        GymRemoteCursor.ExerciseList(
                            selectedExerciseLogId = it.exerciseLogId,
                        )
                    } ?: GymRemoteCursor.SessionRoot
                }
            }

            is GymRemoteCursor.SetList -> {
                val exercise = tree.exerciseById(
                    cursor.exerciseLogId
                ) ?: return tree.firstExerciseOrNull()?.let {
                    GymRemoteCursor.ExerciseList(
                        selectedExerciseLogId = it.exerciseLogId,
                    )
                } ?: GymRemoteCursor.SessionRoot

                if (exercise.sets.isEmpty()) {
                    return GymRemoteCursor.AddSetPrompt(
                        exerciseLogId = exercise.exerciseLogId,
                    )
                }

                val setStillExists = cursor.selectedSetLogId?.let {
                    exercise.setById(it)
                }

                if (setStillExists != null) {
                    cursor
                } else {
                    val firstSet = exercise.firstSetOrNull()

                    GymRemoteCursor.SetList(
                        exerciseLogId = exercise.exerciseLogId,
                        selectedSetLogId = firstSet?.setLogId,
                    )
                }
            }

            is GymRemoteCursor.SetField -> {
                val exercise = tree.exerciseById(
                    cursor.exerciseLogId
                ) ?: return tree.firstExerciseOrNull()?.let {
                    GymRemoteCursor.ExerciseList(
                        selectedExerciseLogId = it.exerciseLogId,
                    )
                } ?: GymRemoteCursor.SessionRoot

                val set = exercise.setById(
                    cursor.setLogId
                )

                if (set != null) {
                    cursor
                } else if (exercise.sets.isEmpty()) {
                    GymRemoteCursor.AddSetPrompt(
                        exerciseLogId = exercise.exerciseLogId,
                    )
                } else {
                    val firstSet = exercise.firstSetOrNull()

                    GymRemoteCursor.SetList(
                        exerciseLogId = exercise.exerciseLogId,
                        selectedSetLogId = firstSet?.setLogId,
                    )
                }
            }

            is GymRemoteCursor.AddSetPrompt -> {
                val exercise = tree.exerciseById(
                    cursor.exerciseLogId
                )

                if (exercise != null) {
                    cursor
                } else {
                    tree.firstExerciseOrNull()?.let {
                        GymRemoteCursor.ExerciseList(
                            selectedExerciseLogId = it.exerciseLogId,
                        )
                    } ?: GymRemoteCursor.SessionRoot
                }
            }

            is GymRemoteCursor.MusicYield -> {
                val exercise = tree.exerciseById(
                    cursor.exerciseLogId
                ) ?: return tree.firstExerciseOrNull()?.let {
                    GymRemoteCursor.ExerciseList(
                        selectedExerciseLogId = it.exerciseLogId,
                    )
                } ?: GymRemoteCursor.SessionRoot

                val set = exercise.setById(
                    cursor.setLogId
                )

                if (set != null) {
                    cursor
                } else {
                    val firstSet = exercise.firstSetOrNull()

                    if (firstSet == null) {
                        GymRemoteCursor.AddSetPrompt(
                            exerciseLogId = exercise.exerciseLogId,
                        )
                    } else {
                        GymRemoteCursor.SetList(
                            exerciseLogId = exercise.exerciseLogId,
                            selectedSetLogId = firstSet.setLogId,
                        )
                    }
                }
            }
        }
    }

    private fun onConfirm(
        tree: GymRemoteSessionTree,
        cursor: GymRemoteCursor,
    ): GymRemoteTreeResult {

        return when (cursor) {

            GymRemoteCursor.SessionRoot -> {
                val firstExercise = tree.firstExerciseOrNull()

                if (firstExercise == null) {
                    GymRemoteTreeResult(
                        nextCursor = cursor,
                        effects = listOf(
                            GymRemoteEffect.Speak(
                                "No exercises in session."
                            )
                        )
                    )
                } else {
                    GymRemoteTreeResult(
                        nextCursor = GymRemoteCursor.ExerciseList(
                            selectedExerciseLogId = firstExercise.exerciseLogId,
                        ),
                        effects = listOf(
                            GymRemoteEffect.SelectRemoteExercise(
                                firstExercise.exerciseLogId,
                            ),
                            GymRemoteEffect.Speak(
                                firstExercise.exerciseSpeech()
                            )
                        )
                    )
                }
            }

            is GymRemoteCursor.ExerciseList -> {
                val exercise = tree.exerciseById(
                    cursor.selectedExerciseLogId
                ) ?: return GymRemoteTreeResult(
                    nextCursor = GymRemoteCursor.SessionRoot,
                )

                if (exercise.sets.isEmpty()) {
                    GymRemoteTreeResult(
                        nextCursor = GymRemoteCursor.AddSetPrompt(
                            exerciseLogId = exercise.exerciseLogId,
                        ),
                        effects = listOf(
                            GymRemoteEffect.SelectRemoteExercise(
                                exercise.exerciseLogId,
                            ),
                            GymRemoteEffect.Speak(
                                exercise.noSetsSpeech()
                            )
                        )
                    )
                } else {
                    val firstSet = exercise.firstSetOrNull()

                    GymRemoteTreeResult(
                        nextCursor = GymRemoteCursor.SetList(
                            exerciseLogId = exercise.exerciseLogId,
                            selectedSetLogId = firstSet?.setLogId,
                        ),
                        effects = buildList {
                            add(
                                GymRemoteEffect.SelectRemoteExercise(
                                    exercise.exerciseLogId,
                                )
                            )

                            firstSet?.let {
                                add(
                                    GymRemoteEffect.Speak(
                                        it.setSpeech()
                                    )
                                )
                            }
                        }
                    )
                }
            }

            is GymRemoteCursor.SetList -> {
                val exercise = tree.exerciseById(
                    cursor.exerciseLogId
                )

                val set = cursor.selectedSetLogId
                    ?.let { setId ->
                        exercise?.setById(setId)
                    }

                if (exercise == null || set == null) {
                    GymRemoteTreeResult(
                        nextCursor = GymRemoteCursor.ExerciseList(
                            selectedExerciseLogId = cursor.exerciseLogId,
                        ),
                    )
                } else {
                    GymRemoteTreeResult(
                        nextCursor = GymRemoteCursor.SetField(
                            exerciseLogId = exercise.exerciseLogId,
                            setLogId = set.setLogId,
                            field = GymRemoteField.WEIGHT,
                        ),
                        effects = listOf(
                            GymRemoteEffect.SelectRemoteExercise(
                                exercise.exerciseLogId,
                            ),
                            GymRemoteEffect.Speak(
                                set.weightSpeech()
                            )
                        )
                    )
                }
            }

            is GymRemoteCursor.AddSetPrompt -> {
                GymRemoteTreeResult(
                    nextCursor = cursor,
                    effects = listOf(
                        GymRemoteEffect.SelectRemoteExercise(
                            cursor.exerciseLogId,
                        ),
                        GymRemoteEffect.AddSetToExercise(
                            exerciseLogId = cursor.exerciseLogId,
                        ),
                        GymRemoteEffect.Speak(
                            "Adding new set."
                        )
                    )
                )
            }

            is GymRemoteCursor.SetField -> {
                GymRemoteTreeResult(
                    nextCursor = cursor,
                    effects = listOf(
                        GymRemoteEffect.Speak("Start set.")
                    )
                )
            }

            is GymRemoteCursor.MusicYield -> {
                GymRemoteTreeResult(
                    nextCursor = cursor,
                )
            }
        }
    }

    private fun onNext(
        tree: GymRemoteSessionTree,
        cursor: GymRemoteCursor,
    ): GymRemoteTreeResult {

        return when (cursor) {

            GymRemoteCursor.SessionRoot -> {
                GymRemoteTreeResult(
                    nextCursor = cursor,
                )
            }

            is GymRemoteCursor.ExerciseList -> {
                val nextExercise = tree.nextExerciseOrNull(
                    cursor.selectedExerciseLogId
                ) ?: return GymRemoteTreeResult(
                    nextCursor = cursor,
                )

                GymRemoteTreeResult(
                    nextCursor = GymRemoteCursor.ExerciseList(
                        selectedExerciseLogId = nextExercise.exerciseLogId,
                    ),
                    effects = listOf(
                        GymRemoteEffect.SelectRemoteExercise(
                            nextExercise.exerciseLogId,
                        ),
                        GymRemoteEffect.Speak(
                            nextExercise.exerciseSpeech()
                        )
                    )
                )
            }

            is GymRemoteCursor.SetList -> {
                val exercise = tree.exerciseById(
                    cursor.exerciseLogId
                ) ?: return GymRemoteTreeResult(
                    nextCursor = cursor,
                )

                val currentSetId = cursor.selectedSetLogId
                    ?: return GymRemoteTreeResult(
                        nextCursor = cursor,
                    )

                val nextSet = exercise.nextSetOrNull(
                    currentSetId
                )

                if (nextSet == null) {
                    GymRemoteTreeResult(
                        nextCursor = GymRemoteCursor.AddSetPrompt(
                            exerciseLogId = exercise.exerciseLogId,
                        ),
                        effects = listOf(
                            GymRemoteEffect.Speak(
                                exercise.addSetPromptSpeech()
                            )
                        )
                    )
                } else {
                    GymRemoteTreeResult(
                        nextCursor = GymRemoteCursor.SetList(
                            exerciseLogId = exercise.exerciseLogId,
                            selectedSetLogId = nextSet.setLogId,
                        ),
                        effects = listOf(
                            GymRemoteEffect.Speak(
                                nextSet.setSpeech()
                            )
                        )
                    )
                }
            }

            is GymRemoteCursor.SetField -> {
                GymRemoteTreeResult(
                    nextCursor = cursor.copy(
                        field = when (cursor.field) {
                            GymRemoteField.WEIGHT -> GymRemoteField.REPS
                            GymRemoteField.REPS -> GymRemoteField.WEIGHT
                            else -> cursor.field
                        }
                    )
                )
            }

            is GymRemoteCursor.AddSetPrompt -> {
                GymRemoteTreeResult(
                    nextCursor = cursor,
                )
            }

            is GymRemoteCursor.MusicYield -> {
                GymRemoteTreeResult(
                    nextCursor = cursor,
                )
            }
        }
    }

    private fun onPrevious(
        tree: GymRemoteSessionTree,
        cursor: GymRemoteCursor,
    ): GymRemoteTreeResult {

        return when (cursor) {

            GymRemoteCursor.SessionRoot -> {
                GymRemoteTreeResult(
                    nextCursor = cursor,
                )
            }

            is GymRemoteCursor.ExerciseList -> {
                val previousExercise = tree.previousExerciseOrNull(
                    cursor.selectedExerciseLogId
                ) ?: return GymRemoteTreeResult(
                    nextCursor = cursor,
                )

                GymRemoteTreeResult(
                    nextCursor = GymRemoteCursor.ExerciseList(
                        selectedExerciseLogId = previousExercise.exerciseLogId,
                    ),
                    effects = listOf(
                        GymRemoteEffect.SelectRemoteExercise(
                            previousExercise.exerciseLogId,
                        ),
                        GymRemoteEffect.Speak(
                            previousExercise.exerciseSpeech()
                        )
                    )
                )
            }

            is GymRemoteCursor.SetList -> {
                val exercise = tree.exerciseById(
                    cursor.exerciseLogId
                ) ?: return GymRemoteTreeResult(
                    nextCursor = cursor,
                )

                val currentSetId = cursor.selectedSetLogId
                    ?: return GymRemoteTreeResult(
                        nextCursor = cursor,
                    )

                val previousSet = exercise.previousSetOrNull(
                    currentSetId
                )

                if (previousSet == null) {
                    GymRemoteTreeResult(
                        nextCursor = GymRemoteCursor.ExerciseList(
                            selectedExerciseLogId = exercise.exerciseLogId,
                        ),
                        effects = listOf(
                            GymRemoteEffect.SelectRemoteExercise(
                                exercise.exerciseLogId,
                            ),
                            GymRemoteEffect.Speak(
                                exercise.exerciseSpeech()
                            )
                        )
                    )
                } else {
                    GymRemoteTreeResult(
                        nextCursor = GymRemoteCursor.SetList(
                            exerciseLogId = exercise.exerciseLogId,
                            selectedSetLogId = previousSet.setLogId,
                        ),
                        effects = listOf(
                            GymRemoteEffect.Speak(
                                previousSet.setSpeech()
                            )
                        )
                    )
                }
            }

            is GymRemoteCursor.SetField -> {
                GymRemoteTreeResult(
                    nextCursor = GymRemoteCursor.SetList(
                        exerciseLogId = cursor.exerciseLogId,
                        selectedSetLogId = cursor.setLogId,
                    ),
                )
            }

            is GymRemoteCursor.AddSetPrompt -> {
                val exercise = tree.exerciseById(
                    cursor.exerciseLogId
                ) ?: return GymRemoteTreeResult(
                    nextCursor = cursor,
                )

                val lastSet = exercise.lastSetOrNull()

                if (lastSet == null) {
                    GymRemoteTreeResult(
                        nextCursor = GymRemoteCursor.ExerciseList(
                            selectedExerciseLogId = exercise.exerciseLogId,
                        ),
                    )
                } else {
                    GymRemoteTreeResult(
                        nextCursor = GymRemoteCursor.SetList(
                            exerciseLogId = exercise.exerciseLogId,
                            selectedSetLogId = lastSet.setLogId,
                        ),
                        effects = listOf(
                            GymRemoteEffect.Speak(
                                lastSet.setSpeech()
                            )
                        )
                    )
                }
            }

            is GymRemoteCursor.MusicYield -> {
                GymRemoteTreeResult(
                    nextCursor = cursor,
                )
            }
        }
    }
}

data class GymRemoteTreeResult(
    val nextCursor: GymRemoteCursor,
    val effects: List<GymRemoteEffect> = emptyList(),
)