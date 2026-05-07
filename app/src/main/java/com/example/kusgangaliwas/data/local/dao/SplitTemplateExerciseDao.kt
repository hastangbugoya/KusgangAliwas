package com.example.kusgangaliwas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kusgangaliwas.data.local.entity.SplitTemplateExerciseEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for exercises inside a split template (roadmap).
 *
 * Responsibilities:
 * - manage exercises within a split
 * - maintain suggested order
 * - allow reordering and updates
 */
@Dao
interface SplitTemplateExerciseDao {

    @Query(
        """
        SELECT *
        FROM split_template_exercise
        WHERE splitTemplateId = :splitTemplateId
        ORDER BY suggestedOrder ASC
        """
    )
    fun observeExercisesForSplit(
        splitTemplateId: Long,
    ): Flow<List<SplitTemplateExerciseEntity>>

    @Query(
        """
        SELECT *
        FROM split_template_exercise
        WHERE splitTemplateId = :splitTemplateId
        ORDER BY suggestedOrder ASC
        """
    )
    suspend fun getExercisesForSplit(
        splitTemplateId: Long,
    ): List<SplitTemplateExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSplitExercise(entity: SplitTemplateExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSplitExercises(entities: List<SplitTemplateExerciseEntity>)

    @Update
    suspend fun updateSplitExercise(entity: SplitTemplateExerciseEntity)

    @Query(
        """
        DELETE FROM split_template_exercise
        WHERE id = :id
        """
    )
    suspend fun deleteSplitExercise(id: Long)

    @Query(
        """
        DELETE FROM split_template_exercise
        WHERE splitTemplateId = :splitTemplateId
        """
    )
    suspend fun deleteAllForSplit(splitTemplateId: Long)

    /**
     * Bulk update for reordering.
     */
    @Update
    suspend fun updateSplitExercises(entities: List<SplitTemplateExerciseEntity>)
}