package com.vital.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.vital.ui.theme.LocalTrueBlack
import androidx.hilt.navigation.compose.hiltViewModel
import com.vital.ui.navigation.TabBar
import com.vital.ui.navigation.VitalNavigation
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import kotlinx.coroutines.cancel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val trueBlack by viewModel.trueBlack.collectAsState()
            CompositionLocalProvider(LocalTrueBlack provides trueBlack) {
                VitalTheme(trueBlack = trueBlack) {
                    Surface(
                        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
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

