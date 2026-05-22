package com.example.kusgangaliwas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.kusgangaliwas.data.local.entity.PlannedSessionExerciseEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for planned exercise items inside a planned session.
 *
 * These items represent roadmap intent, including:
 * - exercises copied from a split template
 * - manually added planned items
 * - carried-forward missed/skipped items
 */
@Dao
interface PlannedSessionExerciseDao {

    @Query(
        """
        SELECT *
        FROM planned_session_exercise
        WHERE plannedSessionId = :plannedSessionId
        ORDER BY suggestedOrder ASC, id ASC
        """
    )
    fun observeExercisesForPlannedSession(
        plannedSessionId: Long,
    ): Flow<List<PlannedSessionExerciseEntity>>

    @Query(
        """
        SELECT *
        FROM planned_session_exercise
        WHERE plannedSessionId = :plannedSessionId
        ORDER BY suggestedOrder ASC, id ASC
        """
    )
    suspend fun getExercisesForPlannedSession(
        plannedSessionId: Long,
    ): List<PlannedSessionExerciseEntity>

    @Query(
        """
        SELECT *
        FROM planned_session_exercise
        WHERE id = :plannedSessionExerciseId
        LIMIT 1
        """
    )
    suspend fun getPlannedSessionExerciseById(
        plannedSessionExerciseId: Long,
    ): PlannedSessionExerciseEntity?

    @Query(
        """
        SELECT *
        FROM planned_session_exercise
        WHERE status IN ('missed', 'intentionallySkipped')
        ORDER BY id DESC
        """
    )
    suspend fun getCarryForwardCandidates(): List<PlannedSessionExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPlannedSessionExercise(
        entity: PlannedSessionExerciseEntity,
    ): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPlannedSessionExercises(
        entities: List<PlannedSessionExerciseEntity>,
    )

    @Upsert
    suspend fun upsertPlannedSessionExercise(
        entity: PlannedSessionExerciseEntity,
    ): Long

    @Upsert
    suspend fun upsertPlannedSessionExercises(
        entities: List<PlannedSessionExerciseEntity>,
    )

    @Update
    suspend fun updatePlannedSessionExercise(
        entity: PlannedSessionExerciseEntity,
    )

    @Update
    suspend fun updatePlannedSessionExercises(
        entities: List<PlannedSessionExerciseEntity>,
    )

    @Query(
        """
        UPDATE planned_session_exercise
        SET status = :status
        WHERE id = :plannedSessionExerciseId
        """
    )
    suspend fun updateStatus(
        plannedSessionExerciseId: Long,
        status: String,
    )

    @Query(
        """
        DELETE FROM planned_session_exercise
        WHERE id = :plannedSessionExerciseId
        """
    )
    suspend fun deletePlannedSessionExercise(
        plannedSessionExerciseId: Long,
    )

    @Query(
        """
        DELETE FROM planned_session_exercise
        WHERE plannedSessionId = :plannedSessionId
        """
    )
    suspend fun deleteAllForPlannedSession(
        plannedSessionId: Long,
    )
}