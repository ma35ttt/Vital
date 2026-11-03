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
import com.vital.ui.navigation.VitalNavigation
import com.vital.ui.theme.LocalTrueBlack
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
            val trueBlack = settings.trueBlack
            CompositionLocalProvider(LocalTrueBlack provides trueBlack) {
                VitalTheme(trueBlack = trueBlack) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = if (trueBlack) VitalColors.trueBlackBackground else VitalColors.falseBlackBackground
                    ) {
                        VitalNavigation()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
