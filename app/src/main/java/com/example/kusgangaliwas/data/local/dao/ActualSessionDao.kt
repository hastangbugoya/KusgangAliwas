package com.example.kusgangaliwas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kusgangaliwas.data.local.entity.ActualSessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for actual performed workout sessions.
 *
 * This represents what actually happened:
 * - performed date
 * - link to planned session (optional)
 * - session lifecycle (inProgress → completed)
 */
@Dao
interface ActualSessionDao {

    @Query(
        """
        SELECT *
        FROM actual_session
        ORDER BY performedDateEpochDay DESC, id DESC
        """
    )
    fun observeAllSessions(): Flow<List<ActualSessionEntity>>

    @Query(
        """
        SELECT *
        FROM actual_session
        WHERE performedDateEpochDay >= :startEpochDay
            AND performedDateEpochDay < :endEpochDay
        ORDER BY performedDateEpochDay DESC, id DESC
        """
    )
    fun observeSessionsBetweenDates(
        startEpochDay: Long,
        endEpochDay: Long,
    ): Flow<List<ActualSessionEntity>>

    @Query(
        """
        SELECT *
        FROM actual_session
        WHERE plannedSessionId = :plannedSessionId
        ORDER BY id DESC
        LIMIT 1
        """
    )
    suspend fun getLatestForPlannedSession(
        plannedSessionId: Long,
    ): ActualSessionEntity?

    @Query(
        """
        SELECT *
        FROM actual_session
        WHERE id = :actualSessionId
        LIMIT 1
        """
    )
    suspend fun getById(
        actualSessionId: Long,
    ): ActualSessionEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertActualSession(entity: ActualSessionEntity): Long

    @Update
    suspend fun updateActualSession(entity: ActualSessionEntity)

    @Query(
        """
        UPDATE actual_session
        SET status = :status,
            updatedAtEpochMillis = :updatedAtEpochMillis
        WHERE id = :actualSessionId
        """
    )
    suspend fun updateStatus(
        actualSessionId: Long,
        status: String,
        updatedAtEpochMillis: Long,
    )

    @Query(
        """
        DELETE FROM actual_session
        WHERE id = :actualSessionId
        """
    )
    suspend fun deleteActualSession(actualSessionId: Long)
}