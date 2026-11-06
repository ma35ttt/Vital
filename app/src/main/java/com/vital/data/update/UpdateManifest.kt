package com.vital.data.update

import kotlinx.serialization.Serializable

/**
 * The small file published alongside each release. The app fetches it, compares
 * [versionCode] with its own, and offers the download when the published build is newer.
 *
 * Keep the schema additive: older installed builds ignore unknown keys, so new fields can
 * be added without breaking the updater that is already on a friend's phone.
 */
@Serializable
data class UpdateManifest(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val sha256: String,
    val notes: List<String> = emptyList(),
    val publishedAt: String? = null
)
