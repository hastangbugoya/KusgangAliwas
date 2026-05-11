package com.example.kusgangaliwas.domain.usecase.exercise

import com.example.kusgangaliwas.data.local.entity.ExerciseEntity
import com.example.kusgangaliwas.domain.repository.ExerciseRepository
import com.example.kusgangaliwas.data.local.entity.ExerciseType

/**
 * Creates a new exercise in the user's exercise library.
 *
 * Validation stays here so UI/ViewModels can remain simple and testable.
 */
class CreateExerciseUseCase(
    private val exerciseRepository: ExerciseRepository,
) {

    suspend operator fun invoke(
        name: String,
        exerciseType: ExerciseType,
        notes: String? = null,
        nowEpochMillis: Long = System.currentTimeMillis(),
    ): Long {
        val cleanedName = name.trim()

        require(cleanedName.isNotBlank()) {
            "Exercise name cannot be blank."
        }

        return exerciseRepository.insertExercise(
            ExerciseEntity(
                name = cleanedName,
                exerciseType = exerciseType,
                notes = notes?.trim()?.takeIf { it.isNotBlank() },
                isActive = true,
                createdAtEpochMillis = nowEpochMillis,
                updatedAtEpochMillis = nowEpochMillis,
            )
        )
    }
}