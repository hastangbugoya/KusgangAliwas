package com.example.kusgangaliwas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kusgangaliwas.data.local.entity.SplitTemplateMuscleGroupCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface SplitTemplateMuscleGroupDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(
        crossRef: SplitTemplateMuscleGroupCrossRef,
    )

    @Query(
        """
        DELETE FROM split_template_muscle_group
        WHERE splitTemplateId = :splitTemplateId
            AND muscleGroupId = :muscleGroupId
        """
    )
    suspend fun delete(
        splitTemplateId: Long,
        muscleGroupId: Long,
    )

    @Query(
        """
        SELECT *
        FROM split_template_muscle_group
        WHERE splitTemplateId = :splitTemplateId
        """
    )
    fun observeForSplit(
        splitTemplateId: Long,
    ): Flow<List<SplitTemplateMuscleGroupCrossRef>>
}