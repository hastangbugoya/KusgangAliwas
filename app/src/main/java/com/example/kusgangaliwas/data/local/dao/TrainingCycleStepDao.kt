package com.example.kusgangaliwas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kusgangaliwas.data.local.entity.TrainingCycleStepEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for steps inside a training cycle.
 *
 * Responsibilities:
 * - manage step order
 * - support split/rest/open steps
 */
@Dao
interface TrainingCycleStepDao {

    @Query(
        """
        SELECT *
        FROM training_cycle_step
        WHERE cycleId = :cycleId
        ORDER BY stepOrder ASC
        """
    )
    fun observeStepsForCycle(
        cycleId: Long,
    ): Flow<List<TrainingCycleStepEntity>>

    @Query(
        """
        SELECT *
        FROM training_cycle_step
        WHERE cycleId = :cycleId
        ORDER BY stepOrder ASC
        """
    )
    suspend fun getStepsForCycle(
        cycleId: Long,
    ): List<TrainingCycleStepEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertStep(entity: TrainingCycleStepEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSteps(entities: List<TrainingCycleStepEntity>)

    @Update
    suspend fun updateStep(entity: TrainingCycleStepEntity)

    @Update
    suspend fun updateSteps(entities: List<TrainingCycleStepEntity>)

    @Query(
        """
        DELETE FROM training_cycle_step
        WHERE id = :stepId
        """
    )
    suspend fun deleteStep(stepId: Long)

    @Query(
        """
        DELETE FROM training_cycle_step
        WHERE cycleId = :cycleId
        """
    )
    suspend fun deleteAllStepsForCycle(cycleId: Long)
}