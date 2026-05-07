package com.example.kusgangaliwas.di

import com.example.kusgangaliwas.data.repository.ExerciseRepositoryImpl
import com.example.kusgangaliwas.data.repository.SessionRepositoryImpl
import com.example.kusgangaliwas.data.repository.SplitTemplateRepositoryImpl
import com.example.kusgangaliwas.data.repository.TrainingCycleRepositoryImpl
import com.example.kusgangaliwas.domain.repository.ExerciseRepository
import com.example.kusgangaliwas.domain.repository.SessionRepository
import com.example.kusgangaliwas.domain.repository.SplitTemplateRepository
import com.example.kusgangaliwas.domain.repository.TrainingCycleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds repository interfaces to their Room-backed implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindExerciseRepository(
        impl: ExerciseRepositoryImpl,
    ): ExerciseRepository

    @Binds
    @Singleton
    abstract fun bindSplitTemplateRepository(
        impl: SplitTemplateRepositoryImpl,
    ): SplitTemplateRepository

    @Binds
    @Singleton
    abstract fun bindTrainingCycleRepository(
        impl: TrainingCycleRepositoryImpl,
    ): TrainingCycleRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(
        impl: SessionRepositoryImpl,
    ): SessionRepository
}