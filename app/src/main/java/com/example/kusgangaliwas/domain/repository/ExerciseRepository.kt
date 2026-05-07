package com.example.kusgangaliwas.domain.repository

import com.example.kusgangaliwas.data.local.entity.ExerciseEntity
import com.example.kusgangaliwas.data.local.entity.ExerciseMuscleGroupCrossRef
import com.example.kusgangaliwas.data.local.entity.ExerciseSubstitutionEntity
import com.example.kusgangaliwas.data.local.entity.MuscleGroupEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository boundary for exercise-library behavior.
 *
 * This repository groups:
 * - exercises
 * - muscle groups
 * - exercise ↔ muscle group mappings
 * - explicit user-defined substitutions
 *
 * Use cases should depend on this interface, not directly on DAOs.
 */
interface ExerciseRepository {

    fun observeActiveExercises(): Flow<List<ExerciseEntity>>

    fun observeAllExercises(): Flow<List<ExerciseEntity>>

    suspend fun getExerciseById(exerciseId: Long): ExerciseEntity?

    suspend fun insertExercise(entity: ExerciseEntity): Long

    suspend fun updateExercise(entity: ExerciseEntity)

    suspend fun softDeleteExercise(
        exerciseId: Long,
        updatedAtEpochMillis: Long,
    )

    fun observeActiveMuscleGroups(): Flow<List<MuscleGroupEntity>>

    fun observeAllMuscleGroups(): Flow<List<MuscleGroupEntity>>

    suspend fun getMuscleGroupById(muscleGroupId: Long): MuscleGroupEntity?

    suspend fun insertMuscleGroup(entity: MuscleGroupEntity): Long

    suspend fun updateMuscleGroup(entity: MuscleGroupEntity)

    suspend fun softDeleteMuscleGroup(muscleGroupId: Long)

    fun observeMuscleGroupsForExercise(
        exerciseId: Long,
    ): Flow<List<ExerciseMuscleGroupCrossRef>>

    fun observeExercisesForMuscleGroup(
        muscleGroupId: Long,
    ): Flow<List<ExerciseMuscleGroupCrossRef>>

    suspend fun upsertExerciseMuscleGroup(
        entity: ExerciseMuscleGroupCrossRef,
    )

    suspend fun upsertExerciseMuscleGroups(
        entities: List<ExerciseMuscleGroupCrossRef>,
    )

    suspend fun replaceMuscleGroupsForExercise(
        exerciseId: Long,
        mappings: List<ExerciseMuscleGroupCrossRef>,
    )

    suspend fun deleteExerciseMuscleGroupMapping(
        exerciseId: Long,
        muscleGroupId: Long,
    )

    fun observeActiveSubstitutionsForExercise(
        sourceExerciseId: Long,
    ): Flow<List<ExerciseSubstitutionEntity>>

    fun observeAllSubstitutionsForExercise(
        sourceExerciseId: Long,
    ): Flow<List<ExerciseSubstitutionEntity>>

    suspend fun insertSubstitution(entity: ExerciseSubstitutionEntity): Long

    suspend fun updateSubstitution(entity: ExerciseSubstitutionEntity)

    suspend fun softDeleteSubstitution(substitutionId: Long)

    suspend fun deleteSubstitutionPair(
        sourceExerciseId: Long,
        substituteExerciseId: Long,
    )
}