package com.example.kusgangaliwas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kusgangaliwas.data.local.entity.PlannedSessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for planned calendar sessions.
 *
 * Planned sessions represent intent:
 * - what was scheduled
 * - what date it was scheduled for
 * - whether it came from manual planning or cycle generation
 *
 * Actual performance is stored separately in ActualSessionEntity.
 */
@Dao
interface PlannedSessionDao {

    @Query(
        """
        SELECT *
        FROM planned_session
        WHERE scheduledDateEpochDay BETWEEN :startEpochDay AND :endEpochDay
        ORDER BY scheduledDateEpochDay ASC, id ASC
        """
    )
    fun observeSessionsBetweenDates(
        startEpochDay: Long,
        endEpochDay: Long,
    ): Flow<List<PlannedSessionEntity>>

    @Query(
        """
        SELECT *
        FROM planned_session
        WHERE scheduledDateEpochDay = :epochDay
        ORDER BY id ASC
        """
    )
    fun observeSessionsForDate(epochDay: Long): Flow<List<PlannedSessionEntity>>

    @Query(
        """
        SELECT *
        FROM planned_session
        WHERE id = :plannedSessionId
        LIMIT 1
        """
    )
    suspend fun getPlannedSessionById(
        plannedSessionId: Long,
    ): PlannedSessionEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPlannedSession(entity: PlannedSessionEntity): Long

    @Update
    suspend fun updatePlannedSession(entity: PlannedSessionEntity)

    @Query(
        """
        UPDATE planned_session
        SET status = :status,
            updatedAtEpochMillis = :updatedAtEpochMillis
        WHERE id = :plannedSessionId
        """
    )
    suspend fun updateStatus(
        plannedSessionId: Long,
        status: String,
        updatedAtEpochMillis: Long,
    )

    @Query(
        """
        DELETE FROM planned_session
        WHERE id = :plannedSessionId
        """
    )
    suspend fun deletePlannedSession(plannedSessionId: Long)
}