package com.example.kusgangaliwas.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercise_pr",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["exerciseId"]),
        Index(value = ["exerciseId", "prType"], unique = true),
    ],
)
data class ExercisePrEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val exerciseId: Long,

    val prType: ExercisePrType,

    val value: Double,

    val secondaryValue: Double? = null,

    val achievedAtEpochMillis: Long,

    val sourceSetLogId: Long? = null,

    val notes: String? = null,

    val createdAtEpochMillis: Long,

    val updatedAtEpochMillis: Long,
)