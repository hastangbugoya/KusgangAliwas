package com.example.kusgangaliwas.domain.usecase.pace

import com.example.kusgangaliwas.data.local.entity.ExercisePaceProfileEntity
import com.example.kusgangaliwas.domain.repository.ExercisePaceProfileRepository
import javax.inject.Inject

/**
 * Resolves the effective pace profile for an exercise in a split/session context.
 *
 * Resolution rule:
 * - if a split exercise has a paceProfileId, use that profile
 * - otherwise use the exercise default pace profile
 * - otherwise use no pace nudges
 *
 * Disabled profiles intentionally resolve to null so the runtime can treat them
 * as "no pace nudges" without needing special branching.
 */
class ResolveExercisePaceProfileUseCase @Inject constructor(
    private val exercisePaceProfileRepository: ExercisePaceProfileRepository,
) {
    suspend operator fun invoke(
        exerciseId: Long,
        paceProfileId: Long?,
    ): ExercisePaceProfileEntity? {
        val resolvedProfile = if (paceProfileId != null) {
            exercisePaceProfileRepository.getProfileById(paceProfileId)
                ?.takeIf { it.exerciseId == exerciseId }
        } else {
            exercisePaceProfileRepository.getDefaultProfileForExercise(exerciseId)
        }

        return resolvedProfile?.takeIf { it.isEnabled }
    }
}