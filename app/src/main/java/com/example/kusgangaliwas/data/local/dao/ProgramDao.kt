package com.example.kusgangaliwas.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kusgangaliwas.data.local.entity.ProgramEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for ProgramEntity.
 *
 * Programs are optional groupings for split schedules.
 * They are NOT required for scheduling to work.
 *
 * Design intent:
 * - Keep queries simple (no joins for now)
 * - Focus on CRUD + basic observation
 */
@Dao
interface ProgramDao {

    @Query(
        """
        SELECT *
        FROM program
        ORDER BY isActive DESC, createdAtEpochMillis DESC, name COLLATE NOCASE ASC
        """
    )
    fun observeAllPrograms(): Flow<List<ProgramEntity>>

    @Query(
        """
        SELECT *
        FROM program
        WHERE isActive = 1
        ORDER BY createdAtEpochMillis DESC, name COLLATE NOCASE ASC
        """
    )
    fun observeActivePrograms(): Flow<List<ProgramEntity>>

    @Query(
        """
        SELECT *
        FROM program
        WHERE id = :id
        LIMIT 1
        """
    )
    suspend fun getProgramById(id: Long): ProgramEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgram(program: ProgramEntity): Long

    @Update
    suspend fun updateProgram(program: ProgramEntity)

    @Delete
    suspend fun deleteProgram(program: ProgramEntity)

    @Query(
        """
        DELETE FROM program
        WHERE id = :id
        """
    )
    suspend fun deleteProgramById(id: Long)
}