package com.example.kusgangaliwas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kusgangaliwas.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for the exercise library.
 *
 * V1 keeps operations simple:
 * - create/update exercises
 * - observe active exercises
 * - look up exercises by id
 * - soft-disable exercises instead of deleting them
 */
@Dao
interface ExerciseDao {

    @Query(
        """
        SELECT *
        FROM exercise
        WHERE isActive = 1
        ORDER BY name COLLATE NOCASE ASC
        """
    )
    fun observeActiveExercises(): Flow<List<ExerciseEntity>>

    @Query(
        """
        SELECT *
        FROM exercise
        ORDER BY name COLLATE NOCASE ASC
        """
    )
    fun observeAllExercises(): Flow<List<ExerciseEntity>>

    @Query(
        """
        SELECT *
        FROM exercise
        WHERE id = :exerciseId
        LIMIT 1
        """
    )
    suspend fun getExerciseById(exerciseId: Long): ExerciseEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExercise(entity: ExerciseEntity): Long

    @Update
    suspend fun updateExercise(entity: ExerciseEntity)

    @Query(
        """
        UPDATE exercise
        SET isActive = 0,
            updatedAtEpochMillis = :updatedAtEpochMillis
        WHERE id = :exerciseId
        """
    )
    suspend fun softDeleteExercise(
        exerciseId: Long,
        updatedAtEpochMillis: Long,
    )
}