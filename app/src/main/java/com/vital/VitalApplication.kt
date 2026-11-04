package com.vital

import android.app.Application
import com.vital.data.seed.SeedLoader
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class VitalApplication : Application() {

    @Inject
    lateinit var seedLoader: SeedLoader

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        scope.launch {
            seedLoader.seedUserAndSettings()
            seedLoader.loadIfNeeded()
        }
    }
}
