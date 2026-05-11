package com.example.kusgangaliwas.data.local.db

import androidx.room.TypeConverter
import com.example.kusgangaliwas.data.local.entity.ExerciseType

/**
 * Central Room converters for KusgangAliwas.
 *
 * Keep enums/simple persisted transformations here.
 */
class DatabaseConverters {

    @TypeConverter
    fun fromExerciseType(
        value: ExerciseType?,
    ): String? {
        return value?.name
    }

    @TypeConverter
    fun toExerciseType(
        value: String?,
    ): ExerciseType? {
        return value?.let(ExerciseType::valueOf)
    }
}