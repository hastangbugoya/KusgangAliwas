package com.example.kusgangaliwas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kusgangaliwas.data.local.entity.SplitTemplateEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for split templates.
 *
 * Splits define a reusable grouping of exercises (the roadmap),
 * not the schedule or actual execution.
 */
@Dao
interface SplitTemplateDao {

    @Query(
        """
        SELECT *
        FROM split_template
        WHERE isActive = 1
        ORDER BY name COLLATE NOCASE ASC
        """
    )
    fun observeActiveSplits(): Flow<List<SplitTemplateEntity>>

    @Query(
        """
        SELECT *
        FROM split_template
        ORDER BY name COLLATE NOCASE ASC
        """
    )
    fun observeAllSplits(): Flow<List<SplitTemplateEntity>>

    @Query(
        """
        SELECT *
        FROM split_template
        WHERE id = :splitId
        LIMIT 1
        """
    )
    suspend fun getSplitById(splitId: Long): SplitTemplateEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSplit(entity: SplitTemplateEntity): Long

    @Update
    suspend fun updateSplit(entity: SplitTemplateEntity)

    @Query(
        """
        UPDATE split_template
        SET isActive = 0,
            updatedAtEpochMillis = :updatedAtEpochMillis
        WHERE id = :splitId
        """
    )
    suspend fun softDeleteSplit(
        splitId: Long,
        updatedAtEpochMillis: Long,
    )
}