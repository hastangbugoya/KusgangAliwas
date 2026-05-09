package com.example.kusgangaliwas.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kusgangaliwas.data.local.entity.SplitScheduleEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for split scheduling rules.
 *
 * Split schedules are lightweight rules that say when a SplitTemplate should
 * produce planned sessions. They may optionally belong to a Program, but program
 * membership is not required.
 */
@Dao
interface SplitScheduleDao {

    @Query(
        """
        SELECT *
        FROM split_schedule
        ORDER BY isActive DESC, startEpochDay ASC, title COLLATE NOCASE ASC
        """
    )
    fun observeAllSchedules(): Flow<List<SplitScheduleEntity>>

    @Query(
        """
        SELECT *
        FROM split_schedule
        WHERE isActive = 1
        ORDER BY startEpochDay ASC, title COLLATE NOCASE ASC
        """
    )
    fun observeActiveSchedules(): Flow<List<SplitScheduleEntity>>

    @Query(
        """
        SELECT *
        FROM split_schedule
        WHERE id = :id
        LIMIT 1
        """
    )
    suspend fun getScheduleById(id: Long): SplitScheduleEntity?

    @Query(
        """
        SELECT *
        FROM split_schedule
        WHERE splitTemplateId = :splitTemplateId
        ORDER BY isActive DESC, startEpochDay ASC, title COLLATE NOCASE ASC
        """
    )
    fun observeSchedulesForSplitTemplate(splitTemplateId: Long): Flow<List<SplitScheduleEntity>>

    @Query(
        """
        SELECT *
        FROM split_schedule
        WHERE programId = :programId
        ORDER BY isActive DESC, startEpochDay ASC, cycleOrder ASC, title COLLATE NOCASE ASC
        """
    )
    fun observeSchedulesForProgram(programId: Long): Flow<List<SplitScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSchedule(schedule: SplitScheduleEntity): Long

    @Update
    suspend fun updateSchedule(schedule: SplitScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: SplitScheduleEntity)

    @Query(
        """
        DELETE FROM split_schedule
        WHERE id = :id
        """
    )
    suspend fun deleteScheduleById(id: Long)
}