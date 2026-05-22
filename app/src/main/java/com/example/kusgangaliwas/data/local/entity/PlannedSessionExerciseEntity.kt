package com.example.kusgangaliwas.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents one planned exercise item inside a planned session.
 *
 * This is a session-specific copy of a roadmap item.
 * It allows the user to change a planned session without mutating
 * the reusable SplitTemplate.
 *
 * Also supports carry-forward behavior:
 * - missed exercises from previous sessions
 * - intentionally skipped exercises imported into a later session
 */
@Entity(
    tableName = "planned_session_exercise",
    foreignKeys = [
        ForeignKey(
            entity = PlannedSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["plannedSessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["plannedExerciseId"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = SplitTemplateExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["sourceSplitTemplateExerciseId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = ExercisePaceProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["paceProfileId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["plannedSessionId"]),
        Index(value = ["plannedExerciseId"]),
        Index(value = ["sourceSplitTemplateExerciseId"]),
        Index(value = ["sourcePlannedSessionExerciseId"]),
        Index(value = ["paceProfileId"]),
    ],
)
data class PlannedSessionExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val plannedSessionId: Long,

    val plannedExerciseId: Long,

    /**
     * Optional planned-session pace profile.
     *
     * This should be resolved when the planned session exercise is created:
     * - split exercise selected pace profile
     * - else exercise default pace profile
     * - else null
     *
     * Runtime logging can use this value directly without re-resolving from
     * the split template. If the profile is deleted later, Room sets this to
     * null and the session simply has no pace nudges.
     */
    val paceProfileId: Long? = null,

    /**
     * Optional reference to the split-template roadmap item this came from.
     */
    val sourceSplitTemplateExerciseId: Long? = null,

    /**
     * Optional reference to a previous planned session exercise.
     *
     * Used for carry-forward behavior:
     * - missed from previous split
     * - intentionally skipped from previous split
     * - imported into current session
     *
     * Note:
     * This is intentionally not declared as a Room foreign key yet because
     * self-referencing planned items can complicate deletes/migrations.
     */
    val sourcePlannedSessionExerciseId: Long? = null,

    /**
     * Suggested values:
     * - "template"
     * - "manual"
     * - "carriedForwardMissed"
     * - "carriedForwardSkipped"
     * - "cycleGenerated"
     */
    val originType: String,

    /**
     * Suggested order in the planned roadmap.
     *
     * Not enforced during actual session logging.
     */
    val suggestedOrder: Int,

    /**
     * Suggested values:
     * - "suggested"
     * - "performed"
     * - "substituted"
     * - "missed"
     * - "intentionallySkipped"
     * - "carriedForward"
     */
    val status: String = "suggested",

    val notes: String? = null,
)