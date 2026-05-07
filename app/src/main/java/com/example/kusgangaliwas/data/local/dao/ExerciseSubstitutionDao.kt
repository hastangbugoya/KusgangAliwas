package com.example.kusgangaliwas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kusgangaliwas.data.local.entity.ExerciseSubstitutionEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for user-defined exercise substitutions.
 *
 * These are explicit user preferences, separate from automatic
 * muscle-group-based alternative suggestions.
 */
@Dao
interface ExerciseSubstitutionDao {

    @Query(
        """
        SELECT *
        FROM exercise_substitution
        WHERE sourceExerciseId = :sourceExerciseId
        AND isActive = 1
        ORDER BY id ASC
        """
    )
    fun observeActiveSubstitutionsForExercise(
        sourceExerciseId: Long,
    ): Flow<List<ExerciseSubstitutionEntity>>

    @Query(
        """
        SELECT *
        FROM exercise_substitution
        WHERE sourceExerciseId = :sourceExerciseId
        ORDER BY id ASC
        """
    )
    fun observeAllSubstitutionsForExercise(
        sourceExerciseId: Long,
    ): Flow<List<ExerciseSubstitutionEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSubstitution(entity: ExerciseSubstitutionEntity): Long

    @Update
    suspend fun updateSubstitution(entity: ExerciseSubstitutionEntity)

    @Query(
        """
        UPDATE exercise_substitution
        SET isActive = 0
        WHERE id = :substitutionId
        """
    )
    suspend fun softDeleteSubstitution(substitutionId: Long)

    @Query(
        """
        DELETE FROM exercise_substitution
        WHERE sourceExerciseId = :sourceExerciseId
        AND substituteExerciseId = :substituteExerciseId
        """
    )
    suspend fun deleteSubstitutionPair(
        sourceExerciseId: Long,
        substituteExerciseId: Long,
    )
}