package com.example.kusgangaliwas.data.repository

import com.example.kusgangaliwas.data.local.dao.SplitScheduleDao
import com.example.kusgangaliwas.data.local.entity.SplitScheduleEntity
import com.example.kusgangaliwas.domain.repository.SplitScheduleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room-backed implementation of SplitScheduleRepository.
 *
 * This repository is intentionally lightweight and focused on persistence.
 * Schedule expansion/materialization should live in use cases.
 */
@Singleton
class SplitScheduleRepositoryImpl @Inject constructor(
    private val splitScheduleDao: SplitScheduleDao,
) : SplitScheduleRepository {

    override fun observeAllSchedules(): Flow<List<SplitScheduleEntity>> {
        return splitScheduleDao.observeAllSchedules()
    }

    override fun observeActiveSchedules(): Flow<List<SplitScheduleEntity>> {
        return splitScheduleDao.observeActiveSchedules()
    }

    override fun observeSchedulesForSplitTemplate(
        splitTemplateId: Long,
    ): Flow<List<SplitScheduleEntity>> {
        return splitScheduleDao.observeSchedulesForSplitTemplate(
            splitTemplateId = splitTemplateId,
        )
    }

    override fun observeSchedulesForProgram(
        programId: Long,
    ): Flow<List<SplitScheduleEntity>> {
        return splitScheduleDao.observeSchedulesForProgram(
            programId = programId,
        )
    }

    override suspend fun getScheduleById(id: Long): SplitScheduleEntity? {
        return splitScheduleDao.getScheduleById(id)
    }

    override suspend fun upsertSchedule(schedule: SplitScheduleEntity): Long {
        return splitScheduleDao.upsertSchedule(schedule)
    }

    override suspend fun updateSchedule(schedule: SplitScheduleEntity) {
        splitScheduleDao.updateSchedule(schedule)
    }

    override suspend fun deleteSchedule(schedule: SplitScheduleEntity) {
        splitScheduleDao.deleteSchedule(schedule)
    }

    override suspend fun deleteScheduleById(id: Long) {
        splitScheduleDao.deleteScheduleById(id)
    }
}