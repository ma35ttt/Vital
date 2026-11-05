package com.vital.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Whether Vital is currently on screen.
 *
 * [com.vital.ui.MainActivity] flips it in `onStart`/`onStop`; [RestTimerForegroundService]
 * reads it to decide when the floating rest chip may show. The chip is the out-of-app
 * surface only, so it stays hidden the whole time the app is in front.
 */
@Singleton
class AppVisibility @Inject constructor() {

    private val _isForeground = MutableStateFlow(true)

    val isForeground: StateFlow<Boolean> = _isForeground.asStateFlow()

    fun onForeground() {
        _isForeground.value = true
    }

    fun onBackground() {
        _isForeground.value = false
    }
}
