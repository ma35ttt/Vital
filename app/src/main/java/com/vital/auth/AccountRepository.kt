package com.vital.auth

import android.content.Context
import kotlinx.coroutines.flow.Flow

/**
 * Google sign-in, backed by Firebase Auth. Kept behind an interface so the UI (and, later,
 * the sync engine) do not depend on Firebase directly and so it can be faked in tests.
 */
interface AccountRepository {

    /** Whether a Firebase project is configured (google-services.json present). */
    val isConfigured: Boolean

    /** The signed-in account, or [AccountState.SignedOut]. */
    val state: Flow<AccountState>

    /** The current uid, or null when signed out / not configured. */
    fun currentUid(): String?

    /**
     * Runs the Google account picker and signs in. [context] must be an Activity, which is
     * what Credential Manager needs to show the bottom sheet. Returns the new state, or a
     * failure carrying the reason (canceled, not configured, network, ...).
     */
    suspend fun signInWithGoogle(context: Context): Result<AccountState>

    fun signOut()
}
