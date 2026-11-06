package com.vital.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import com.vital.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

/** Everything the update UI can be showing at a given moment. */
sealed interface UpdateState {
    data object Idle : UpdateState
    data class Checking(val manual: Boolean) : UpdateState
    data class Available(val manifest: UpdateManifest) : UpdateState
    data class Downloading(val manifest: UpdateManifest, val progress: Float?) : UpdateState
    data class Ready(val manifest: UpdateManifest, val file: File) : UpdateState
    data class NeedsPermission(val manifest: UpdateManifest, val file: File?) : UpdateState
    data class Failed(val manifest: UpdateManifest?, val message: String) : UpdateState
    data class UpToDate(val manual: Boolean) : UpdateState
}

/**
 * Checks the published update manifest, downloads and verifies the APK, and hands it to the
 * system installer. Android always shows its own confirmation; this class cannot and does not
 * try to bypass it.
 *
 * A single instance is shared by the app shell and Settings, so both surfaces observe the
 * same state and a manual check is reflected everywhere.
 */
@Singleton
class UpdateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val client: OkHttpClient,
    private val preferences: UpdatePreferences
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }

    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    private var checkJob: Job? = null
    private var downloadJob: Job? = null

    /**
     * Fetch the manifest and decide whether to offer an update. A launch check is throttled
     * and respects a previous "Later"; a manual check from Settings bypasses both.
     */
    fun check(manual: Boolean) {
        val url = BuildConfig.UPDATE_MANIFEST_URL.trim()
        if (url.isEmpty()) {
            if (manual) {
                _state.value = UpdateState.Failed(
                    manifest = null,
                    message = "Updates aren't configured for this build."
                )
            }
            return
        }
        if (checkJob?.isActive == true) return

        val now = System.currentTimeMillis()
        if (!manual && now - preferences.lastCheckAt < CHECK_INTERVAL_MS) return

        checkJob = scope.launch {
            _state.value = UpdateState.Checking(manual)
            try {
                val manifest = fetchManifest(url)
                preferences.lastCheckAt = System.currentTimeMillis()
                _state.value = when {
                    manifest.versionCode <= BuildConfig.VERSION_CODE -> UpdateState.UpToDate(manual)
                    !manual && manifest.versionCode == preferences.dismissedVersionCode -> UpdateState.Idle
                    else -> UpdateState.Available(manifest)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _state.value = if (manual) {
                    UpdateState.Failed(
                        manifest = null,
                        message = error.message ?: "Couldn't check for updates."
                    )
                } else {
                    UpdateState.Idle
                }
            }
        }
    }

    /** Remember the release the user chose to skip, and stop any download in flight. */
    fun dismiss() {
        val current = _state.value
        val manifest = current.manifestOrNull()
        if (current is UpdateState.Downloading) downloadJob?.cancel()
        manifest?.let { preferences.dismissedVersionCode = it.versionCode }
        _state.value = UpdateState.Idle
    }

    /** The primary action on the release-notes dialog. */
    fun updateNow() {
        val manifest = _state.value.manifestOrNull() ?: return
        if (!canInstallPackages()) {
            _state.value = UpdateState.NeedsPermission(manifest, null)
            openInstallPermissionSettings()
            return
        }
        download(manifest)
    }

    /** Open the installer again after the user backed out of the system prompt. */
    fun installReady() {
        val ready = _state.value as? UpdateState.Ready ?: return
        install(ready.manifest, ready.file)
    }

    /** Retry a failed check or a failed download, whichever the dialog is showing. */
    fun retry() {
        val current = _state.value
        if (current is UpdateState.Failed && current.manifest == null) {
            check(manual = true)
            return
        }
        val manifest = current.manifestOrNull() ?: return
        if (!canInstallPackages()) {
            _state.value = UpdateState.NeedsPermission(manifest, null)
            openInstallPermissionSettings()
            return
        }
        download(manifest)
    }

    /** Called when the app returns to the foreground, to resume after granting permission. */
    fun onResume() {
        val current = _state.value as? UpdateState.NeedsPermission ?: return
        if (!canInstallPackages()) return
        if (current.file != null) {
            install(current.manifest, current.file)
        } else {
            download(current.manifest)
        }
    }

    fun openInstallPermissionSettings() {
        val packageUri = Uri.parse("package:${context.packageName}")
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageUri)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }.onFailure {
            runCatching {
                context.startActivity(
                    Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
    }

    private fun download(manifest: UpdateManifest) {
        if (downloadJob?.isActive == true) return
        downloadJob = scope.launch {
            val directory = File(context.cacheDir, "updates").apply { mkdirs() }
            directory.listFiles()?.forEach { it.delete() }
            val target = File(directory, "vital-${manifest.versionCode}.apk")

            _state.value = UpdateState.Downloading(manifest, null)
            try {
                downloadTo(target, manifest) { progress ->
                    _state.value = UpdateState.Downloading(manifest, progress)
                }
                val actual = sha256(target)
                if (!actual.equals(manifest.sha256, ignoreCase = true)) {
                    throw IOException("The download was incomplete. Please try again.")
                }
                if (!canInstallPackages()) {
                    _state.value = UpdateState.NeedsPermission(manifest, target)
                    return@launch
                }
                _state.value = UpdateState.Ready(manifest, target)
                install(manifest, target)
            } catch (cancelled: CancellationException) {
                target.delete()
                throw cancelled
            } catch (error: Exception) {
                target.delete()
                _state.value = UpdateState.Failed(
                    manifest = manifest,
                    message = error.message ?: "Download failed. Please try again."
                )
            }
        }
    }

    private fun install(manifest: UpdateManifest, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }.onFailure {
            _state.value = UpdateState.Failed(manifest, "Couldn't open the installer.")
        }
    }

    private fun fetchManifest(url: String): UpdateManifest {
        val request = Request.Builder()
            .url(url)
            .header("Cache-Control", "no-cache")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Update server returned ${response.code}.")
            }
            val body = response.body?.string()
                ?: throw IOException("The update server sent an empty response.")
            val manifest = try {
                json.decodeFromString<UpdateManifest>(body)
            } catch (error: Exception) {
                throw IOException("The update file is malformed.", error)
            }
            if (manifest.versionCode <= 0) {
                throw IOException("The update file has no version.")
            }
            val secureUrl = manifest.apkUrl.startsWith("https://") ||
                (BuildConfig.DEBUG && manifest.apkUrl.startsWith("http://"))
            if (!secureUrl) {
                throw IOException("The update download link is not secure.")
            }
            if (!manifest.sha256.matches(SHA256_PATTERN)) {
                throw IOException("The update file has no valid checksum.")
            }
            return manifest
        }
    }

    private fun downloadTo(
        target: File,
        manifest: UpdateManifest,
        onProgress: (Float?) -> Unit
    ) {
        val request = Request.Builder().url(manifest.apkUrl).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Download failed (${response.code}).")
            }
            val body = response.body ?: throw IOException("The download was empty.")
            val total = body.contentLength()
            body.byteStream().use { input ->
                target.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var read = 0L
                    var lastReport = 0L
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        output.write(buffer, 0, count)
                        read += count
                        val now = System.currentTimeMillis()
                        if (now - lastReport >= PROGRESS_INTERVAL_MS) {
                            lastReport = now
                            onProgress(
                                if (total > 0) {
                                    (read.toFloat() / total).coerceIn(0f, 1f)
                                } else {
                                    null
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { byte -> "%02x".format(byte) }
    }

    private fun canInstallPackages(): Boolean =
        context.packageManager.canRequestPackageInstalls()

    private fun UpdateState.manifestOrNull(): UpdateManifest? = when (this) {
        is UpdateState.Available -> manifest
        is UpdateState.Downloading -> manifest
        is UpdateState.Ready -> manifest
        is UpdateState.NeedsPermission -> manifest
        is UpdateState.Failed -> manifest
        else -> null
    }

    private companion object {
        const val CHECK_INTERVAL_MS = 6 * 60 * 60 * 1000L
        const val PROGRESS_INTERVAL_MS = 150L
        val SHA256_PATTERN = Regex("^[a-fA-F0-9]{64}$")
    }
}
