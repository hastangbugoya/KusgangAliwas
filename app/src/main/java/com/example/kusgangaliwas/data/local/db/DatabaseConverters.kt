package com.example.kusgangaliwas.data.local.db

import androidx.room.TypeConverter
import com.example.kusgangaliwas.data.local.entity.ExerciseMuscleEmphasis
import com.example.kusgangaliwas.data.local.entity.ExercisePrType
import com.example.kusgangaliwas.data.local.entity.ExerciseType
import com.example.kusgangaliwas.domain.model.session.ActualSessionStatus

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

    @TypeConverter
    fun fromExerciseMuscleEmphasis(
        value: ExerciseMuscleEmphasis?,
    ): String? {
        return value?.name
    }

    @TypeConverter
    fun toExerciseMuscleEmphasis(
        value: String?,
    ): ExerciseMuscleEmphasis? {
        return value
            ?.uppercase()
            ?.let(ExerciseMuscleEmphasis::valueOf)
    }

    @TypeConverter
    fun fromExercisePrType(
        value: ExercisePrType?,
    ): String? {
        return value?.name
    }

    @TypeConverter
    fun toExercisePrType(
        value: String?,
    ): ExercisePrType? {
        return value
            ?.uppercase()
            ?.let(ExercisePrType::valueOf)
    }

    @TypeConverter
    fun fromActualSessionStatus(
        value: ActualSessionStatus?,
    ): String? {
        return when (value) {
            ActualSessionStatus.IN_PROGRESS -> "inProgress"
            ActualSessionStatus.COMPLETED -> "completed"
            ActualSessionStatus.ABANDONED -> "abandoned"
            null -> null
        }
    }

    @TypeConverter
    fun toActualSessionStatus(
        value: String?,
    ): ActualSessionStatus? {
        return when (value) {
            "inProgress" -> ActualSessionStatus.IN_PROGRESS
            "completed" -> ActualSessionStatus.COMPLETED
            "abandoned" -> ActualSessionStatus.ABANDONED

            /*
             * Backward/forward safety fallback.
             *
             * Prevent crashes if older or unexpected values exist.
             */
            else -> ActualSessionStatus.IN_PROGRESS
        }
    }
}