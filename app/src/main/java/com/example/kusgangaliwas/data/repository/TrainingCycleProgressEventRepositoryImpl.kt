package com.example.kusgangaliwas.data.repository

import com.example.kusgangaliwas.data.local.dao.TrainingCycleProgressEventDao
import com.example.kusgangaliwas.data.local.entity.TrainingCycleProgressEventEntity
import com.example.kusgangaliwas.domain.repository.TrainingCycleProgressEventRepository
import javax.inject.Inject

class TrainingCycleProgressEventRepositoryImpl @Inject constructor(
    private val trainingCycleProgressEventDao: TrainingCycleProgressEventDao,
) : TrainingCycleProgressEventRepository {

    override suspend fun getEventsForCycle(
        trainingCycleId: Long,
    ): List<TrainingCycleProgressEventEntity> {
        return trainingCycleProgressEventDao.getEventsForCycle(
            trainingCycleId = trainingCycleId,
        )
    }

    override suspend fun getLatestEventForCycle(
        trainingCycleId: Long,
    ): TrainingCycleProgressEventEntity? {
        return trainingCycleProgressEventDao.getLatestEventForCycle(
            trainingCycleId = trainingCycleId,
        )
    }

    override suspend fun insertEvent(
        entity: TrainingCycleProgressEventEntity,
    ): Long {
        return trainingCycleProgressEventDao.insertEvent(entity)
    }

    override suspend fun deleteEvent(
        eventId: Long,
    ) {
        trainingCycleProgressEventDao.deleteEvent(eventId)
    }

    override suspend fun deleteEventsForCycle(
        trainingCycleId: Long,
    ) {
        trainingCycleProgressEventDao.deleteEventsForCycle(
            trainingCycleId = trainingCycleId,
        )
    }
}