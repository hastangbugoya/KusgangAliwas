package com.example.kusgangaliwas.domain.repository

import com.example.kusgangaliwas.data.local.entity.ExercisePaceProfileEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for exercise pace profiles.
 *
 * Pace profiles are optional named timing presets for an exercise. They support
 * gentle nudges during focused exercise work without making timing strict or
 * punitive.
 *
 * Resolution rule used by higher-level session logic:
 * - if a split exercise has a paceProfileId, use that profile
 * - otherwise use the exercise default profile
 * - otherwise use no pace nudges
 *
 * Pace names may be reused across exercises. For example, multiple exercises
 * can each define their own "Speed" profile with exercise-specific timing.
 */
interface ExercisePaceProfileRepository {
    fun observeProfilesForExercise(exerciseId: Long): Flow<List<ExercisePaceProfileEntity>>

    suspend fun getProfilesForExercise(exerciseId: Long): List<ExercisePaceProfileEntity>

    suspend fun getProfileById(profileId: Long): ExercisePaceProfileEntity?

    suspend fun getProfileForExerciseByName(
        exerciseId: Long,
        name: String,
    ): ExercisePaceProfileEntity?

    suspend fun getProfilesForExercisesByName(
        exerciseIds: List<Long>,
        name: String,
    ): List<ExercisePaceProfileEntity>

    suspend fun getDefaultProfileForExercise(exerciseId: Long): ExercisePaceProfileEntity?

    suspend fun getEnabledProfilesForExercise(exerciseId: Long): List<ExercisePaceProfileEntity>

    suspend fun insertProfile(profile: ExercisePaceProfileEntity): Long

    suspend fun updateProfile(profile: ExercisePaceProfileEntity)

    suspend fun deleteProfile(profile: ExercisePaceProfileEntity)

    suspend fun deleteProfileById(profileId: Long)

    suspend fun setDefaultProfileForExercise(
        exerciseId: Long,
        profileId: Long,
        updatedAtEpochMillis: Long,
    )
}