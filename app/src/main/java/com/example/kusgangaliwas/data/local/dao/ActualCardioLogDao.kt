package com.example.kusgangaliwas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kusgangaliwas.data.local.entity.ActualCardioLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for cardio blocks within an actual session.
 *
 * Supports mixed session timelines like:
 * - warm-up cardio
 * - strength exercises
 * - cooldown cardio
 */
@Dao
interface ActualCardioLogDao {

    @Query(
        """
        SELECT *
        FROM actual_cardio_log
        WHERE actualSessionId = :actualSessionId
        ORDER BY logOrder ASC, id ASC
        """
    )
    fun observeCardioLogsForSession(
        actualSessionId: Long,
    ): Flow<List<ActualCardioLogEntity>>

    @Query(
        """
        SELECT *
        FROM actual_cardio_log
        WHERE actualSessionId = :actualSessionId
        ORDER BY logOrder ASC, id ASC
        """
    )
    suspend fun getCardioLogsForSession(
        actualSessionId: Long,
    ): List<ActualCardioLogEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCardioLog(entity: ActualCardioLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCardioLogs(entities: List<ActualCardioLogEntity>)

    @Update
    suspend fun updateCardioLog(entity: ActualCardioLogEntity)

    @Update
    suspend fun updateCardioLogs(entities: List<ActualCardioLogEntity>)

    @Query(
        """
        DELETE FROM actual_cardio_log
        WHERE id = :cardioLogId
        """
    )
    suspend fun deleteCardioLog(cardioLogId: Long)

    @Query(
        """
        DELETE FROM actual_cardio_log
        WHERE actualSessionId = :actualSessionId
        """
    )
    suspend fun deleteAllCardioLogsForSession(actualSessionId: Long)

    @Query(
        """
    SELECT *
    FROM actual_cardio_log
    WHERE exerciseId = :exerciseId
        AND (
            distance IS NOT NULL
            OR durationSeconds IS NOT NULL
            OR averageInclinePercent IS NOT NULL
            OR averageResistance IS NOT NULL
        )
    ORDER BY performedAtEpochMillis DESC, id DESC
    """
    )
    suspend fun getLogsForExercise(
        exerciseId: Long,
    ): List<ActualCardioLogEntity>
}