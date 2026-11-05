package com.vital.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
    private val syncViewModel: SyncViewModel by viewModels()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /** Last value of the high-refresh preference, so it can be re-asked for on resume. */
    private var preferHighRefreshRate: Boolean? = null

    @Inject
    lateinit var appVisibility: AppVisibility

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
            val trueBlack = settings.trueBlack
            val highRefreshRate = settings.highRefreshRate
            preferHighRefreshRate = highRefreshRate

            // Ask the window for the fastest display mode whenever the preference changes.
            LaunchedEffect(highRefreshRate) {
                DisplayRefresh.apply(this@MainActivity, highRefreshRate)
            }

            CompositionLocalProvider(LocalTrueBlack provides trueBlack) {
                VitalTheme(themeId = settings.themeId, trueBlack = trueBlack) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = VitalColors.paper
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
        // Pull anything changed on other devices when the app comes forward (throttled inside).
        syncViewModel.onForeground()
    }

    override fun onResume() {
        super.onResume()
        // The system can hand the display back to its default rate while Vital is away.
        preferHighRefreshRate?.let { DisplayRefresh.apply(this, it) }
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
