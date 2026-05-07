package com.example.kusgangaliwas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kusgangaliwas.data.local.entity.TrainingCycleEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for training cycles.
 *
 * A cycle defines the suggested sequence of training steps.
 * It does not enforce dates or completion.
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
        WHERE id = :cycleId
        LIMIT 1
        """
    )
    suspend fun getCycleById(cycleId: Long): TrainingCycleEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCycle(entity: TrainingCycleEntity): Long

    @Update
    suspend fun updateCycle(entity: TrainingCycleEntity)

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
}