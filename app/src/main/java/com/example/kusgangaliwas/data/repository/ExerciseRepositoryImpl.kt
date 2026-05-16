package com.example.kusgangaliwas.data.repository

import com.example.kusgangaliwas.data.local.dao.ExerciseDao
import com.example.kusgangaliwas.data.local.dao.ExerciseMuscleGroupDao
import com.example.kusgangaliwas.data.local.dao.ExerciseSubstitutionDao
import com.example.kusgangaliwas.data.local.dao.MuscleGroupDao
import com.example.kusgangaliwas.data.local.entity.ExerciseEntity
import com.example.kusgangaliwas.data.local.entity.ExerciseMuscleGroupCrossRef
import com.example.kusgangaliwas.data.local.entity.ExerciseSubstitutionEntity
import com.example.kusgangaliwas.data.local.entity.MuscleGroupEntity
import com.example.kusgangaliwas.domain.repository.ExerciseRepository
import com.example.kusgangaliwas.data.local.dao.ExercisePrDao
import com.example.kusgangaliwas.data.local.entity.ExercisePrEntity
import com.example.kusgangaliwas.data.local.entity.ExercisePrType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Room-backed implementation of ExerciseRepository.
 *
 * This keeps use cases independent from DAO details and groups
 * related exercise-library operations behind one boundary.
 */
class ExerciseRepositoryImpl @Inject constructor(
    private val exerciseDao: ExerciseDao,
    private val muscleGroupDao: MuscleGroupDao,
    private val exerciseMuscleGroupDao: ExerciseMuscleGroupDao,
    private val exerciseSubstitutionDao: ExerciseSubstitutionDao,
    private val exercisePrDao: ExercisePrDao,
) : ExerciseRepository {

    override fun observeActiveExercises(): Flow<List<ExerciseEntity>> {
        return exerciseDao.observeActiveExercises()
    }

    override fun observeAllExercises(): Flow<List<ExerciseEntity>> {
        return exerciseDao.observeAllExercises()
    }

    override suspend fun getExerciseById(exerciseId: Long): ExerciseEntity? {
        return exerciseDao.getExerciseById(exerciseId)
    }

    override suspend fun insertExercise(entity: ExerciseEntity): Long {
        return exerciseDao.insertExercise(entity)
    }

    override suspend fun updateExercise(entity: ExerciseEntity) {
        exerciseDao.updateExercise(entity)
    }

    override suspend fun softDeleteExercise(
        exerciseId: Long,
        updatedAtEpochMillis: Long,
    ) {
        exerciseDao.softDeleteExercise(
            exerciseId = exerciseId,
            updatedAtEpochMillis = updatedAtEpochMillis,
        )
    }

    override fun observeActiveMuscleGroups(): Flow<List<MuscleGroupEntity>> {
        return muscleGroupDao.observeActiveMuscleGroups()
    }

    override fun observeAllMuscleGroups(): Flow<List<MuscleGroupEntity>> {
        return muscleGroupDao.observeAllMuscleGroups()
    }

    override suspend fun getMuscleGroupById(muscleGroupId: Long): MuscleGroupEntity? {
        return muscleGroupDao.getMuscleGroupById(muscleGroupId)
    }

    override suspend fun insertMuscleGroup(entity: MuscleGroupEntity): Long {
        return muscleGroupDao.insertMuscleGroup(entity)
    }

    override suspend fun updateMuscleGroup(entity: MuscleGroupEntity) {
        muscleGroupDao.updateMuscleGroup(entity)
    }

    override suspend fun softDeleteMuscleGroup(muscleGroupId: Long) {
        muscleGroupDao.softDeleteMuscleGroup(muscleGroupId)
    }

    override fun observeMuscleGroupsForExercise(
        exerciseId: Long,
    ): Flow<List<ExerciseMuscleGroupCrossRef>> {
        return exerciseMuscleGroupDao.observeMuscleGroupsForExercise(exerciseId)
    }

    override fun observeExercisesForMuscleGroup(
        muscleGroupId: Long,
    ): Flow<List<ExerciseMuscleGroupCrossRef>> {
        return exerciseMuscleGroupDao.observeExercisesForMuscleGroup(muscleGroupId)
    }

    override suspend fun upsertExerciseMuscleGroup(
        entity: ExerciseMuscleGroupCrossRef,
    ) {
        exerciseMuscleGroupDao.upsertCrossRef(entity)
    }

    override suspend fun upsertExerciseMuscleGroups(
        entities: List<ExerciseMuscleGroupCrossRef>,
    ) {
        exerciseMuscleGroupDao.upsertCrossRefs(entities)
    }

    override suspend fun replaceMuscleGroupsForExercise(
        exerciseId: Long,
        mappings: List<ExerciseMuscleGroupCrossRef>,
    ) {
        exerciseMuscleGroupDao.deleteAllForExercise(exerciseId)
        if (mappings.isNotEmpty()) {
            exerciseMuscleGroupDao.upsertCrossRefs(mappings)
        }
    }

    override suspend fun deleteExerciseMuscleGroupMapping(
        exerciseId: Long,
        muscleGroupId: Long,
    ) {
        exerciseMuscleGroupDao.deleteMapping(
            exerciseId = exerciseId,
            muscleGroupId = muscleGroupId,
        )
    }

    override fun observePrsForExercise(
        exerciseId: Long,
    ): Flow<List<ExercisePrEntity>> {
        return exercisePrDao.observePrsForExercise(exerciseId)
    }

    override suspend fun getPrForExercise(
        exerciseId: Long,
        prType: ExercisePrType,
    ): ExercisePrEntity? {
        return exercisePrDao.getPrForExercise(
            exerciseId = exerciseId,
            prType = prType,
        )
    }

    override suspend fun upsertExercisePr(
        entity: ExercisePrEntity,
    ) {
        exercisePrDao.upsertExercisePr(entity)
    }

    override suspend fun deletePrForExercise(
        exerciseId: Long,
        prType: ExercisePrType,
    ) {
        exercisePrDao.deletePrForExercise(
            exerciseId = exerciseId,
            prType = prType,
        )
    }

    override fun observeActiveSubstitutionsForExercise(
        sourceExerciseId: Long,
    ): Flow<List<ExerciseSubstitutionEntity>> {
        return exerciseSubstitutionDao.observeActiveSubstitutionsForExercise(sourceExerciseId)
    }

    override fun observeAllSubstitutionsForExercise(
        sourceExerciseId: Long,
    ): Flow<List<ExerciseSubstitutionEntity>> {
        return exerciseSubstitutionDao.observeAllSubstitutionsForExercise(sourceExerciseId)
    }

    override suspend fun insertSubstitution(entity: ExerciseSubstitutionEntity): Long {
        return exerciseSubstitutionDao.insertSubstitution(entity)
    }

    override suspend fun updateSubstitution(entity: ExerciseSubstitutionEntity) {
        exerciseSubstitutionDao.updateSubstitution(entity)
    }

    override suspend fun softDeleteSubstitution(substitutionId: Long) {
        exerciseSubstitutionDao.softDeleteSubstitution(substitutionId)
    }

    override suspend fun deleteSubstitutionPair(
        sourceExerciseId: Long,
        substituteExerciseId: Long,
    ) {
        exerciseSubstitutionDao.deleteSubstitutionPair(
            sourceExerciseId = sourceExerciseId,
            substituteExerciseId = substituteExerciseId,
        )
    }
}