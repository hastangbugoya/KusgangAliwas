package com.example.kusgangaliwas.domain.usecase.cycle

import com.example.kusgangaliwas.data.local.entity.TrainingCycleStepEntity
import com.example.kusgangaliwas.domain.model.cycle.CycleProgressCompletion
import com.example.kusgangaliwas.domain.repository.SessionRepository
import com.example.kusgangaliwas.domain.repository.TrainingCycleProgressEventRepository
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
 * The current round is determined by walking all progression completions
 * oldest -> newest.
 *
 * Progression completions come from:
 * - completed workout sessions
 * - mark-done events
 *
 * Each unique completed step is added to the current round. When all cycle
 * steps have been completed, the round resets automatically.
 *
 * The next suggestion is the lowest ordered cycle step not yet completed in
 * the current round.
 */
class GetNextCycleSplitSuggestionUseCase @Inject constructor(
    private val trainingCycleRepository: TrainingCycleRepository,
    private val sessionRepository: SessionRepository,
    private val trainingCycleProgressEventRepository:
    TrainingCycleProgressEventRepository,
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

        val sessionCompletions = sessionRepository
            .getCompletedCycleSessions(trainingCycleId)
            .mapNotNull { session ->

                val stepId = session.trainingCycleStepId
                    ?: return@mapNotNull null

                CycleProgressCompletion(
                    trainingCycleStepId = stepId,
                    epochDay = session.performedDateEpochDay,
                    sourceType = "session",
                    sourceId = session.id,
                )
            }

        val progressEventCompletions =
            trainingCycleProgressEventRepository
                .getEventsForCycle(trainingCycleId)
                .map { event ->
                    CycleProgressCompletion(
                        trainingCycleStepId = event.trainingCycleStepId,
                        epochDay = event.eventDateEpochDay,
                        sourceType = event.eventType,
                        sourceId = event.id,
                    )
                }

        val allCompletions = (
                sessionCompletions + progressEventCompletions
                ).sortedWith(
                compareBy<CycleProgressCompletion> {
                    it.epochDay
                }.thenBy {
                    it.sourceId
                }
            )

        val completedStepIdsInCurrentRound = mutableSetOf<Long>()

        allCompletions.forEach { completion ->

            val stepId = completion.trainingCycleStepId

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