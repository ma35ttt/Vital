package com.vital.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import com.vital.service.AppVisibility
import com.vital.ui.navigation.VitalNavigation
import com.vital.ui.theme.LocalTrueBlack
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @Inject
    lateinit var appVisibility: AppVisibility

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
            val trueBlack = settings.trueBlack
            val lightMode = settings.lightMode
            CompositionLocalProvider(LocalTrueBlack provides trueBlack) {
                VitalTheme(darkTheme = !lightMode, trueBlack = trueBlack) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = when {
                            lightMode -> VitalColors.paper
                            trueBlack -> VitalColors.trueBlackBackground
                            else -> VitalColors.falseBlackBackground
                        }
                    ) {
                        VitalNavigation()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        appVisibility.onForeground()
    }

    override fun onStop() {
        // The rest chip only belongs over other apps, so hide it the moment Vital leaves.
        appVisibility.onBackground()
        super.onStop()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
