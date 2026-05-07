package com.example.kusgangaliwas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kusgangaliwas.data.local.entity.ExerciseMuscleGroupCrossRef
import kotlinx.coroutines.flow.Flow

/**
 * DAO for mapping exercises to muscle groups.
 *
 * Responsibilities:
 * - assign muscle groups to an exercise
 * - remove mappings
 * - query relationships (exercise ↔ muscle groups)
 */
@Dao
interface ExerciseMuscleGroupDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCrossRef(entity: ExerciseMuscleGroupCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCrossRefs(entities: List<ExerciseMuscleGroupCrossRef>)

    @Query(
        """
        DELETE FROM exercise_muscle_group
        WHERE exerciseId = :exerciseId
        """
    )
    suspend fun deleteAllForExercise(exerciseId: Long)

    @Query(
        """
        DELETE FROM exercise_muscle_group
        WHERE exerciseId = :exerciseId
        AND muscleGroupId = :muscleGroupId
        """
    )
    suspend fun deleteMapping(
        exerciseId: Long,
        muscleGroupId: Long,
    )

    @Query(
        """
        SELECT *
        FROM exercise_muscle_group
        WHERE exerciseId = :exerciseId
        """
    )
    fun observeMuscleGroupsForExercise(
        exerciseId: Long,
    ): Flow<List<ExerciseMuscleGroupCrossRef>>

    @Query(
        """
        SELECT *
        FROM exercise_muscle_group
        WHERE muscleGroupId = :muscleGroupId
        """
    )
    fun observeExercisesForMuscleGroup(
        muscleGroupId: Long,
    ): Flow<List<ExerciseMuscleGroupCrossRef>>
}