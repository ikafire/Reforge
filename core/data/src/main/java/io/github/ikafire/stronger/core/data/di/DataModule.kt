package io.github.ikafire.stronger.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.ikafire.stronger.core.data.repository.BodyMeasurementRepositoryImpl
import io.github.ikafire.stronger.core.data.repository.ExerciseRepositoryImpl
import io.github.ikafire.stronger.core.data.repository.TemplateRepositoryImpl
import io.github.ikafire.stronger.core.data.repository.UserPreferencesRepositoryImpl
import io.github.ikafire.stronger.core.data.repository.WorkoutRepositoryImpl
import io.github.ikafire.stronger.core.domain.repository.BodyMeasurementRepository
import io.github.ikafire.stronger.core.domain.repository.ExerciseRepository
import io.github.ikafire.stronger.core.domain.repository.TemplateRepository
import io.github.ikafire.stronger.core.domain.repository.UserPreferencesRepository
import io.github.ikafire.stronger.core.domain.repository.WorkoutRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindExerciseRepository(impl: ExerciseRepositoryImpl): ExerciseRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(impl: WorkoutRepositoryImpl): WorkoutRepository

    @Binds
    @Singleton
    abstract fun bindTemplateRepository(impl: TemplateRepositoryImpl): TemplateRepository

    @Binds
    @Singleton
    abstract fun bindBodyMeasurementRepository(impl: BodyMeasurementRepositoryImpl): BodyMeasurementRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository
}
