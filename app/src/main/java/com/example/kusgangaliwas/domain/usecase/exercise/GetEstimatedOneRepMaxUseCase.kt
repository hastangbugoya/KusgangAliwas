package com.example.kusgangaliwas.domain.usecase.exercise

import com.example.kusgangaliwas.domain.repository.SessionRepository
import javax.inject.Inject

/**
 * Computes estimated 1RM (one-rep max) for an exercise using logged sets.
 *
 * Uses Epley formula:
 * 1RM = weight * (1 + reps / 30)
 */
class GetEstimatedOneRepMaxUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
) {

    suspend operator fun invoke(
        exerciseId: Long,
    ): Double? {
        require(exerciseId > 0) {
            "Invalid exerciseId."
        }

        val logs = sessionRepository.getLogsForExercise(exerciseId)

        return logs
            .flatMap { log ->
                sessionRepository.getSetsForExercise(log.id)
            }
            .mapNotNull { set ->
                val weight = set.weight
                val reps = set.reps

                if (weight == null || reps == null || weight <= 0.0 || reps <= 0) {
                    null
                } else {
                    estimateOneRepMax(weight = weight, reps = reps)
                }
            }
            .maxOrNull()
    }

    private fun estimateOneRepMax(
        weight: Double,
        reps: Int,
    ): Double {
        return weight * (1.0 + reps / 30.0)
    }
}