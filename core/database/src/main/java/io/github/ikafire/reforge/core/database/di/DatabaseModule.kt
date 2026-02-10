package io.github.ikafire.reforge.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.ikafire.reforge.core.common.Constants
import io.github.ikafire.reforge.core.database.ReforgeDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ReforgeDatabase {
        return Room.databaseBuilder(
            context,
            ReforgeDatabase::class.java,
            Constants.DATABASE_NAME,
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideExerciseDao(db: ReforgeDatabase) = db.exerciseDao()

    @Provides
    fun provideWorkoutDao(db: ReforgeDatabase) = db.workoutDao()

    @Provides
    fun provideWorkoutExerciseDao(db: ReforgeDatabase) = db.workoutExerciseDao()

    @Provides
    fun provideWorkoutSetDao(db: ReforgeDatabase) = db.workoutSetDao()

    @Provides
    fun provideTemplateDao(db: ReforgeDatabase) = db.templateDao()

    @Provides
    fun provideTemplateFolderDao(db: ReforgeDatabase) = db.templateFolderDao()

    @Provides
    fun provideTemplateExerciseDao(db: ReforgeDatabase) = db.templateExerciseDao()

    @Provides
    fun provideBodyMeasurementDao(db: ReforgeDatabase) = db.bodyMeasurementDao()
}
