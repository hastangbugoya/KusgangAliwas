package com.example.kusgangaliwas.domain.usecase.session

import com.example.kusgangaliwas.data.local.entity.SplitTemplateEntity
import com.example.kusgangaliwas.data.local.entity.SplitTemplateExerciseEntity
import com.example.kusgangaliwas.domain.repository.SessionRepository
import com.example.kusgangaliwas.domain.repository.SplitTemplateRepository
import javax.inject.Inject

/**
 * Creates a brand-new reusable split template from the
 * current contents/order of an actual workout session.
 *
 * Important:
 * - Uses current session item ordering.
 * - Includes all current session exercises.
 * - Ignores performed sets/reps/weights.
 * - Intended to snapshot workout structure only.
 * - Historical sessions remain untouched.
 */
class CreateSplitTemplateFromActualSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val splitTemplateRepository: SplitTemplateRepository,
) {

    suspend operator fun invoke(
        actualSessionId: Long,
        splitName: String,
        splitNotes: String?,
        createdAtEpochMillis: Long = System.currentTimeMillis(),
    ): Long {

        val session = sessionRepository.getActualSessionById(actualSessionId)
            ?: error("Actual session not found.")

        val exerciseLogs = sessionRepository
            .getLogsForSession(actualSessionId)
            .sortedBy { it.logOrder }

        val splitId = splitTemplateRepository.insertSplit(
            SplitTemplateEntity(
                name = splitName,
                notes = splitNotes,
                isActive = true,
                createdAtEpochMillis = createdAtEpochMillis,
                updatedAtEpochMillis = createdAtEpochMillis,
            )
        )

        val splitExercises = exerciseLogs.mapIndexed { index, log ->
            SplitTemplateExerciseEntity(
                splitTemplateId = splitId,
                exerciseId = requireNotNull(log.exerciseId),
                suggestedOrder = index + 1,
            )
        }

        splitTemplateRepository.insertSplitExercises(
            splitExercises
        )

        return splitId
    }
}