package io.github.ikafire.reforge.core.data

import android.content.Context
import io.github.ikafire.reforge.core.database.dao.ExerciseDao
import io.github.ikafire.reforge.core.database.entity.ExerciseEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import javax.inject.Inject

class ExerciseSeeder @Inject constructor(
    private val exerciseDao: ExerciseDao,
) {
    suspend fun seedIfNeeded(context: Context) = withContext(Dispatchers.IO) {
        val count = exerciseDao.getExerciseCount()
        if (count > 0) return@withContext

        val json = context.assets.open("exercises.json")
            .bufferedReader()
            .use { it.readText() }

        val jsonArray = JSONArray(json)
        val entities = (0 until jsonArray.length()).map { i ->
            val obj = jsonArray.getJSONObject(i)
            val secondaryMuscles = if (obj.has("secondaryMuscles")) {
                val arr = obj.getJSONArray("secondaryMuscles")
                (0 until arr.length()).map { arr.getString(it) }
            } else {
                emptyList()
            }

            ExerciseEntity(
                id = obj.getString("id"),
                name = obj.getString("name"),
                category = obj.getString("category"),
                primaryMuscle = obj.getString("primaryMuscle"),
                secondaryMuscles = secondaryMuscles,
                instructions = if (obj.has("instructions") && !obj.isNull("instructions")) {
                    obj.getString("instructions")
                } else null,
                isCustom = false,
                resistanceProfile = null,
                createdAt = 0L,
            )
        }

        exerciseDao.insertExercises(entities)
    }
}
