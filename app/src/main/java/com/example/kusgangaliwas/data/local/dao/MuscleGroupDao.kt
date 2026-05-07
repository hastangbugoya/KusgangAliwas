package com.example.kusgangaliwas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kusgangaliwas.data.local.entity.MuscleGroupEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for muscle group definitions.
 *
 * Muscle groups are:
 * - user-facing, layman-friendly labels
 * - reusable across all exercises
 */
@Dao
interface MuscleGroupDao {

    @Query(
        """
        SELECT *
        FROM muscle_group
        WHERE isActive = 1
        ORDER BY sortOrder ASC, name COLLATE NOCASE ASC
        """
    )
    fun observeActiveMuscleGroups(): Flow<List<MuscleGroupEntity>>

    @Query(
        """
        SELECT *
        FROM muscle_group
        ORDER BY sortOrder ASC, name COLLATE NOCASE ASC
        """
    )
    fun observeAllMuscleGroups(): Flow<List<MuscleGroupEntity>>

    @Query(
        """
        SELECT *
        FROM muscle_group
        WHERE id = :muscleGroupId
        LIMIT 1
        """
    )
    suspend fun getMuscleGroupById(muscleGroupId: Long): MuscleGroupEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMuscleGroup(entity: MuscleGroupEntity): Long

    @Update
    suspend fun updateMuscleGroup(entity: MuscleGroupEntity)

    @Query(
        """
        UPDATE muscle_group
        SET isActive = 0
        WHERE id = :muscleGroupId
        """
    )
    suspend fun softDeleteMuscleGroup(muscleGroupId: Long)
}