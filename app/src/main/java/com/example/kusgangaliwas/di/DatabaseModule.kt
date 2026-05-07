package com.example.kusgangaliwas.di

import android.content.Context
import androidx.room.Room
import com.example.kusgangaliwas.data.local.dao.ActualExerciseLogDao
import com.example.kusgangaliwas.data.local.dao.ActualExerciseSetLogDao
import com.example.kusgangaliwas.data.local.dao.ActualSessionDao
import com.example.kusgangaliwas.data.local.dao.CycleCalendarAnchorDao
import com.example.kusgangaliwas.data.local.dao.ExerciseDao
import com.example.kusgangaliwas.data.local.dao.ExerciseMuscleGroupDao
import com.example.kusgangaliwas.data.local.dao.ExerciseSubstitutionDao
import com.example.kusgangaliwas.data.local.dao.MuscleGroupDao
import com.example.kusgangaliwas.data.local.dao.PlannedSessionDao
import com.example.kusgangaliwas.data.local.dao.PlannedSessionExerciseDao
import com.example.kusgangaliwas.data.local.dao.SplitTemplateDao
import com.example.kusgangaliwas.data.local.dao.SplitTemplateExerciseDao
import com.example.kusgangaliwas.data.local.dao.TrainingCycleDao
import com.example.kusgangaliwas.data.local.dao.TrainingCycleStepDao
import com.example.kusgangaliwas.data.local.db.KusgangAliwasDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides the Room database and DAO dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): KusgangAliwasDatabase {
        return Room.databaseBuilder(
            context,
            KusgangAliwasDatabase::class.java,
            "kusgang_aliwas.db",
        ).build()
    }

    @Provides
    fun provideExerciseDao(database: KusgangAliwasDatabase): ExerciseDao =
        database.exerciseDao()

    @Provides
    fun provideMuscleGroupDao(database: KusgangAliwasDatabase): MuscleGroupDao =
        database.muscleGroupDao()

    @Provides
    fun provideExerciseMuscleGroupDao(database: KusgangAliwasDatabase): ExerciseMuscleGroupDao =
        database.exerciseMuscleGroupDao()

    @Provides
    fun provideExerciseSubstitutionDao(database: KusgangAliwasDatabase): ExerciseSubstitutionDao =
        database.exerciseSubstitutionDao()

    @Provides
    fun provideSplitTemplateDao(database: KusgangAliwasDatabase): SplitTemplateDao =
        database.splitTemplateDao()

    @Provides
    fun provideSplitTemplateExerciseDao(database: KusgangAliwasDatabase): SplitTemplateExerciseDao =
        database.splitTemplateExerciseDao()

    @Provides
    fun provideTrainingCycleDao(database: KusgangAliwasDatabase): TrainingCycleDao =
        database.trainingCycleDao()

    @Provides
    fun provideTrainingCycleStepDao(database: KusgangAliwasDatabase): TrainingCycleStepDao =
        database.trainingCycleStepDao()

    @Provides
    fun provideCycleCalendarAnchorDao(database: KusgangAliwasDatabase): CycleCalendarAnchorDao =
        database.cycleCalendarAnchorDao()

    @Provides
    fun providePlannedSessionDao(database: KusgangAliwasDatabase): PlannedSessionDao =
        database.plannedSessionDao()

    @Provides
    fun providePlannedSessionExerciseDao(database: KusgangAliwasDatabase): PlannedSessionExerciseDao =
        database.plannedSessionExerciseDao()

    @Provides
    fun provideActualSessionDao(database: KusgangAliwasDatabase): ActualSessionDao =
        database.actualSessionDao()

    @Provides
    fun provideActualExerciseLogDao(database: KusgangAliwasDatabase): ActualExerciseLogDao =
        database.actualExerciseLogDao()

    @Provides
    fun provideActualExerciseSetLogDao(database: KusgangAliwasDatabase): ActualExerciseSetLogDao =
        database.actualExerciseSetLogDao()
}