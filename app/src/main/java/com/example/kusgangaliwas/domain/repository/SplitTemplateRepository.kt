package com.example.kusgangaliwas.domain.repository

import com.example.kusgangaliwas.data.local.entity.SplitTemplateEntity
import com.example.kusgangaliwas.data.local.entity.SplitTemplateExerciseEntity
import com.example.kusgangaliwas.data.local.entity.SplitTemplateMuscleGroupCrossRef
import com.example.kusgangaliwas.data.local.model.SplitTemplateSummaryRow
import kotlinx.coroutines.flow.Flow

/**
 * Repository boundary for split templates and their roadmap exercises.
 *
 * Splits define reusable session plans (what to do),
 * independent of scheduling (when to do it).
 */
interface SplitTemplateRepository {

    // ----------------------------
    // Split Templates
    // ----------------------------

    fun observeActiveSplits(): Flow<List<SplitTemplateEntity>>

    fun observeAllSplits(): Flow<List<SplitTemplateEntity>>

    suspend fun getSplitById(splitId: Long): SplitTemplateEntity?

    suspend fun insertSplit(entity: SplitTemplateEntity): Long

    suspend fun updateSplit(entity: SplitTemplateEntity)

    suspend fun softDeleteSplit(
        splitId: Long,
        updatedAtEpochMillis: Long,
    )

    suspend fun getActiveSplitSummaries(): List<SplitTemplateSummaryRow>

    // ----------------------------
    // Split Exercises (Roadmap)
    // ----------------------------

    fun observeExercisesForSplit(
        splitTemplateId: Long,
    ): Flow<List<SplitTemplateExerciseEntity>>

    suspend fun getExercisesForSplit(
        splitTemplateId: Long,
    ): List<SplitTemplateExerciseEntity>

    suspend fun getSplitExerciseById(
        id: Long,
    ): SplitTemplateExerciseEntity?

    suspend fun insertSplitExercise(
        entity: SplitTemplateExerciseEntity,
    ): Long

    suspend fun insertSplitExercises(
        entities: List<SplitTemplateExerciseEntity>,
    )

    suspend fun updateSplitExercise(
        entity: SplitTemplateExerciseEntity,
    )

    suspend fun updatePaceProfileForSplitExercise(
        splitTemplateExerciseId: Long,
        paceProfileId: Long?,
    )

    suspend fun updateSplitExercises(
        entities: List<SplitTemplateExerciseEntity>,
    )

    suspend fun deleteSplitExercise(id: Long)

    suspend fun deleteAllExercisesForSplit(splitTemplateId: Long)

    // ----------------------------
    // Split Muscle Groups
    // ----------------------------

    fun observeMuscleGroupsForSplit(
        splitTemplateId: Long,
    ): Flow<List<SplitTemplateMuscleGroupCrossRef>>

    suspend fun upsertSplitMuscleGroup(
        crossRef: SplitTemplateMuscleGroupCrossRef,
    )

    suspend fun deleteSplitMuscleGroup(
        splitTemplateId: Long,
        muscleGroupId: Long,
    )
}