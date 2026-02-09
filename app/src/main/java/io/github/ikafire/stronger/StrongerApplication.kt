package io.github.ikafire.stronger

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.github.ikafire.stronger.core.data.ExerciseSeeder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class StrongerApplication : Application() {

    @Inject
    lateinit var exerciseSeeder: ExerciseSeeder

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            exerciseSeeder.seedIfNeeded(this@StrongerApplication)
        }
    }
}
