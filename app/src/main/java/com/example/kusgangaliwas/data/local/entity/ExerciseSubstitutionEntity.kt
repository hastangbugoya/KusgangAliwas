package com.example.kusgangaliwas.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a user-defined substitution or equivalence between two exercises.
 *
 * This is intentionally separate from muscle-group-based suggestions:
 * - Substitutions are explicit user preferences (e.g., “use DB press if bench is busy”).
 * - Muscle group matches are computed suggestions.
 *
 * Directional:
 * sourceExerciseId -> substituteExerciseId
 *
 * If you want bidirectional behavior, insert two rows.
 */
@Entity(
    tableName = "exercise_substitution",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["sourceExerciseId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["substituteExerciseId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["sourceExerciseId"]),
        Index(value = ["substituteExerciseId"]),
        // Prevent duplicate pairs
        Index(value = ["sourceExerciseId", "substituteExerciseId"], unique = true),
    ],
)
data class ExerciseSubstitutionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val sourceExerciseId: Long,
    val substituteExerciseId: Long,

    /**
     * Optional label for the relationship.
     *
     * Examples:
     * - "machine"
     * - "dumbbell"
     * - "bodyweight"
     * - "similar"
     */
    val relationshipType: String? = null,

    val notes: String? = null,

    val isActive: Boolean = true,
)