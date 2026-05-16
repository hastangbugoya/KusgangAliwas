package com.example.kusgangaliwas.domain.repository

import com.example.kusgangaliwas.data.local.entity.TrainingCycleProgressEventEntity

interface TrainingCycleProgressEventRepository {

    suspend fun getEventsForCycle(
        trainingCycleId: Long,
    ): List<TrainingCycleProgressEventEntity>

    suspend fun getLatestEventForCycle(
        trainingCycleId: Long,
    ): TrainingCycleProgressEventEntity?

    suspend fun insertEvent(
        entity: TrainingCycleProgressEventEntity,
    ): Long

    suspend fun deleteEvent(
        eventId: Long,
    )

    suspend fun deleteEventsForCycle(
        trainingCycleId: Long,
    )
}