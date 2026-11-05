package com.vital.auth

/**
 * Who is signed in, if anyone. The uid namespaces the user's cloud data; the display name
 * and email are only for showing in Settings.
 */
sealed interface AccountState {
    data object SignedOut : AccountState

    data class SignedIn(
        val uid: String,
        val displayName: String?,
        val email: String?
    ) : AccountState
}
