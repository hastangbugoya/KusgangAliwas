package com.example.kusgangaliwas.domain.repository

import com.example.kusgangaliwas.data.local.entity.CycleCalendarAnchorEntity
import com.example.kusgangaliwas.data.local.entity.TrainingCycleEntity
import com.example.kusgangaliwas.data.local.entity.TrainingCycleStepEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository boundary for training cycles and their calendar anchoring.
 *
 * Cycles define sequence:
 * A → Rest → B → Rest → C → ...
 *
 * Anchors map that sequence onto actual calendar dates.
 */
interface TrainingCycleRepository {

    // ----------------------------
    // Cycles
    // ----------------------------

    fun observeActiveCycles(): Flow<List<TrainingCycleEntity>>

    fun observeAllCycles(): Flow<List<TrainingCycleEntity>>

    suspend fun getCycleById(cycleId: Long): TrainingCycleEntity?

    suspend fun insertCycle(entity: TrainingCycleEntity): Long

    suspend fun updateCycle(entity: TrainingCycleEntity)

    suspend fun softDeleteCycle(
        cycleId: Long,
        updatedAtEpochMillis: Long,
    )

    // ----------------------------
    // Cycle Steps
    // ----------------------------

    fun observeStepsForCycle(
        cycleId: Long,
    ): Flow<List<TrainingCycleStepEntity>>

    suspend fun getStepsForCycle(
        cycleId: Long,
    ): List<TrainingCycleStepEntity>

    suspend fun insertStep(entity: TrainingCycleStepEntity): Long

    suspend fun insertSteps(entities: List<TrainingCycleStepEntity>)

    suspend fun updateStep(entity: TrainingCycleStepEntity)

    suspend fun updateSteps(entities: List<TrainingCycleStepEntity>)

    suspend fun deleteStep(stepId: Long)

    suspend fun deleteAllStepsForCycle(cycleId: Long)

    // ----------------------------
    // Calendar Anchors
    // ----------------------------

    fun observeAnchorsForCycle(
        cycleId: Long,
    ): Flow<List<CycleCalendarAnchorEntity>>

    suspend fun getAnchorsForCycle(
        cycleId: Long,
    ): List<CycleCalendarAnchorEntity>

    suspend fun insertAnchor(entity: CycleCalendarAnchorEntity): Long

    suspend fun upsertAnchor(entity: CycleCalendarAnchorEntity)

    suspend fun deleteAnchor(anchorId: Long)

    suspend fun deleteAllAnchorsForCycle(cycleId: Long)
}