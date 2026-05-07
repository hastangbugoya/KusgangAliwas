package com.example.kusgangaliwas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kusgangaliwas.data.local.entity.CycleCalendarAnchorEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for cycle → calendar anchors.
 *
 * Anchors define where a cycle "starts" on a calendar date.
 * Multiple anchors allow restarting the cycle at different dates.
 */
@Dao
interface CycleCalendarAnchorDao {

    @Query(
        """
        SELECT *
        FROM cycle_calendar_anchor
        WHERE cycleId = :cycleId
        ORDER BY anchorDateEpochDay ASC
        """
    )
    fun observeAnchorsForCycle(
        cycleId: Long,
    ): Flow<List<CycleCalendarAnchorEntity>>

    @Query(
        """
        SELECT *
        FROM cycle_calendar_anchor
        WHERE cycleId = :cycleId
        ORDER BY anchorDateEpochDay ASC
        """
    )
    suspend fun getAnchorsForCycle(
        cycleId: Long,
    ): List<CycleCalendarAnchorEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAnchor(entity: CycleCalendarAnchorEntity): Long

    /**
     * Replace or "restart" anchor on a specific date.
     *
     * Since we enforce unique (cycleId, anchorDateEpochDay),
     * inserting a new one for the same date should overwrite.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAnchor(entity: CycleCalendarAnchorEntity)

    @Query(
        """
        DELETE FROM cycle_calendar_anchor
        WHERE id = :anchorId
        """
    )
    suspend fun deleteAnchor(anchorId: Long)

    @Query(
        """
        DELETE FROM cycle_calendar_anchor
        WHERE cycleId = :cycleId
        """
    )
    suspend fun deleteAllAnchorsForCycle(cycleId: Long)
}