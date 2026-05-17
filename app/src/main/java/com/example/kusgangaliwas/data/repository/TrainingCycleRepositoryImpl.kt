package com.example.kusgangaliwas.data.repository

import com.example.kusgangaliwas.data.local.dao.CycleCalendarAnchorDao
import com.example.kusgangaliwas.data.local.dao.TrainingCycleDao
import com.example.kusgangaliwas.data.local.dao.TrainingCycleStepDao
import com.example.kusgangaliwas.data.local.entity.CycleCalendarAnchorEntity
import com.example.kusgangaliwas.data.local.entity.TrainingCycleEntity
import com.example.kusgangaliwas.data.local.entity.TrainingCycleStepEntity
import com.example.kusgangaliwas.domain.repository.TrainingCycleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Room-backed implementation of TrainingCycleRepository.
 */
class TrainingCycleRepositoryImpl @Inject constructor(
    private val trainingCycleDao: TrainingCycleDao,
    private val trainingCycleStepDao: TrainingCycleStepDao,
    private val cycleCalendarAnchorDao: CycleCalendarAnchorDao,
) : TrainingCycleRepository {

    override fun observeActiveCycles(): Flow<List<TrainingCycleEntity>> {
        return trainingCycleDao.observeActiveCycles()
    }

    override suspend fun getActiveCycles():
            List<TrainingCycleEntity> {

        return trainingCycleDao.getActiveCycles()
    }

    override fun observeAllCycles(): Flow<List<TrainingCycleEntity>> {
        return trainingCycleDao.observeAllCycles()
    }

    override fun observeMostRecentlyUpdatedActiveCycle():
            Flow<TrainingCycleEntity?> {

        return trainingCycleDao
            .observeMostRecentlyUpdatedActiveCycle()
    }

    override suspend fun getAnyActiveCycle():
            TrainingCycleEntity? {

        return trainingCycleDao.getAnyActiveCycle()
    }

    override suspend fun getCycleById(cycleId: Long): TrainingCycleEntity? {
        return trainingCycleDao.getCycleById(cycleId)
    }

    override suspend fun insertCycle(entity: TrainingCycleEntity): Long {
        return trainingCycleDao.insertCycle(entity)
    }

    override suspend fun updateCycle(entity: TrainingCycleEntity) {
        trainingCycleDao.updateCycle(entity)
    }

    override suspend fun softDeleteCycle(
        cycleId: Long,
        updatedAtEpochMillis: Long,
    ) {
        trainingCycleDao.softDeleteCycle(
            cycleId = cycleId,
            updatedAtEpochMillis = updatedAtEpochMillis,
        )
    }

    override fun observeStepsForCycle(
        cycleId: Long,
    ): Flow<List<TrainingCycleStepEntity>> {
        return trainingCycleStepDao.observeStepsForCycle(cycleId)
    }

    override suspend fun getStepsForCycle(
        cycleId: Long,
    ): List<TrainingCycleStepEntity> {
        return trainingCycleStepDao.getStepsForCycle(cycleId)
    }

    override suspend fun getStepById(
        stepId: Long,
    ): TrainingCycleStepEntity? {
        return trainingCycleStepDao.getStepById(stepId)
    }

    override suspend fun getStepForSplit(
        cycleId: Long,
        splitTemplateId: Long,
    ): TrainingCycleStepEntity? {

        return trainingCycleStepDao.getStepForSplit(
            cycleId = cycleId,
            splitTemplateId = splitTemplateId,
        )
    }

    override suspend fun insertStep(entity: TrainingCycleStepEntity): Long {
        return trainingCycleStepDao.insertStep(entity)
    }

    override suspend fun insertSteps(entities: List<TrainingCycleStepEntity>) {
        trainingCycleStepDao.insertSteps(entities)
    }

    override suspend fun updateStep(entity: TrainingCycleStepEntity) {
        trainingCycleStepDao.updateStep(entity)
    }

    override suspend fun updateSteps(entities: List<TrainingCycleStepEntity>) {
        trainingCycleStepDao.updateSteps(entities)
    }

    override suspend fun deleteStep(stepId: Long) {
        trainingCycleStepDao.deleteStep(stepId)
    }

    override suspend fun deleteStepForSplit(
        cycleId: Long,
        splitTemplateId: Long,
    ) {
        trainingCycleStepDao.deleteStepForSplit(
            cycleId = cycleId,
            splitTemplateId = splitTemplateId,
        )
    }

    override suspend fun deleteAllStepsForCycle(cycleId: Long) {
        trainingCycleStepDao.deleteAllStepsForCycle(cycleId)
    }

    override fun observeAnchorsForCycle(
        cycleId: Long,
    ): Flow<List<CycleCalendarAnchorEntity>> {
        return cycleCalendarAnchorDao.observeAnchorsForCycle(cycleId)
    }

    override suspend fun getAnchorsForCycle(
        cycleId: Long,
    ): List<CycleCalendarAnchorEntity> {
        return cycleCalendarAnchorDao.getAnchorsForCycle(cycleId)
    }

    override suspend fun insertAnchor(entity: CycleCalendarAnchorEntity): Long {
        return cycleCalendarAnchorDao.insertAnchor(entity)
    }

    override suspend fun upsertAnchor(entity: CycleCalendarAnchorEntity) {
        cycleCalendarAnchorDao.upsertAnchor(entity)
    }

    override suspend fun deleteAnchor(anchorId: Long) {
        cycleCalendarAnchorDao.deleteAnchor(anchorId)
    }

    override suspend fun deleteAllAnchorsForCycle(cycleId: Long) {
        cycleCalendarAnchorDao.deleteAllAnchorsForCycle(cycleId)
    }
}