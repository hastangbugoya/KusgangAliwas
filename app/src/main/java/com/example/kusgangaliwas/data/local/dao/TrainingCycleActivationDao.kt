package com.example.kusgangaliwas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kusgangaliwas.data.local.entity.TrainingCycleActivationEntity

@Dao
interface TrainingCycleActivationDao {

    @Query(
        """
        SELECT *
        FROM training_cycle_activation
        WHERE cycleId = :cycleId
            AND deactivatedDateEpochDay IS NULL
        ORDER BY activatedDateEpochDay DESC, id DESC
        LIMIT 1
        """
    )
    suspend fun getActiveActivationForCycle(
        cycleId: Long,
    ): TrainingCycleActivationEntity?

    @Query(
        """
        SELECT *
        FROM training_cycle_activation
        WHERE cycleId = :cycleId
        ORDER BY activatedDateEpochDay DESC, id DESC
        LIMIT 1
        """
    )
    suspend fun getLatestActivationForCycle(
        cycleId: Long,
    ): TrainingCycleActivationEntity?

    @Query(
        """
        SELECT *
        FROM training_cycle_activation
        WHERE cycleId = :cycleId
        ORDER BY activatedDateEpochDay ASC, id ASC
        """
    )
    suspend fun getActivationsForCycle(
        cycleId: Long,
    ): List<TrainingCycleActivationEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertActivation(
        entity: TrainingCycleActivationEntity,
    ): Long

    @Update
    suspend fun updateActivation(
        entity: TrainingCycleActivationEntity,
    )

    @Query(
        """
        UPDATE training_cycle_activation
        SET deactivatedDateEpochDay = :deactivatedDateEpochDay,
            updatedAtEpochMillis = :updatedAtEpochMillis
        WHERE cycleId = :cycleId
            AND deactivatedDateEpochDay IS NULL
        """
    )
    suspend fun deactivateActiveActivationForCycle(
        cycleId: Long,
        deactivatedDateEpochDay: Long,
        updatedAtEpochMillis: Long,
    )
}