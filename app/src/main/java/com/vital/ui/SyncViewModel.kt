package com.vital.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vital.auth.AccountRepository
import com.vital.auth.AccountState
import com.vital.data.entity.SyncMeta
import com.vital.data.repository.SyncMetaRepository
import com.vital.data.sync.SyncEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Drives cloud sync: pushes/pulls when a user signs in, when the app comes to the foreground,
 * and on demand. Exposes an in-flight flag, an error message, and the last-synced time for the
 * Settings status line.
 */
@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncEngine: SyncEngine,
    private val accountRepository: AccountRepository,
    private val syncMetaRepository: SyncMetaRepository
) : ViewModel() {

    private val _syncing = MutableStateFlow(false)
    val syncing: StateFlow<Boolean> = _syncing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /** Epoch millis of the last successful sync, or 0 when never synced. */
    val lastSyncedAt: StateFlow<Long> = syncMetaRepository.getSyncMeta()
        .map { it?.lastPushAt ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    private var lastAttemptAt = 0L
    private var syncedUid: String? = null

    init {
        // Sign-in triggers the first sync for the new account.
        viewModelScope.launch {
            accountRepository.state.collect { state ->
                if (state is AccountState.SignedIn) {
                    if (syncedUid != state.uid) {
                        syncedUid = state.uid
                        sync(force = true)
                    }
                } else {
                    syncedUid = null
                }
            }
        }
    }

    fun syncNow() = sync(force = true)

    /** Call on app foreground; throttled so quick app switches do not hammer Firestore. */
    fun onForeground() = sync(force = false)

    private fun sync(force: Boolean) {
        val uid = accountRepository.currentUid() ?: return
        if (_syncing.value) return
        val now = System.currentTimeMillis()
        if (!force && now - lastAttemptAt < FOREGROUND_MIN_INTERVAL_MS) return
        lastAttemptAt = now
        _syncing.value = true
        _error.value = null
        viewModelScope.launch {
            val result = runCatching { syncEngine.sync(uid) }
            _syncing.value = false
            result
                .onSuccess {
                    syncMetaRepository.insertSyncMeta(
                        SyncMeta(lastPushAt = now, lastPullAt = now, lastError = null)
                    )
                }
                .onFailure { error ->
                    _error.value = friendlyError(error)
                    syncMetaRepository.insertSyncMeta(
                        SyncMeta(lastError = error.message)
                    )
                }
        }
    }

    private fun friendlyError(error: Throwable): String {
        val message = error.message.orEmpty()
        return when {
            message.contains("network", ignoreCase = true) ||
                message.contains("unavailable", ignoreCase = true) -> "No connection"
            message.contains("permission", ignoreCase = true) -> "Sync permission denied"
            message.contains("not configured", ignoreCase = true) -> "Sign-in is not set up"
            else -> "Sync failed"
        }
    }

    private companion object {
        const val FOREGROUND_MIN_INTERVAL_MS = 5 * 60 * 1000L
    }
}
