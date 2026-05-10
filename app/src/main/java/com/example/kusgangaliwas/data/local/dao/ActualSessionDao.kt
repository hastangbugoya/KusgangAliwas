package com.example.kusgangaliwas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kusgangaliwas.data.local.entity.ActualSessionEntity
import com.example.kusgangaliwas.data.local.model.ExerciseWeightSuggestion
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

    /**
     * Returns the latest historical max weight suggestion for an exercise.
     *
     * V1 behavior:
     * - looks at the most recent previous session containing the exercise
     * - returns the maximum logged weight from that session
     *
     * This intentionally favors simplicity over advanced analytics.
     */
    @Query(
        """
    SELECT
        ael.exerciseId AS exerciseId,
        NULL AS exerciseName,
        s.id AS sourceActualSessionId,
        s.performedDateEpochDay AS sourcePerformedDateEpochDay,
        sets.weight AS suggestedWeight
    FROM actual_session s
    INNER JOIN actual_exercise_log ael
        ON ael.actualSessionId = s.id
    INNER JOIN actual_exercise_set_log sets
        ON sets.actualExerciseLogId = ael.id
    WHERE ael.exerciseId = :exerciseId
        AND sets.weight IS NOT NULL
        AND s.id = (
            SELECT s2.id
            FROM actual_session s2
            INNER JOIN actual_exercise_log ael2
                ON ael2.actualSessionId = s2.id
            INNER JOIN actual_exercise_set_log sets2
                ON sets2.actualExerciseLogId = ael2.id
            WHERE ael2.exerciseId = :exerciseId
                AND sets2.weight IS NOT NULL
            ORDER BY s2.performedDateEpochDay DESC, s2.id DESC
            LIMIT 1
        )
    ORDER BY sets.weight DESC, sets.id DESC
    LIMIT 1
    """
    )
    suspend fun getLatestWeightSuggestionForExercise(
        exerciseId: Long,
    ): ExerciseWeightSuggestion?

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

    @Query(
        """
        SELECT *
        FROM actual_session
        WHERE id = :actualSessionId
        LIMIT 1
        """
    )
    fun observeById(
        actualSessionId: Long,
    ): Flow<ActualSessionEntity?>
}