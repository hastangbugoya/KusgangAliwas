package com.example.kusgangaliwas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kusgangaliwas.data.local.entity.TrainingCycleProgressEventEntity

/**
 * DAO for non-session cycle progress events.
 */
@Dao
interface TrainingCycleProgressEventDao {

    @Query(
        """
        SELECT *
        FROM training_cycle_progress_event
        WHERE trainingCycleId = :trainingCycleId
        ORDER BY eventDateEpochDay ASC, id ASC
        """
    )
    suspend fun getEventsForCycle(
        trainingCycleId: Long,
    ): List<TrainingCycleProgressEventEntity>

    @Query(
        """
        SELECT *
        FROM training_cycle_progress_event
        WHERE trainingCycleId = :trainingCycleId
        ORDER BY eventDateEpochDay DESC, id DESC
        LIMIT 1
        """
    )
    suspend fun getLatestEventForCycle(
        trainingCycleId: Long,
    ): TrainingCycleProgressEventEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEvent(
        entity: TrainingCycleProgressEventEntity,
    ): Long

    @Query(
        """
        DELETE FROM training_cycle_progress_event
        WHERE id = :eventId
        """
    )
    suspend fun deleteEvent(
        eventId: Long,
    )

    @Query(
        """
        DELETE FROM training_cycle_progress_event
        WHERE trainingCycleId = :trainingCycleId
        """
    )
    suspend fun deleteEventsForCycle(
        trainingCycleId: Long,
    )
}