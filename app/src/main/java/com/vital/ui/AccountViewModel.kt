package com.vital.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vital.auth.AccountRepository
import com.vital.auth.AccountState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Drives the Account section in Settings: the signed-in state, the Google sign-in flow, and
 * its in-flight/error state. Sync itself is handled by [SyncViewModel].
 */
@HiltViewModel
class AccountViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {

    val account: StateFlow<AccountState> = accountRepository.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccountState.SignedOut)

    /** False until a Firebase project is configured, so the UI can explain why sign-in is off. */
    val isConfigured: Boolean get() = accountRepository.isConfigured

    private val _signingIn = MutableStateFlow(false)
    val signingIn: StateFlow<Boolean> = _signingIn.asStateFlow()

    private val _signInError = MutableStateFlow<String?>(null)
    val signInError: StateFlow<String?> = _signInError.asStateFlow()

    fun signIn(context: Context) {
        if (_signingIn.value) return
        _signInError.value = null
        _signingIn.value = true
        viewModelScope.launch {
            val result = accountRepository.signInWithGoogle(context)
            _signingIn.value = false
            result.onFailure { _signInError.value = friendlySignInError(it) }
        }
    }

    fun signOut() {
        accountRepository.signOut()
        _signInError.value = null
    }

    fun clearError() {
        _signInError.value = null
    }

    private fun friendlySignInError(error: Throwable): String {
        val message = error.message.orEmpty()
        return when {
            message.contains("cancel", ignoreCase = true) -> "Sign-in was canceled"
            message.contains("not configured", ignoreCase = true) ||
                message.contains("client id", ignoreCase = true) ->
                "Sign-in is not set up yet"
            message.contains("network", ignoreCase = true) ->
                "No connection. Try again"
            else -> "Sign-in failed. Try again"
        }
    }
}
