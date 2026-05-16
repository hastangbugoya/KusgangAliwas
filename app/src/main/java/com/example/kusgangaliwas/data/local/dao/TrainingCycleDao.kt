package com.example.kusgangaliwas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kusgangaliwas.data.local.entity.TrainingCycleEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for reusable training cycles.
 *
 * A cycle is an ordered split queue used for progression suggestions.
 *
 * Cycles are intentionally:
 * - day-agnostic
 * - non-blocking
 * - recommendation-based
 *
 * Runtime progress should be derived from cycle progress/session history,
 * not from mutable index state stored on the cycle itself.
 */
@Dao
interface TrainingCycleDao {

    @Query(
        """
        SELECT *
        FROM training_cycle
        WHERE isActive = 1
        ORDER BY name COLLATE NOCASE ASC
        """
    )
    fun observeActiveCycles(): Flow<List<TrainingCycleEntity>>

    @Query(
        """
        SELECT *
        FROM training_cycle
        ORDER BY name COLLATE NOCASE ASC
        """
    )
    fun observeAllCycles(): Flow<List<TrainingCycleEntity>>

    @Query(
        """
        SELECT *
        FROM training_cycle
        WHERE isActive = 1
        ORDER BY updatedAtEpochMillis DESC
        LIMIT 1
        """
    )
    fun observeMostRecentlyUpdatedActiveCycle():
            Flow<TrainingCycleEntity?>

    @Query(
        """
        SELECT *
        FROM training_cycle
        WHERE id = :cycleId
        LIMIT 1
        """
    )
    suspend fun getCycleById(
        cycleId: Long,
    ): TrainingCycleEntity?

    @Query(
        """
        SELECT *
        FROM training_cycle
        WHERE isActive = 1
        LIMIT 1
        """
    )
    suspend fun getAnyActiveCycle():
            TrainingCycleEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCycle(
        entity: TrainingCycleEntity,
    ): Long

    @Update
    suspend fun updateCycle(
        entity: TrainingCycleEntity,
    )

    @Query(
        """
        UPDATE training_cycle
        SET isActive = 0,
            updatedAtEpochMillis = :updatedAtEpochMillis
        WHERE id = :cycleId
        """
    )
    suspend fun softDeleteCycle(
        cycleId: Long,
        updatedAtEpochMillis: Long,
    )

    @Query(
        """
        UPDATE training_cycle
        SET isActive = 0,
            updatedAtEpochMillis = :updatedAtEpochMillis
        """
    )
    suspend fun deactivateAllCycles(
        updatedAtEpochMillis: Long,
    )
}