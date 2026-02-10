package io.github.ikafire.reforge

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.github.ikafire.reforge.core.data.ExerciseSeeder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class ReforgeApplication : Application() {

    @Inject
    lateinit var exerciseSeeder: ExerciseSeeder

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            exerciseSeeder.seedIfNeeded(this@ReforgeApplication)
        }
    }
}
