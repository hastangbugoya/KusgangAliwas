package com.example.kusgangaliwas.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Many-to-many relationship between Exercise and MuscleGroup.
 *
 * This allows:
 * - One exercise to target multiple muscle groups
 * - One muscle group to be linked to many exercises
 *
 * The emphasis field is intentionally simple for v1 and can be expanded later.
 */
@Entity(
    tableName = "exercise_muscle_group",
    primaryKeys = ["exerciseId", "muscleGroupId"],
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = MuscleGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["muscleGroupId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["exerciseId"]),
        Index(value = ["muscleGroupId"]),
    ],
)
data class ExerciseMuscleGroupCrossRef(
    val exerciseId: Long,
    val muscleGroupId: Long,

    /**
     * Indicates how strongly this muscle group is involved.
     *
     * Values:
     * - PRIMARY
     * - SECONDARY
     * - STABILIZER
     */
    val emphasis: ExerciseMuscleEmphasis =
        ExerciseMuscleEmphasis.SECONDARY,
)