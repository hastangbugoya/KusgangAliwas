package com.example.kusgangaliwas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kusgangaliwas.data.local.entity.ActualExerciseLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for actual exercise logs inside a performed session.
 *
 * These records represent what actually happened:
 * - planned exercise performed
 * - substitution
 * - impromptu exercise
 * - loose note / stray exercise
 */
@Dao
interface ActualExerciseLogDao {

    @Query(
        """
        SELECT *
        FROM actual_exercise_log
        WHERE actualSessionId = :actualSessionId
        ORDER BY logOrder ASC, id ASC
        """
    )
    fun observeLogsForSession(
        actualSessionId: Long,
    ): Flow<List<ActualExerciseLogEntity>>

    @Query(
        """
        SELECT *
        FROM actual_exercise_log
        WHERE actualSessionId = :actualSessionId
        ORDER BY logOrder ASC, id ASC
        """
    )
    suspend fun getLogsForSession(
        actualSessionId: Long,
    ): List<ActualExerciseLogEntity>

    @Query(
        """
        SELECT *
        FROM actual_exercise_log
        WHERE id = :actualExerciseLogId
        LIMIT 1
        """
    )
    suspend fun getById(
        actualExerciseLogId: Long,
    ): ActualExerciseLogEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertLog(entity: ActualExerciseLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertLogs(entities: List<ActualExerciseLogEntity>)

    @Update
    suspend fun updateLog(entity: ActualExerciseLogEntity)

    @Update
    suspend fun updateLogs(entities: List<ActualExerciseLogEntity>)

    @Query(
        """
        DELETE FROM actual_exercise_log
        WHERE id = :actualExerciseLogId
        """
    )
    suspend fun deleteLog(actualExerciseLogId: Long)

    @Query(
        """
        DELETE FROM actual_exercise_log
        WHERE actualSessionId = :actualSessionId
        """
    )
    suspend fun deleteAllForSession(actualSessionId: Long)

    @Query(
        """
    SELECT *
    FROM actual_exercise_log
    WHERE exerciseId = :exerciseId
        AND id IN (
            SELECT actualExerciseLogId
            FROM actual_exercise_set_log
        )
    ORDER BY performedAtEpochMillis DESC, id DESC
    """
    )
    suspend fun getLogsForExercise(
        exerciseId: Long,
    ): List<ActualExerciseLogEntity>
}