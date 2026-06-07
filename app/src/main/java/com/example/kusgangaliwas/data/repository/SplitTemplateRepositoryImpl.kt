package com.example.kusgangaliwas.data.repository

import com.example.kusgangaliwas.data.local.dao.SplitTemplateDao
import com.example.kusgangaliwas.data.local.dao.SplitTemplateExerciseDao
import com.example.kusgangaliwas.data.local.dao.SplitTemplateMuscleGroupDao
import com.example.kusgangaliwas.data.local.entity.SplitTemplateEntity
import com.example.kusgangaliwas.data.local.entity.SplitTemplateExerciseEntity
import com.example.kusgangaliwas.data.local.entity.SplitTemplateMuscleGroupCrossRef
import com.example.kusgangaliwas.data.local.model.SplitTemplateSummaryRow
import com.example.kusgangaliwas.domain.repository.SplitTemplateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Room-backed implementation of SplitTemplateRepository.
 */
class SplitTemplateRepositoryImpl @Inject constructor(
    private val splitTemplateDao: SplitTemplateDao,
    private val splitTemplateExerciseDao: SplitTemplateExerciseDao,
    private val splitTemplateMuscleGroupDao: SplitTemplateMuscleGroupDao,
) : SplitTemplateRepository {

    override fun observeActiveSplits(): Flow<List<SplitTemplateEntity>> {
        return splitTemplateDao.observeActiveSplits()
    }

    override fun observeAllSplits(): Flow<List<SplitTemplateEntity>> {
        return splitTemplateDao.observeAllSplits()
    }

    override suspend fun getActiveSplitSummaries():
            List<SplitTemplateSummaryRow> {

        return splitTemplateDao.getActiveSplitSummaries()
    }

    override suspend fun getSplitById(splitId: Long): SplitTemplateEntity? {
        return splitTemplateDao.getSplitById(splitId)
    }

    override suspend fun insertSplit(entity: SplitTemplateEntity): Long {
        return splitTemplateDao.insertSplit(entity)
    }

    override suspend fun updateSplit(entity: SplitTemplateEntity) {
        splitTemplateDao.updateSplit(entity)
    }

    override suspend fun softDeleteSplit(
        splitId: Long,
        updatedAtEpochMillis: Long,
    ) {
        splitTemplateDao.softDeleteSplit(
            splitId = splitId,
            updatedAtEpochMillis = updatedAtEpochMillis,
        )
    }

    override suspend fun restoreSplit(
        splitId: Long,
        updatedAtEpochMillis: Long,
    ) {
        splitTemplateDao.restoreSplit(
            splitId = splitId,
            updatedAtEpochMillis = updatedAtEpochMillis,
        )
    }

    override fun observeExercisesForSplit(
        splitTemplateId: Long,
    ): Flow<List<SplitTemplateExerciseEntity>> {
        return splitTemplateExerciseDao.observeExercisesForSplit(splitTemplateId)
    }

    override suspend fun getExercisesForSplit(
        splitTemplateId: Long,
    ): List<SplitTemplateExerciseEntity> {
        return splitTemplateExerciseDao.getExercisesForSplit(splitTemplateId)
    }

    override suspend fun getSplitExerciseById(
        id: Long,
    ): SplitTemplateExerciseEntity? {
        return splitTemplateExerciseDao.getSplitExerciseById(id)
    }

    override suspend fun insertSplitExercise(
        entity: SplitTemplateExerciseEntity,
    ): Long {
        return splitTemplateExerciseDao.insertSplitExercise(entity)
    }

    override suspend fun insertSplitExercises(
        entities: List<SplitTemplateExerciseEntity>,
    ) {
        splitTemplateExerciseDao.insertSplitExercises(entities)
    }

    override suspend fun updateSplitExercise(
        entity: SplitTemplateExerciseEntity,
    ) {
        splitTemplateExerciseDao.updateSplitExercise(entity)
    }

    override suspend fun updatePaceProfileForSplitExercise(
        splitTemplateExerciseId: Long,
        paceProfileId: Long?,
    ) {
        splitTemplateExerciseDao.updatePaceProfileForSplitExercise(
            splitTemplateExerciseId = splitTemplateExerciseId,
            paceProfileId = paceProfileId,
        )
    }

    override suspend fun updateSplitExercises(
        entities: List<SplitTemplateExerciseEntity>,
    ) {
        splitTemplateExerciseDao.updateSplitExercises(entities)
    }

    override suspend fun deleteSplitExercise(id: Long) {
        splitTemplateExerciseDao.deleteSplitExercise(id)
    }

    override suspend fun deleteAllExercisesForSplit(splitTemplateId: Long) {
        splitTemplateExerciseDao.deleteAllForSplit(splitTemplateId)
    }

    override fun observeMuscleGroupsForSplit(
        splitTemplateId: Long,
    ): Flow<List<SplitTemplateMuscleGroupCrossRef>> {
        return splitTemplateMuscleGroupDao.observeForSplit(splitTemplateId)
    }

    override suspend fun upsertSplitMuscleGroup(
        crossRef: SplitTemplateMuscleGroupCrossRef,
    ) {
        splitTemplateMuscleGroupDao.upsert(crossRef)
    }

    override suspend fun deleteSplitMuscleGroup(
        splitTemplateId: Long,
        muscleGroupId: Long,
    ) {
        splitTemplateMuscleGroupDao.delete(
            splitTemplateId = splitTemplateId,
            muscleGroupId = muscleGroupId,
        )
    }
}