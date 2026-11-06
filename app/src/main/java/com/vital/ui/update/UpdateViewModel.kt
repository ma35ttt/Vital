package com.vital.ui.update

import androidx.lifecycle.ViewModel
import com.vital.BuildConfig
import com.vital.data.update.UpdateManager
import com.vital.data.update.UpdateState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val manager: UpdateManager
) : ViewModel() {

    val state: StateFlow<UpdateState> = manager.state
    val currentVersion: String = BuildConfig.VERSION_NAME

    fun check(manual: Boolean = false) = manager.check(manual)
    fun updateNow() = manager.updateNow()
    fun installReady() = manager.installReady()
    fun retry() = manager.retry()
    fun dismiss() = manager.dismiss()
    fun onResume() = manager.onResume()
    fun openSettings() = manager.openInstallPermissionSettings()
}
