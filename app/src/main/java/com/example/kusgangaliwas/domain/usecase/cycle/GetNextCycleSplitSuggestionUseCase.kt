package com.example.kusgangaliwas.domain.usecase.cycle

import com.example.kusgangaliwas.data.local.entity.ActualSessionEntity
import com.example.kusgangaliwas.data.local.entity.TrainingCycleStepEntity
import com.example.kusgangaliwas.domain.repository.SessionRepository
import com.example.kusgangaliwas.domain.repository.TrainingCycleRepository
import javax.inject.Inject

/**
 * Determines the next suggested split for a training cycle.
 *
 * Rules:
 * - cycles are day-agnostic
 * - cycles are non-blocking
 * - cycle progression is based on completed splits
 * - leapfrogging is allowed
 * - duplicate completion of the same split within the same round does not
 *   advance the cycle
 *
 * The current round is determined by walking completed cycle sessions from
 * oldest to newest. Each unique completed step is added to the current round.
 * When all cycle steps have been completed, the current round is cleared and
 * the next round begins.
 *
 * The next suggestion is the lowest ordered cycle step not yet completed in
 * the current round.
 */
class GetNextCycleSplitSuggestionUseCase @Inject constructor(
    private val trainingCycleRepository: TrainingCycleRepository,
    private val sessionRepository: SessionRepository,
) {

    suspend operator fun invoke(
        trainingCycleId: Long,
    ): TrainingCycleStepEntity? {
        val orderedSteps = trainingCycleRepository
            .getStepsForCycle(trainingCycleId)
            .sortedBy { it.stepOrder }

        if (orderedSteps.isEmpty()) {
            return null
        }

        val validStepIds = orderedSteps
            .map { it.id }
            .toSet()

        val completedSessions = sessionRepository
            .getCompletedCycleSessions(trainingCycleId)
            .sortedWith(
                compareBy<ActualSessionEntity> {
                    it.performedDateEpochDay
                }.thenBy {
                    it.id
                }
            )

        val completedStepIdsInCurrentRound = mutableSetOf<Long>()

        completedSessions.forEach { session ->
            val stepId = session.trainingCycleStepId
                ?: return@forEach

            if (stepId !in validStepIds) {
                return@forEach
            }

            completedStepIdsInCurrentRound.add(stepId)

            if (completedStepIdsInCurrentRound.size == orderedSteps.size) {
                completedStepIdsInCurrentRound.clear()
            }
        }

        return orderedSteps.firstOrNull { step ->
            step.id !in completedStepIdsInCurrentRound
        } ?: orderedSteps.firstOrNull()
    }
}