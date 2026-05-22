package com.example.kusgangaliwas.data.repository

import com.example.kusgangaliwas.data.local.dao.ExercisePaceProfileDao
import com.example.kusgangaliwas.data.local.entity.ExercisePaceProfileEntity
import com.example.kusgangaliwas.domain.repository.ExercisePaceProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room-backed repository for exercise pace profiles.
 *
 * This implementation keeps the "one default pace profile per exercise"
 * app-level rule centralized so callers do not need to manually clear older
 * defaults before saving a new default profile.
 *
 * Pace names may be reused across exercises. For example, multiple exercises
 * can each define their own "Speed" profile with exercise-specific timing.
 */
@Singleton
class ExercisePaceProfileRepositoryImpl @Inject constructor(
    private val exercisePaceProfileDao: ExercisePaceProfileDao,
) : ExercisePaceProfileRepository {

    override fun observeProfilesForExercise(
        exerciseId: Long,
    ): Flow<List<ExercisePaceProfileEntity>> {
        return exercisePaceProfileDao.observeProfilesForExercise(exerciseId)
    }

    override suspend fun getProfilesForExercise(
        exerciseId: Long,
    ): List<ExercisePaceProfileEntity> {
        return exercisePaceProfileDao.getProfilesForExercise(exerciseId)
    }

    override suspend fun getProfileById(
        profileId: Long,
    ): ExercisePaceProfileEntity? {
        return exercisePaceProfileDao.getProfileById(profileId)
    }

    override suspend fun getProfileForExerciseByName(
        exerciseId: Long,
        name: String,
    ): ExercisePaceProfileEntity? {
        val cleanedName = name.trim()

        if (cleanedName.isBlank()) {
            return null
        }

        return exercisePaceProfileDao.getProfileForExerciseByName(
            exerciseId = exerciseId,
            name = cleanedName,
        )
    }

    override suspend fun getProfilesForExercisesByName(
        exerciseIds: List<Long>,
        name: String,
    ): List<ExercisePaceProfileEntity> {
        val cleanedName = name.trim()
        val validExerciseIds = exerciseIds
            .filter { it > 0L }
            .distinct()

        if (cleanedName.isBlank() || validExerciseIds.isEmpty()) {
            return emptyList()
        }

        return exercisePaceProfileDao.getProfilesForExercisesByName(
            exerciseIds = validExerciseIds,
            name = cleanedName,
        )
    }

    override suspend fun getDefaultProfileForExercise(
        exerciseId: Long,
    ): ExercisePaceProfileEntity? {
        return exercisePaceProfileDao.getDefaultProfileForExercise(exerciseId)
    }

    override suspend fun getEnabledProfilesForExercise(
        exerciseId: Long,
    ): List<ExercisePaceProfileEntity> {
        return exercisePaceProfileDao.getEnabledProfilesForExercise(exerciseId)
    }

    override suspend fun insertProfile(
        profile: ExercisePaceProfileEntity,
    ): Long {
        val insertedId = exercisePaceProfileDao.insert(profile)

        if (profile.isDefault) {
            exercisePaceProfileDao.setDefaultProfileForExercise(
                exerciseId = profile.exerciseId,
                profileId = insertedId,
                updatedAtEpochMillis = profile.updatedAtEpochMillis,
            )
        }

        return insertedId
    }

    override suspend fun updateProfile(
        profile: ExercisePaceProfileEntity,
    ) {
        exercisePaceProfileDao.update(profile)

        if (profile.isDefault) {
            exercisePaceProfileDao.setDefaultProfileForExercise(
                exerciseId = profile.exerciseId,
                profileId = profile.id,
                updatedAtEpochMillis = profile.updatedAtEpochMillis,
            )
        }
    }

    override suspend fun deleteProfile(
        profile: ExercisePaceProfileEntity,
    ) {
        exercisePaceProfileDao.delete(profile)
    }

    override suspend fun deleteProfileById(
        profileId: Long,
    ) {
        exercisePaceProfileDao.deleteById(profileId)
    }

    override suspend fun setDefaultProfileForExercise(
        exerciseId: Long,
        profileId: Long,
        updatedAtEpochMillis: Long,
    ) {
        exercisePaceProfileDao.setDefaultProfileForExercise(
            exerciseId = exerciseId,
            profileId = profileId,
            updatedAtEpochMillis = updatedAtEpochMillis,
        )
    }
}