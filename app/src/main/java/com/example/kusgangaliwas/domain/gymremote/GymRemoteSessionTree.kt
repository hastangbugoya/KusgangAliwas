package com.example.kusgangaliwas.domain.gymremote

/**
 * V2 remote-session navigation tree for Pocket Gym Mode.
 *
 * Rationale:
 * The original remote implementation was intentionally small: one selected
 * exercise, a linear set-field focus, and a reducer that moved through
 * weight/reps fields. That worked for early testing, but it cannot represent
 * the planned pocket workflow where the user can navigate the entire session
 * without taking the phone out:
 *
 * Session root
 * -> exercise traversal
 * -> selected exercise
 * -> set traversal
 * -> set field editing
 * -> add-set prompt
 * -> music-yield/in-progress set behavior
 *
 * This file defines a Room-free, Compose-free, Android-free read model that
 * represents the current session as a tree the remote cursor can traverse.
 *
 * Future developers and AI assistants should read the technical design before
 * changing this model:
 *
 * app/src/main/java/com/example/kusgangaliwas/KA_Pocket_Gym_Remote_Mode_Technical_Design.md
 *
 * Keep this model plain and deterministic. It should be safe to unit test
 * cursor/reducer behavior using only:
 *
 * session tree + cursor + input -> next cursor + effects
 */
data class GymRemoteSessionTree(
    val exercises: List<GymRemoteExerciseNode> = emptyList(),
) {

    fun sortedExercises(): List<GymRemoteExerciseNode> {
        return exercises.sortedWith(
            compareBy<GymRemoteExerciseNode> { it.logOrder }
                .thenBy { it.exerciseLogId }
        )
    }

    fun firstExerciseOrNull(): GymRemoteExerciseNode? {
        return sortedExercises().firstOrNull()
    }

    fun exerciseById(
        exerciseLogId: Long,
    ): GymRemoteExerciseNode? {
        return exercises.firstOrNull { it.exerciseLogId == exerciseLogId }
    }

    fun nextExerciseOrNull(
        currentExerciseLogId: Long,
    ): GymRemoteExerciseNode? {
        val sorted = sortedExercises()
        val currentIndex = sorted.indexOfFirst {
            it.exerciseLogId == currentExerciseLogId
        }

        if (currentIndex == -1) return sorted.firstOrNull()

        return sorted.getOrNull(currentIndex + 1)
    }

    fun previousExerciseOrNull(
        currentExerciseLogId: Long,
    ): GymRemoteExerciseNode? {
        val sorted = sortedExercises()
        val currentIndex = sorted.indexOfFirst {
            it.exerciseLogId == currentExerciseLogId
        }

        if (currentIndex == -1) return sorted.firstOrNull()

        return sorted.getOrNull(currentIndex - 1)
    }
}

/**
 * Strength exercise node visible to remote navigation.
 *
 * Cardio can be added later as a separate node type or generalized item node.
 * For the first implementation pass, this targets strength sets only because
 * the current remote flow already works on ActualExerciseLogEntity sets.
 */
data class GymRemoteExerciseNode(
    val exerciseLogId: Long,
    val exerciseName: String,
    val logOrder: Int,
    val sets: List<GymRemoteSetNode> = emptyList(),
) {

    fun sortedSets(): List<GymRemoteSetNode> {
        return sets.sortedWith(
            compareBy<GymRemoteSetNode> { it.setOrder }
                .thenBy { it.setLogId }
        )
    }

    fun firstSetOrNull(): GymRemoteSetNode? {
        return sortedSets().firstOrNull()
    }

    fun setById(
        setLogId: Long,
    ): GymRemoteSetNode? {
        return sets.firstOrNull { it.setLogId == setLogId }
    }

    fun nextSetOrNull(
        currentSetLogId: Long,
    ): GymRemoteSetNode? {
        val sorted = sortedSets()
        val currentIndex = sorted.indexOfFirst {
            it.setLogId == currentSetLogId
        }

        if (currentIndex == -1) return sorted.firstOrNull()

        return sorted.getOrNull(currentIndex + 1)
    }

    fun previousSetOrNull(
        currentSetLogId: Long,
    ): GymRemoteSetNode? {
        val sorted = sortedSets()
        val currentIndex = sorted.indexOfFirst {
            it.setLogId == currentSetLogId
        }

        if (currentIndex == -1) return sorted.firstOrNull()

        return sorted.getOrNull(currentIndex - 1)
    }

    fun lastSetOrNull(): GymRemoteSetNode? {
        return sortedSets().lastOrNull()
    }
}

/**
 * Set node visible to remote navigation.
 */
data class GymRemoteSetNode(
    val setLogId: Long,
    val setOrder: Int,
    val weight: Double? = null,
    val reps: Int? = null,
)

/**
 * Formatting helpers for remote speech.
 *
 * Keep these plain and deterministic because the spoken output is effectively
 * the user's screen while the phone is in-pocket.
 */
fun GymRemoteExerciseNode.exerciseSpeech(): String {
    return exerciseName.ifBlank {
        "Exercise"
    }
}

fun GymRemoteExerciseNode.noSetsSpeech(): String {
    return "${exerciseSpeech()}. No sets. Press play to add set."
}

fun GymRemoteExerciseNode.addSetPromptSpeech(): String {
    return "${exerciseSpeech()}. Press play to add next set."
}

fun GymRemoteSetNode.weightSpeech(): String {
    val weightText = weight?.let(::formatGymRemoteNumber) ?: "no"
    return "Set $setOrder. $weightText pounds."
}

fun GymRemoteSetNode.repsSpeech(): String {
    val repsText = reps?.toString() ?: "no"
    return "Set $setOrder. $repsText reps."
}

fun GymRemoteSetNode.setSpeech(): String {
    return "Set $setOrder."
}

fun formatGymRemoteNumber(
    value: Double,
): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        value.toString()
    }
}