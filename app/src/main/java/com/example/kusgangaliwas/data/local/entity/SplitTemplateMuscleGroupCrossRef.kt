package com.example.kusgangaliwas.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "split_template_muscle_group",
    primaryKeys = ["splitTemplateId", "muscleGroupId"],
    foreignKeys = [
        ForeignKey(
            entity = SplitTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["splitTemplateId"],
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
        Index(value = ["splitTemplateId"]),
        Index(value = ["muscleGroupId"]),
    ],
)
data class SplitTemplateMuscleGroupCrossRef(
    val splitTemplateId: Long,
    val muscleGroupId: Long,
)
