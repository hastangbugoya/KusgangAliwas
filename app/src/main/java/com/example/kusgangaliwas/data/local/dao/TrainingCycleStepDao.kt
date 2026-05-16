package com.example.kusgangaliwas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kusgangaliwas.data.local.entity.TrainingCycleStepEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for ordered split steps inside a training cycle.
 *
 * Cycles are day-agnostic. This DAO manages only the cycle definition/order,
 * not runtime cycle progress.
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

    @Query(
        """
        SELECT *
        FROM training_cycle_step
        WHERE id = :stepId
        LIMIT 1
        """
    )
    suspend fun getStepById(
        stepId: Long,
    ): TrainingCycleStepEntity?

    @Query(
        """
        SELECT *
        FROM training_cycle_step
        WHERE cycleId = :cycleId
            AND splitTemplateId = :splitTemplateId
        LIMIT 1
        """
    )
    suspend fun getStepForSplit(
        cycleId: Long,
        splitTemplateId: Long,
    ): TrainingCycleStepEntity?

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
            AND splitTemplateId = :splitTemplateId
        """
    )
    suspend fun deleteStepForSplit(
        cycleId: Long,
        splitTemplateId: Long,
    )

    @Query(
        """
        DELETE FROM training_cycle_step
        WHERE cycleId = :cycleId
        """
    )
    suspend fun deleteAllStepsForCycle(cycleId: Long)
}