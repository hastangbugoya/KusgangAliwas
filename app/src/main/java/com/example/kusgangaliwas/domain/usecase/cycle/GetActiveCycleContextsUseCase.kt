package com.example.kusgangaliwas.domain.usecase.cycle

import com.example.kusgangaliwas.domain.model.cycle.CycleDayContext
import com.example.kusgangaliwas.domain.repository.SessionRepository
import com.example.kusgangaliwas.domain.repository.SplitTemplateRepository
import com.example.kusgangaliwas.domain.repository.TrainingCycleRepository
import javax.inject.Inject

class GetActiveCycleContextsUseCase @Inject constructor(
    private val trainingCycleRepository: TrainingCycleRepository,
    private val splitTemplateRepository: SplitTemplateRepository,
    private val sessionRepository: SessionRepository,
    private val getNextCycleSplitSuggestionUseCase:
    GetNextCycleSplitSuggestionUseCase,
) {

    suspend operator fun invoke(): List<CycleDayContext> {
        val cycles = trainingCycleRepository
            .getActiveCycles()

        return cycles.map { cycle ->
            val nextStep = getNextCycleSplitSuggestionUseCase(
                trainingCycleId = cycle.id,
            )

            val nextSplit = nextStep?.splitTemplateId?.let { splitId ->
                splitTemplateRepository.getSplitById(splitId)
            }

            val latestSession = sessionRepository
                .getLatestCompletedCycleSession(cycle.id)

            val latestStep = latestSession
                ?.trainingCycleStepId
                ?.let { stepId ->
                    trainingCycleRepository.getStepById(stepId)
                }

            val latestSplit = latestStep
                ?.splitTemplateId
                ?.let { splitId ->
                    splitTemplateRepository.getSplitById(splitId)
                }

            CycleDayContext(
                trainingCycleId = cycle.id,
                trainingCycleName = cycle.name,
                lastCompletedStepName = latestSplit?.name,
                lastCompletedEpochDay =
                    latestSession?.performedDateEpochDay,
                nextStepId = nextStep?.id,
                nextSplitTemplateId = nextStep?.splitTemplateId,
                nextStepName = nextSplit?.name,
                hasWarnBeforeMarkDone =
                    nextStep?.warnBeforeMarkDone ?: false,
            )
        }
    }
}