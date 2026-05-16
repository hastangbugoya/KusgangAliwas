package com.example.kusgangaliwas.domain.usecase.cycle

import com.example.kusgangaliwas.data.local.entity.TrainingCycleProgressEventEntity
import com.example.kusgangaliwas.domain.repository.TrainingCycleProgressEventRepository
import javax.inject.Inject

/**
 * Marks a cycle split done without creating an actual workout session.
 *
 * This is not a workout log. It only clears the split from the current cycle
 * round.
 */
class MarkCycleSplitDoneUseCase @Inject constructor(
    private val trainingCycleProgressEventRepository:
    TrainingCycleProgressEventRepository,
) {

    suspend operator fun invoke(
        trainingCycleId: Long,
        trainingCycleStepId: Long,
        eventDateEpochDay: Long,
        createdAtEpochMillis: Long,
        notes: String? = null,
    ): Long {
        return trainingCycleProgressEventRepository.insertEvent(
            TrainingCycleProgressEventEntity(
                trainingCycleId = trainingCycleId,
                trainingCycleStepId = trainingCycleStepId,
                eventType = "markedDone",
                eventDateEpochDay = eventDateEpochDay,
                notes = notes,
                createdAtEpochMillis = createdAtEpochMillis,
            )
        )
    }
}