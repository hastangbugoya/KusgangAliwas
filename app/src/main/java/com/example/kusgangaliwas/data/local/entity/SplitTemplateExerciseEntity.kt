package com.example.kusgangaliwas.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents an exercise inside a SplitTemplate (the roadmap).
 *
 * This defines the suggested order and content of a split.
 * It does NOT represent actual performance — that will be handled later
 * by session/log tables.
 *
 * Key idea:
 * - SplitTemplate = reusable plan
 * - SplitTemplateExercise = one item in that plan
 */
@Entity(
    tableName = "split_template_exercise",
    foreignKeys = [
        ForeignKey(
            entity = SplitTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["splitTemplateId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index(value = ["splitTemplateId"]),
        Index(value = ["exerciseId"]),
        // Prevent duplicate exercise in same split (v1 simplification)
        Index(value = ["splitTemplateId", "exerciseId"], unique = true),
    ],
)
data class SplitTemplateExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val splitTemplateId: Long,

    val exerciseId: Long,

    /**
     * Suggested execution order inside the split.
     *
     * Not enforced — purely advisory.
     */
    val suggestedOrder: Int,

    val notes: String? = null,

    val targetSets: Int? = null,

    val targetRepsMin: Int? = null,
    val targetRepsMax: Int? = null,

    val targetDistance: Double? = null,
    val targetDistanceUnit: String? = null,
    val targetDurationMinutes: Int? = null,

    /**
     * Allows marking optional exercises (e.g. accessories).
     */
    val isOptional: Boolean = false,
)
