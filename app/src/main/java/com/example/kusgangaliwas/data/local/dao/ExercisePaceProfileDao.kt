package com.example.kusgangaliwas.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.kusgangaliwas.data.local.entity.ExercisePaceProfileEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for exercise-specific pace profiles.
 *
 * Pace profiles are optional named timing presets for an exercise. They are
 * intentionally stored per exercise so the same exercise can be used with
 * different pacing in different split contexts, such as treadmill warmup pace
 * versus treadmill full-cardio pace.
 */
@Dao
interface ExercisePaceProfileDao {
    @Query(
        """
        SELECT *
        FROM exercise_pace_profile
        WHERE exerciseId = :exerciseId
        ORDER BY isDefault DESC, name COLLATE NOCASE ASC, id ASC
        """
    )
    fun observeProfilesForExercise(exerciseId: Long): Flow<List<ExercisePaceProfileEntity>>

    @Query(
        """
        SELECT *
        FROM exercise_pace_profile
        WHERE exerciseId = :exerciseId
        ORDER BY isDefault DESC, name COLLATE NOCASE ASC, id ASC
        """
    )
    suspend fun getProfilesForExercise(exerciseId: Long): List<ExercisePaceProfileEntity>

    @Query(
        """
        SELECT *
        FROM exercise_pace_profile
        WHERE id = :profileId
        LIMIT 1
        """
    )
    suspend fun getProfileById(profileId: Long): ExercisePaceProfileEntity?

    @Query(
        """
        SELECT *
        FROM exercise_pace_profile
        WHERE exerciseId = :exerciseId
            AND name = :name COLLATE NOCASE
        ORDER BY isDefault DESC, updatedAtEpochMillis DESC, id DESC
        LIMIT 1
        """
    )
    suspend fun getProfileForExerciseByName(
        exerciseId: Long,
        name: String,
    ): ExercisePaceProfileEntity?

    @Query(
        """
        SELECT *
        FROM exercise_pace_profile
        WHERE exerciseId IN (:exerciseIds)
            AND name = :name COLLATE NOCASE
        ORDER BY exerciseId ASC, isDefault DESC, updatedAtEpochMillis DESC, id DESC
        """
    )
    suspend fun getProfilesForExercisesByName(
        exerciseIds: List<Long>,
        name: String,
    ): List<ExercisePaceProfileEntity>

    @Query(
        """
        SELECT *
        FROM exercise_pace_profile
        WHERE exerciseId = :exerciseId
            AND isDefault = 1
        ORDER BY updatedAtEpochMillis DESC, id DESC
        LIMIT 1
        """
    )
    suspend fun getDefaultProfileForExercise(exerciseId: Long): ExercisePaceProfileEntity?

    @Query(
        """
        SELECT *
        FROM exercise_pace_profile
        WHERE exerciseId = :exerciseId
            AND isEnabled = 1
        ORDER BY isDefault DESC, name COLLATE NOCASE ASC, id ASC
        """
    )
    suspend fun getEnabledProfilesForExercise(exerciseId: Long): List<ExercisePaceProfileEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(profile: ExercisePaceProfileEntity): Long

    @Update
    suspend fun update(profile: ExercisePaceProfileEntity)

    @Delete
    suspend fun delete(profile: ExercisePaceProfileEntity)

    @Query(
        """
        DELETE FROM exercise_pace_profile
        WHERE id = :profileId
        """
    )
    suspend fun deleteById(profileId: Long)

    @Query(
        """
        UPDATE exercise_pace_profile
        SET isDefault = 0,
            updatedAtEpochMillis = :updatedAtEpochMillis
        WHERE exerciseId = :exerciseId
            AND isDefault = 1
        """
    )
    suspend fun clearDefaultForExercise(
        exerciseId: Long,
        updatedAtEpochMillis: Long,
    )

    @Query(
        """
        UPDATE exercise_pace_profile
        SET isDefault = 1,
            updatedAtEpochMillis = :updatedAtEpochMillis
        WHERE id = :profileId
            AND exerciseId = :exerciseId
        """
    )
    suspend fun markProfileAsDefault(
        exerciseId: Long,
        profileId: Long,
        updatedAtEpochMillis: Long,
    )

    /**
     * Marks one profile as the default for an exercise.
     *
     * The entity allows multiple rows to contain isDefault=true, but app code
     * should use this transaction so normal writes keep one default profile per
     * exercise.
     */
    @Transaction
    suspend fun setDefaultProfileForExercise(
        exerciseId: Long,
        profileId: Long,
        updatedAtEpochMillis: Long,
    ) {
        clearDefaultForExercise(
            exerciseId = exerciseId,
            updatedAtEpochMillis = updatedAtEpochMillis,
        )
        markProfileAsDefault(
            exerciseId = exerciseId,
            profileId = profileId,
            updatedAtEpochMillis = updatedAtEpochMillis,
        )
    }
}