package com.example.kusgangaliwas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kusgangaliwas.data.local.entity.ExercisePrEntity
import com.example.kusgangaliwas.data.local.entity.ExercisePrType
import kotlinx.coroutines.flow.Flow

@Dao
interface ExercisePrDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExercisePr(
        entity: ExercisePrEntity,
    )

    @Query(
        """
        SELECT *
        FROM exercise_pr
        WHERE exerciseId = :exerciseId
        ORDER BY achievedAtEpochMillis DESC
        """
    )
    fun observePrsForExercise(
        exerciseId: Long,
    ): Flow<List<ExercisePrEntity>>

    @Query(
        """
        SELECT *
        FROM exercise_pr
        WHERE exerciseId = :exerciseId
            AND prType = :prType
        LIMIT 1
        """
    )
    suspend fun getPrForExercise(
        exerciseId: Long,
        prType: ExercisePrType,
    ): ExercisePrEntity?

    @Query(
        """
        DELETE FROM exercise_pr
        WHERE exerciseId = :exerciseId
            AND prType = :prType
        """
    )
    suspend fun deletePrForExercise(
        exerciseId: Long,
        prType: ExercisePrType,
    )
}