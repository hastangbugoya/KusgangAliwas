package com.example.kusgangaliwas.domain.usecase.session

import com.example.kusgangaliwas.data.local.entity.ActualCardioLogEntity
import com.example.kusgangaliwas.data.local.entity.ActualExerciseLogEntity
import com.example.kusgangaliwas.data.local.entity.ActualExerciseSetLogEntity
import com.example.kusgangaliwas.data.local.entity.ActualSessionEntity
import com.example.kusgangaliwas.data.local.entity.ExerciseType
import com.example.kusgangaliwas.domain.repository.ExerciseRepository
import com.example.kusgangaliwas.domain.repository.SessionRepository
import com.example.kusgangaliwas.domain.repository.SplitTemplateRepository
import javax.inject.Inject

class CreateSessionFromSplitUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val splitTemplateRepository: SplitTemplateRepository,
    private val exerciseRepository: ExerciseRepository,
) {

    suspend operator fun invoke(
        splitTemplateId: Long,
        epochDay: Long,
    ): Long {
        val split = splitTemplateRepository.getSplitById(splitTemplateId)
            ?: error("Split not found.")

        val roadmap = splitTemplateRepository.getExercisesForSplit(splitTemplateId)
        val now = System.currentTimeMillis()

        val actualSessionId = sessionRepository.insertActualSession(
            ActualSessionEntity(
                plannedSessionId = null,
                performedDateEpochDay = epochDay,
                splitTemplateId = splitTemplateId,
                title = split.name,
                status = "inProgress",
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
                            distance = suggestion?.distance,
                            distanceUnit = suggestion?.distanceUnit ?: "mi",
                            durationSeconds = suggestion?.durationSeconds,
                            averageInclinePercent = suggestion?.averageInclinePercent,
                            averageResistance = suggestion?.averageResistance,
                            notes = null,
                            createdAtEpochMillis = now,
                            updatedAtEpochMillis = now,
                        )
                    )
                }

                else -> {
                    val suggestion = sessionRepository
                        .getLatestWeightSuggestionForExercise(exercise.id)

                    val actualExerciseLogId = sessionRepository.insertActualExerciseLog(
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

                    if (suggestion != null) {
                        sessionRepository.insertSet(
                            ActualExerciseSetLogEntity(
                                actualExerciseLogId = actualExerciseLogId,
                                setOrder = 1,
                                weight = suggestion.suggestedWeight,
                                reps = suggestion.suggestedReps,
                                notes = buildString {
                                    append("From previous session max")
                                    append(" (")
                                    append(formatWeight(suggestion.suggestedWeight))
                                    suggestion.suggestedReps?.let { reps ->
                                        append(" × ")
                                        append(reps)
                                    }
                                    append(")")
                                },
                            )
                        )
                    }
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