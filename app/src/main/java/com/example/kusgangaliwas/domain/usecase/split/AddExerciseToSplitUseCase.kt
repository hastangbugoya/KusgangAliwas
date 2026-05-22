package com.example.kusgangaliwas.domain.usecase.split

import com.example.kusgangaliwas.data.local.entity.SplitTemplateExerciseEntity
import com.example.kusgangaliwas.domain.repository.SplitTemplateRepository

/**
 * Adds an exercise to a split template.
 *
 * This appends to the end of the current roadmap order.
 */
class AddExerciseToSplitUseCase(
    private val splitTemplateRepository: SplitTemplateRepository,
) {

    suspend operator fun invoke(
        splitTemplateId: Long,
        exerciseId: Long,
        paceProfileId: Long? = null,
        notes: String? = null,
        isOptional: Boolean = false,
    ): Long {
        require(splitTemplateId > 0) {
            "Invalid splitTemplateId."
        }

        require(exerciseId > 0) {
            "Invalid exerciseId."
        }

        val currentExercises = splitTemplateRepository
            .getExercisesForSplit(splitTemplateId)

        val nextOrder = if (currentExercises.isEmpty()) {
            0
        } else {
            currentExercises.maxOf { it.suggestedOrder } + 1
        }

        return splitTemplateRepository.insertSplitExercise(
            SplitTemplateExerciseEntity(
                splitTemplateId = splitTemplateId,
                exerciseId = exerciseId,
                paceProfileId = paceProfileId,
                suggestedOrder = nextOrder,
                notes = notes?.trim()?.takeIf { it.isNotBlank() },
                isOptional = isOptional,
            )
        )
    }
}