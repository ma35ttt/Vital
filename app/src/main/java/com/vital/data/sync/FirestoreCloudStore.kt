package com.vital.data.sync

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.vital.auth.awaitTask
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore-backed [CloudStore] using the free Spark tier. Every account's rows live under
 * `users/{uid}/{table}/{rowId}` as `{ json, updatedAt, deleted }`.
 *
 * With no Firebase project configured, [push] fails loudly (it is only ever called while signed
 * in) and [pull] returns nothing, so the app keeps working before google-services.json exists.
 */
@Singleton
class FirestoreCloudStore @Inject constructor(
    @ApplicationContext private val context: Context
) : CloudStore {

    private val firestore: FirebaseFirestore? =
        if (FirebaseApp.getApps(context).isNotEmpty()) FirebaseFirestore.getInstance() else null

    override suspend fun push(uid: String, table: String, rows: List<SyncRow>) {
        if (rows.isEmpty()) return
        val db = firestore ?: error("Firebase is not configured")
        rows.chunked(MAX_WRITES_PER_BATCH).forEach { chunk ->
            val batch = db.batch()
            val collection = db.collection(ROOT).document(uid).collection(table)
            chunk.forEach { row ->
                batch.set(
                    collection.document(row.id),
                    mapOf(
                        FIELD_JSON to row.json,
                        FIELD_UPDATED_AT to row.updatedAt,
                        FIELD_DELETED to row.deleted
                    )
                )
            }
            batch.commit().awaitTask()
        }
    }

    override suspend fun pull(uid: String, table: String, since: Long): List<SyncRow> {
        val db = firestore ?: return emptyList()
        val snapshot = db.collection(ROOT).document(uid).collection(table)
            .whereGreaterThan(FIELD_UPDATED_AT, since)
            .get()
            .awaitTask()
        return snapshot.documents.mapNotNull { doc ->
            val json = doc.getString(FIELD_JSON) ?: return@mapNotNull null
            SyncRow(
                id = doc.id,
                json = json,
                updatedAt = doc.getLong(FIELD_UPDATED_AT) ?: 0L,
                deleted = doc.getBoolean(FIELD_DELETED) ?: false
            )
        }
    }

    private companion object {
        const val ROOT = "users"
        const val FIELD_JSON = "json"
        const val FIELD_UPDATED_AT = "updatedAt"
        const val FIELD_DELETED = "deleted"

        // Firestore caps a write batch at 500 operations; leave headroom.
        const val MAX_WRITES_PER_BATCH = 450
    }
}
