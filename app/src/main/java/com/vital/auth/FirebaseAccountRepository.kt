package com.vital.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.vital.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Firebase Auth implementation of [AccountRepository].
 *
 * Everything here degrades gracefully when no Firebase project is configured: [isConfigured]
 * is false, [state] is always [AccountState.SignedOut], and [signInWithGoogle] fails with a
 * readable reason instead of crashing. That keeps the app usable before google-services.json
 * is added.
 */
@Singleton
class FirebaseAccountRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : AccountRepository {

    // FirebaseApp auto-initializes from google-services.json; with no project the list is empty.
    private val firebaseAuth: FirebaseAuth? =
        if (FirebaseApp.getApps(context).isNotEmpty()) FirebaseAuth.getInstance() else null

    override val isConfigured: Boolean get() = firebaseAuth != null && serverClientId() != null

    override val state: Flow<AccountState> = callbackFlow {
        val auth = firebaseAuth
        if (auth == null) {
            trySend(AccountState.SignedOut)
            awaitClose { }
            return@callbackFlow
        }
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser.toAccountState()) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.distinctUntilChanged()

    override fun currentUid(): String? = firebaseAuth?.currentUser?.uid

    override suspend fun signInWithGoogle(context: Context): Result<AccountState> {
        val auth = firebaseAuth
            ?: return Result.failure(IllegalStateException("Firebase is not configured"))
        val clientId = serverClientId()
            ?: return Result.failure(IllegalStateException("Google sign-in client id is missing"))

        return try {
            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(clientId)
                // Show every account on the device, not just ones already used with Vital, so
                // a first-time user can pick the account they want to sign up with.
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credential = credentialManager.getCredential(context, request).credential
            if (credential !is CustomCredential ||
                credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                return Result.failure(IllegalStateException("Unexpected credential: ${credential.type}"))
            }
            val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(firebaseCredential).awaitTask()
            Result.success(result.user.toAccountState())
        } catch (e: Exception) {
            Log.w(TAG, "Google sign-in failed", e)
            Result.failure(e)
        }
    }

    override fun signOut() {
        firebaseAuth?.signOut()
    }

    /**
     * The OAuth web client id Credential Manager exchanges the Google id token against. Prefer
     * an explicit value in `google_web_client_id`; otherwise fall back to the `default_web_client_id`
     * that the google-services plugin generates when the project has a web client.
     */
    private fun serverClientId(): String? {
        val explicit = context.getString(R.string.google_web_client_id)
        if (explicit.isNotBlank()) return explicit
        val resId = context.resources.getIdentifier(
            "default_web_client_id", "string", context.packageName
        )
        if (resId != 0) {
            val generated = context.getString(resId)
            if (generated.isNotBlank()) return generated
        }
        return null
    }

    private fun FirebaseUser?.toAccountState(): AccountState =
        if (this == null) {
            AccountState.SignedOut
        } else {
            AccountState.SignedIn(uid = uid, displayName = displayName, email = email)
        }

    private companion object {
        const val TAG = "FirebaseAccountRepository"
    }
}
