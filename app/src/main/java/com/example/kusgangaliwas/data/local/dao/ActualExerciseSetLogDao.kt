package com.example.kusgangaliwas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kusgangaliwas.data.local.entity.ActualExerciseSetLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for sets within an exercise log.
 *
 * This supports basic tracking:
 * - weight
 * - reps
 * - duration/distance
 *
 * No analytics yet (future).
 */
@Dao
interface ActualExerciseSetLogDao {

    @Query(
        """
        SELECT *
        FROM actual_exercise_set_log
        WHERE actualExerciseLogId = :actualExerciseLogId
        ORDER BY setOrder ASC, id ASC
        """
    )
    fun observeSetsForExercise(
        actualExerciseLogId: Long,
    ): Flow<List<ActualExerciseSetLogEntity>>

    @Query(
        """
        SELECT *
        FROM actual_exercise_set_log
        WHERE actualExerciseLogId = :actualExerciseLogId
        ORDER BY setOrder ASC, id ASC
        """
    )
    suspend fun getSetsForExercise(
        actualExerciseLogId: Long,
    ): List<ActualExerciseSetLogEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSet(entity: ActualExerciseSetLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSets(entities: List<ActualExerciseSetLogEntity>)

    @Update
    suspend fun updateSet(entity: ActualExerciseSetLogEntity)

    @Update
    suspend fun updateSets(entities: List<ActualExerciseSetLogEntity>)

    @Query(
        """
        DELETE FROM actual_exercise_set_log
        WHERE id = :setId
        """
    )
    suspend fun deleteSet(setId: Long)

    @Query(
        """
        DELETE FROM actual_exercise_set_log
        WHERE actualExerciseLogId = :actualExerciseLogId
        """
    )
    suspend fun deleteAllForExercise(actualExerciseLogId: Long)
}