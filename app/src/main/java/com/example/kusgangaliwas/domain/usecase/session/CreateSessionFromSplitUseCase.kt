package com.example.kusgangaliwas.domain.usecase.session

import com.example.kusgangaliwas.data.local.entity.ActualCardioLogEntity
import com.example.kusgangaliwas.data.local.entity.ActualExerciseLogEntity
import com.example.kusgangaliwas.data.local.entity.ActualExerciseSetLogEntity
import com.example.kusgangaliwas.data.local.entity.ActualSessionEntity
import com.example.kusgangaliwas.data.local.entity.ExerciseType
import com.example.kusgangaliwas.domain.model.session.ActualSessionStatus
import com.example.kusgangaliwas.domain.repository.ExerciseRepository
import com.example.kusgangaliwas.domain.repository.SessionRepository
import com.example.kusgangaliwas.domain.repository.SplitTemplateRepository
import com.example.kusgangaliwas.domain.repository.TrainingCycleRepository
import javax.inject.Inject

class CreateSessionFromSplitUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val splitTemplateRepository: SplitTemplateRepository,
    private val exerciseRepository: ExerciseRepository,
    private val trainingCycleRepository: TrainingCycleRepository,
) {

    suspend operator fun invoke(
        splitTemplateId: Long,
        epochDay: Long,
        trainingCycleId: Long? = null,
        plannedSessionId: Long? = null,
    ): Long {
        val split = splitTemplateRepository.getSplitById(splitTemplateId)
            ?: error("Split not found.")

        val roadmap = splitTemplateRepository.getExercisesForSplit(splitTemplateId)
        val now = System.currentTimeMillis()

        val cycleStep = trainingCycleId?.let { cycleId ->
            trainingCycleRepository.getStepForSplit(
                cycleId = cycleId,
                splitTemplateId = splitTemplateId,
            )
        }

        val actualSessionId = sessionRepository.insertActualSession(
            ActualSessionEntity(
                plannedSessionId = plannedSessionId,
                performedDateEpochDay = epochDay,
                splitTemplateId = splitTemplateId,

                trainingCycleId = trainingCycleId,
                trainingCycleStepId = cycleStep?.id,
                trainingCycleStepOrderSnapshot =
                    cycleStep?.stepOrder,

                title = split.name,
                status = ActualSessionStatus.IN_PROGRESS,
                startedAtEpochMillis = now,
                completedAtEpochMillis = null,
                notes = null,
                createdAtEpochMillis = now,
                updatedAtEpochMillis = now,
            )
        )

        roadmap.forEachIndexed { index, roadmapItem ->
            val exercise = exerciseRepository.getExerciseById(roadmapItem.exerciseId)
                ?: return@forEachIndexed

            when (exercise.exerciseType) {
                ExerciseType.CARDIO -> {
                    val suggestion = sessionRepository
                        .getLatestCardioSuggestionForExercise(exercise.id)

                    sessionRepository.insertCardioLog(
                        ActualCardioLogEntity(
                            actualSessionId = actualSessionId,
                            exerciseId = exercise.id,
                            logOrder = index + 1,
                            logType = "steadyState",
                            freeTextName = exercise.name,
                            distance = null,
                            distanceUnit = suggestion?.distanceUnit ?: "mi",
                            durationSeconds = null,
                            averageInclinePercent = null,
                            averageResistance = null,
                            notes = null,
                            createdAtEpochMillis = now,
                            updatedAtEpochMillis = now,
                        )
                    )
                }

                else -> {
                    sessionRepository.insertActualExerciseLog(
                        ActualExerciseLogEntity(
                            actualSessionId = actualSessionId,
                            exerciseId = exercise.id,
                            logOrder = index + 1,
                            logType = "plannedExercise",
                            freeTextName = exercise.name,
                            notes = null,
                            performedAtEpochMillis = now,
                        )
                    )
                }
            }
        }

        return actualSessionId
    }

    private fun formatWeight(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            value.toString()
        }
    }
}