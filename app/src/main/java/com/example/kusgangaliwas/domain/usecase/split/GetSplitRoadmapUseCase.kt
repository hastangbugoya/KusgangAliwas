package com.example.kusgangaliwas.domain.usecase.split

import com.example.kusgangaliwas.data.local.entity.SplitTemplateExerciseEntity
import com.example.kusgangaliwas.domain.repository.SplitTemplateRepository
import kotlinx.coroutines.flow.Flow

/**
 * Observes the ordered exercise roadmap for a split template.
 *
 * The returned order is the suggested order only.
 * Actual session logging may happen in any order later.
 */
class GetSplitRoadmapUseCase(
    private val splitTemplateRepository: SplitTemplateRepository,
) {

    operator fun invoke(
        splitTemplateId: Long,
    ): Flow<List<SplitTemplateExerciseEntity>> {
        require(splitTemplateId > 0) {
            "Invalid splitTemplateId."
        }

        return splitTemplateRepository.observeExercisesForSplit(splitTemplateId)
    }
}