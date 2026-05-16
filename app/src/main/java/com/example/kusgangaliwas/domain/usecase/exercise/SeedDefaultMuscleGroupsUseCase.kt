package com.example.kusgangaliwas.domain.usecase.exercise

import com.example.kusgangaliwas.data.local.entity.MuscleGroupEntity
import com.example.kusgangaliwas.domain.repository.ExerciseRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class SeedDefaultMuscleGroupsUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
) {

    suspend operator fun invoke() {
        val existingMuscleGroups = exerciseRepository
            .observeAllMuscleGroups()
            .first()

        if (existingMuscleGroups.isNotEmpty()) {
            return
        }

        defaultMuscleGroups.forEachIndexed { index, name ->
            exerciseRepository.insertMuscleGroup(
                MuscleGroupEntity(
                    name = name,
                    sortOrder = index,
                    isActive = true,
                )
            )
        }
    }

    private companion object {
        val defaultMuscleGroups = listOf(
            "Upper front",
            "Upper back",
            "Lower back",
            "Arms",
            "Abs",
            "Legs",
            "Misc",
        )
    }
}