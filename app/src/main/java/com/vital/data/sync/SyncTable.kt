package com.vital.data.sync

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * Describes how one Room table participates in sync. The read/upsert functions are supplied by
 * the caller (DAOs in production, in-memory lists in tests), which keeps [SyncEngine] free of
 * database and Firebase types.
 *
 * @param toCloud sanitizes a local row before it is uploaded (e.g. strips secrets).
 * @param mergeFromCloud combines a newer cloud row with the existing local row, so fields that
 *   are deliberately never uploaded (like a PIN hash) are preserved locally.
 * @param preferCloudOnFirstSync for singleton rows (profile, settings) that always exist locally
 *   with a fresh timestamp: on a device's very first sync the cloud copy wins even if the local
 *   default looks newer, so signing in on a new phone restores the real profile instead of
 *   overwriting the cloud with defaults.
 */
class SyncTable<T : Any>(
    val name: String,
    private val serializer: KSerializer<T>,
    val idOf: (T) -> String,
    val updatedAtOf: (T) -> Long,
    val deletedOf: (T) -> Boolean,
    val readAll: suspend () -> List<T>,
    val upsertAll: suspend (List<T>) -> Unit,
    val toCloud: (T) -> T = { it },
    val mergeFromCloud: (incoming: T, existing: T?) -> T = { incoming, _ -> incoming },
    val preferCloudOnFirstSync: Boolean = false
) {
    fun encode(value: T): String = json.encodeToString(serializer, value)

    fun decode(text: String): T = json.decodeFromString(serializer, text)

    private companion object {
        val json = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }
    }
}
