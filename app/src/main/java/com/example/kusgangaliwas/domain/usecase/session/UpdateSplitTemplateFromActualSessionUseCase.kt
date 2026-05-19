package com.example.kusgangaliwas.domain.usecase.session

import com.example.kusgangaliwas.data.local.entity.SplitTemplateExerciseEntity
import com.example.kusgangaliwas.domain.repository.SessionRepository
import com.example.kusgangaliwas.domain.repository.SplitTemplateRepository
import javax.inject.Inject

/**
 * Replaces an existing split template roadmap with the
 * current contents/order of an actual workout session.
 *
 * Important:
 * - Uses current session item ordering.
 * - Includes all current session exercises.
 * - Captures removals by replacing the split roadmap entirely.
 * - Ignores performed sets/reps/weights.
 * - Affects future sessions only.
 * - Historical sessions remain untouched.
 */
class UpdateSplitTemplateFromActualSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val splitTemplateRepository: SplitTemplateRepository,
) {

    suspend operator fun invoke(
        actualSessionId: Long,
    ): Boolean {
        val session = sessionRepository.getActualSessionById(actualSessionId)
            ?: return false

        val splitTemplateId = session.splitTemplateId
            ?: return false

        val exerciseLogs = sessionRepository
            .getLogsForSession(actualSessionId)
            .sortedBy { it.logOrder }

        val splitExercises = exerciseLogs.mapIndexed { index, log ->
            SplitTemplateExerciseEntity(
                splitTemplateId = splitTemplateId,
                exerciseId = requireNotNull(log.exerciseId),
                suggestedOrder = index + 1,
            )
        }

        splitTemplateRepository.deleteAllExercisesForSplit(splitTemplateId)

        splitTemplateRepository.insertSplitExercises(
            splitExercises
        )

        return true
    }
}