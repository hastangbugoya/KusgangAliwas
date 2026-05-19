package com.example.kusgangaliwas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kusgangaliwas.data.local.entity.SplitTemplateEntity
import com.example.kusgangaliwas.data.local.model.SplitTemplateSummaryRow
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

    @Query(
        """
    SELECT
        st.id AS splitTemplateId,
        st.name AS splitName,

        COALESCE(
            GROUP_CONCAT(DISTINCT mg.name),
            ''
        ) AS muscleGroupsText,

        COUNT(
            DISTINCT CASE
                WHEN e.exerciseType = 'STRENGTH'
                THEN e.id
            END
        ) AS strengthExerciseCount,

        COUNT(
            DISTINCT CASE
                WHEN e.exerciseType = 'CARDIO'
                THEN e.id
            END
        ) AS cardioExerciseCount

    FROM split_template st

    LEFT JOIN split_template_exercise ste
        ON ste.splitTemplateId = st.id

    LEFT JOIN exercise e
        ON e.id = ste.exerciseId

    LEFT JOIN split_template_muscle_group stmg
        ON stmg.splitTemplateId = st.id

    LEFT JOIN muscle_group mg
        ON mg.id = stmg.muscleGroupId

    WHERE st.isActive = 1

    GROUP BY st.id

    ORDER BY st.name COLLATE NOCASE ASC
    """
    )
    suspend fun getActiveSplitSummaries(): List<SplitTemplateSummaryRow>

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