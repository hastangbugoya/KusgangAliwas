package com.example.kusgangaliwas.di

import com.example.kusgangaliwas.domain.repository.ExerciseRepository
import com.example.kusgangaliwas.domain.repository.SessionRepository
import com.example.kusgangaliwas.domain.repository.SplitTemplateRepository
import com.example.kusgangaliwas.domain.usecase.exercise.CreateExerciseUseCase
import com.example.kusgangaliwas.domain.usecase.exercise.GetEstimatedOneRepMaxUseCase
import com.example.kusgangaliwas.domain.usecase.split.AddExerciseToSplitUseCase
import com.example.kusgangaliwas.domain.usecase.split.CreateSplitTemplateUseCase
import com.example.kusgangaliwas.domain.usecase.split.GetSplitRoadmapUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Provides use cases.
 *
 * V1 only includes milestone 1 use cases (exercise + split foundation).
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun provideCreateExerciseUseCase(
        exerciseRepository: ExerciseRepository,
    ): CreateExerciseUseCase {
        return CreateExerciseUseCase(
            exerciseRepository = exerciseRepository,
        )
    }

    @Provides
    fun provideCreateSplitTemplateUseCase(
        splitTemplateRepository: SplitTemplateRepository,
    ): CreateSplitTemplateUseCase {
        return CreateSplitTemplateUseCase(
            splitTemplateRepository = splitTemplateRepository,
        )
    }

    @Provides
    fun provideAddExerciseToSplitUseCase(
        splitTemplateRepository: SplitTemplateRepository,
    ): AddExerciseToSplitUseCase {
        return AddExerciseToSplitUseCase(
            splitTemplateRepository = splitTemplateRepository,
        )
    }

    @Provides
    fun provideGetSplitRoadmapUseCase(
        splitTemplateRepository: SplitTemplateRepository,
    ): GetSplitRoadmapUseCase {
        return GetSplitRoadmapUseCase(
            splitTemplateRepository = splitTemplateRepository,
        )
    }

    @Provides
    fun provideGetEstimatedOneRepMaxUseCase(
        sessionRepository: SessionRepository,
    ): GetEstimatedOneRepMaxUseCase {
        return GetEstimatedOneRepMaxUseCase(
            sessionRepository = sessionRepository,
        )
    }
}