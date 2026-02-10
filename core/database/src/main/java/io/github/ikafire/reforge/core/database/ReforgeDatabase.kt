package io.github.ikafire.reforge.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.ikafire.reforge.core.database.dao.ExerciseDao
import io.github.ikafire.reforge.core.database.dao.WorkoutDao
import io.github.ikafire.reforge.core.database.dao.WorkoutExerciseDao
import io.github.ikafire.reforge.core.database.dao.WorkoutSetDao
import io.github.ikafire.reforge.core.database.dao.TemplateDao
import io.github.ikafire.reforge.core.database.dao.TemplateFolderDao
import io.github.ikafire.reforge.core.database.dao.TemplateExerciseDao
import io.github.ikafire.reforge.core.database.dao.BodyMeasurementDao
import io.github.ikafire.reforge.core.database.entity.ExerciseEntity
import io.github.ikafire.reforge.core.database.entity.WorkoutEntity
import io.github.ikafire.reforge.core.database.entity.WorkoutExerciseEntity
import io.github.ikafire.reforge.core.database.entity.WorkoutSetEntity
import io.github.ikafire.reforge.core.database.entity.WorkoutTemplateEntity
import io.github.ikafire.reforge.core.database.entity.TemplateFolderEntity
import io.github.ikafire.reforge.core.database.entity.TemplateExerciseEntity
import io.github.ikafire.reforge.core.database.entity.BodyMeasurementEntity

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutEntity::class,
        WorkoutExerciseEntity::class,
        WorkoutSetEntity::class,
        WorkoutTemplateEntity::class,
        TemplateFolderEntity::class,
        TemplateExerciseEntity::class,
        BodyMeasurementEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class ReforgeDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun workoutExerciseDao(): WorkoutExerciseDao
    abstract fun workoutSetDao(): WorkoutSetDao
    abstract fun templateDao(): TemplateDao
    abstract fun templateFolderDao(): TemplateFolderDao
    abstract fun templateExerciseDao(): TemplateExerciseDao
    abstract fun bodyMeasurementDao(): BodyMeasurementDao
}
