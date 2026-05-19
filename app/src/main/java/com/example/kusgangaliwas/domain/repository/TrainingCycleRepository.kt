package com.example.kusgangaliwas.domain.repository

import com.example.kusgangaliwas.data.local.entity.CycleCalendarAnchorEntity
import com.example.kusgangaliwas.data.local.entity.TrainingCycleEntity
import com.example.kusgangaliwas.data.local.entity.TrainingCycleStepEntity
import kotlinx.coroutines.flow.Flow
import com.example.kusgangaliwas.data.local.entity.TrainingCycleActivationEntity

/**
 * Repository boundary for training cycles.
 *
 * A cycle is an ordered split queue. It is intentionally day-agnostic,
 * non-blocking, and recommendation-based.
 *
 * Calendar anchors are legacy/simple display helpers and should not drive
 * cycle progression.
 */
interface TrainingCycleRepository {

    // ----------------------------
    // Cycles
    // ----------------------------

    fun observeActiveCycles(): Flow<List<TrainingCycleEntity>>

    suspend fun getActiveCycles(): List<TrainingCycleEntity>

    fun observeAllCycles(): Flow<List<TrainingCycleEntity>>

    fun observeMostRecentlyUpdatedActiveCycle(): Flow<TrainingCycleEntity?>

    suspend fun getAnyActiveCycle(): TrainingCycleEntity?

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

    suspend fun getStepById(
        stepId: Long,
    ): TrainingCycleStepEntity?

    suspend fun getStepForSplit(
        cycleId: Long,
        splitTemplateId: Long,
    ): TrainingCycleStepEntity?

    suspend fun insertStep(entity: TrainingCycleStepEntity): Long

    suspend fun insertSteps(entities: List<TrainingCycleStepEntity>)

    suspend fun updateStep(entity: TrainingCycleStepEntity)

    suspend fun updateSteps(entities: List<TrainingCycleStepEntity>)

    suspend fun deleteStep(stepId: Long)

    suspend fun deleteStepForSplit(
        cycleId: Long,
        splitTemplateId: Long,
    )

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

    // ----------------------------
// Cycle Activations
// ----------------------------

    suspend fun getActiveActivationForCycle(
        cycleId: Long,
    ): TrainingCycleActivationEntity?

    suspend fun getLatestActivationForCycle(
        cycleId: Long,
    ): TrainingCycleActivationEntity?

    suspend fun getActivationsForCycle(
        cycleId: Long,
    ): List<TrainingCycleActivationEntity>

    suspend fun insertActivation(
        entity: TrainingCycleActivationEntity,
    ): Long

    suspend fun updateActivation(
        entity: TrainingCycleActivationEntity,
    )

    suspend fun deactivateActiveActivationForCycle(
        cycleId: Long,
        deactivatedDateEpochDay: Long,
        updatedAtEpochMillis: Long,
    )
}