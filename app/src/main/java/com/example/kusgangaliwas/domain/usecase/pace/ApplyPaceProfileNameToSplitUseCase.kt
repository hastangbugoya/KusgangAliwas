package com.example.kusgangaliwas.domain.usecase.pace

import com.example.kusgangaliwas.domain.repository.ExercisePaceProfileRepository
import com.example.kusgangaliwas.domain.repository.SplitTemplateRepository
import javax.inject.Inject

/**
 * Applies a reusable pace profile name across a split.
 *
 * Pace profile names are reusable across exercises, but each exercise still
 * owns its own timing values. For example:
 * - Treadmill can have a "Speed" pace profile
 * - Bench Press can have a different "Speed" pace profile
 * - Jump Rope can have another "Speed" pace profile
 *
 * This use case finds split exercises whose exercise has a matching enabled
 * pace profile name and assigns that specific profile to the split exercise.
 *
 * Exercises without a matching enabled profile are intentionally skipped. This
 * keeps pace nudges gentle and non-punitive: applying "Speed" to a split should
 * never fail just because some exercises do not have a "Speed" profile.
 */
class ApplyPaceProfileNameToSplitUseCase @Inject constructor(
    private val splitTemplateRepository: SplitTemplateRepository,
    private val exercisePaceProfileRepository: ExercisePaceProfileRepository,
) {
    suspend operator fun invoke(
        splitTemplateId: Long,
        paceProfileName: String,
    ): ApplyPaceProfileNameToSplitResult {
        require(splitTemplateId > 0L) {
            "Invalid splitTemplateId."
        }

        val cleanedName = paceProfileName.trim()

        if (cleanedName.isBlank()) {
            return ApplyPaceProfileNameToSplitResult(
                requestedName = cleanedName,
                totalSplitExercises = 0,
                matchedExercises = 0,
                skippedExercises = 0,
            )
        }

        val splitExercises = splitTemplateRepository
            .getExercisesForSplit(splitTemplateId)

        if (splitExercises.isEmpty()) {
            return ApplyPaceProfileNameToSplitResult(
                requestedName = cleanedName,
                totalSplitExercises = 0,
                matchedExercises = 0,
                skippedExercises = 0,
            )
        }

        val exerciseIds = splitExercises
            .map { splitExercise -> splitExercise.exerciseId }
            .distinct()

        val matchingProfilesByExerciseId = exercisePaceProfileRepository
            .getProfilesForExercisesByName(
                exerciseIds = exerciseIds,
                name = cleanedName,
            )
            .filter { profile -> profile.isEnabled }
            .groupBy { profile -> profile.exerciseId }
            .mapValues { (_, profiles) ->
                profiles.first()
            }

        var matchedCount = 0

        splitExercises.forEach { splitExercise ->
            val matchingProfile = matchingProfilesByExerciseId[splitExercise.exerciseId]
                ?: return@forEach

            splitTemplateRepository.updatePaceProfileForSplitExercise(
                splitTemplateExerciseId = splitExercise.id,
                paceProfileId = matchingProfile.id,
            )

            matchedCount += 1
        }

        return ApplyPaceProfileNameToSplitResult(
            requestedName = cleanedName,
            totalSplitExercises = splitExercises.size,
            matchedExercises = matchedCount,
            skippedExercises = splitExercises.size - matchedCount,
        )
    }
}

data class ApplyPaceProfileNameToSplitResult(
    val requestedName: String,
    val totalSplitExercises: Int,
    val matchedExercises: Int,
    val skippedExercises: Int,
)