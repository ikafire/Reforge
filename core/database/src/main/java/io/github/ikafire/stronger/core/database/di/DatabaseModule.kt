package io.github.ikafire.stronger.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.ikafire.stronger.core.common.Constants
import io.github.ikafire.stronger.core.database.StrongerDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): StrongerDatabase {
        return Room.databaseBuilder(
            context,
            StrongerDatabase::class.java,
            Constants.DATABASE_NAME,
        ).build()
    }

    @Provides
    fun provideExerciseDao(db: StrongerDatabase) = db.exerciseDao()

    @Provides
    fun provideWorkoutDao(db: StrongerDatabase) = db.workoutDao()

    @Provides
    fun provideWorkoutExerciseDao(db: StrongerDatabase) = db.workoutExerciseDao()

    @Provides
    fun provideWorkoutSetDao(db: StrongerDatabase) = db.workoutSetDao()

    @Provides
    fun provideTemplateDao(db: StrongerDatabase) = db.templateDao()

    @Provides
    fun provideTemplateFolderDao(db: StrongerDatabase) = db.templateFolderDao()

    @Provides
    fun provideTemplateExerciseDao(db: StrongerDatabase) = db.templateExerciseDao()

    @Provides
    fun provideBodyMeasurementDao(db: StrongerDatabase) = db.bodyMeasurementDao()
}
