package com.vital.data.sync

import com.vital.data.entity.SyncCursor
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Last-writer-wins sync over a set of tables.
 *
 * For each table it pulls cloud rows newer than the last pull, applies any that beat the local
 * row (by `updatedAt`), then pushes local rows changed since the last push (including tombstones
 * for deletes). Rows carry the same ids on every device, so a fresh device converges to the same
 * data. Cursors advance only after a table completes, so a failed pass is retried rather than
 * skipped.
 */
@Singleton
class SyncEngine @Inject constructor(
    private val cloudStore: CloudStore,
    private val cursorStore: SyncCursorStore,
    private val tables: SyncTables
) {

    suspend fun sync(uid: String): SyncSummary = mutex.withLock {
        var pushed = 0
        var pulled = 0
        tables.all.forEach { table ->
            val summary = syncOne(uid, table)
            pushed += summary.pushed
            pulled += summary.pulled
        }
        SyncSummary(pushed = pushed, pulled = pulled)
    }

    private val mutex = Mutex()

    @Suppress("UNCHECKED_CAST")
    private suspend fun syncOne(uid: String, table: SyncTable<*>): SyncSummary =
        syncTyped(uid, table as SyncTable<Any>)

    private suspend fun <T : Any> syncTyped(uid: String, table: SyncTable<T>): SyncSummary {
        val cursor = cursorStore.cursor(table.name)
        val local = table.readAll().associateBy { table.idOf(it) }.toMutableMap()

        // Pull: apply cloud rows that are newer than what we hold (or are new to us). On a
        // device's first sync, singleton rows flagged preferCloudOnFirstSync let the cloud win
        // even against a fresher-looking local default.
        val firstSync = cursor.lastPullAt == 0L && cursor.lastPushAt == 0L
        val cloudRows = cloudStore.pull(uid, table.name, cursor.lastPullAt)
        var maxPulledAt = cursor.lastPullAt
        val toApply = ArrayList<T>(cloudRows.size)
        for (row in cloudRows) {
            maxPulledAt = maxOf(maxPulledAt, row.updatedAt)
            val existing = local[row.id]
            val existingAt = existing?.let { table.updatedAtOf(it) }
            val cloudWins = existingAt == null ||
                row.updatedAt > existingAt ||
                (firstSync && table.preferCloudOnFirstSync)
            if (cloudWins) {
                val merged = table.mergeFromCloud(table.decode(row.json), existing)
                toApply += merged
                local[row.id] = merged
            }
        }
        if (toApply.isNotEmpty()) table.upsertAll(toApply)

        // Push: send anything changed locally since the last push (tombstones included).
        val toPush = local.values
            .filter { table.updatedAtOf(it) > cursor.lastPushAt }
            .map { table.toCloud(it) }
        if (toPush.isNotEmpty()) {
            cloudStore.push(
                uid,
                table.name,
                toPush.map {
                    SyncRow(
                        id = table.idOf(it),
                        json = table.encode(it),
                        updatedAt = table.updatedAtOf(it),
                        deleted = table.deletedOf(it)
                    )
                }
            )
        }

        val maxLocalAt = local.values.maxOfOrNull { table.updatedAtOf(it) } ?: 0L
        cursorStore.save(
            SyncCursor(
                tableName = table.name,
                lastPullAt = maxPulledAt,
                lastPushAt = maxOf(cursor.lastPushAt, maxLocalAt)
            )
        )
        return SyncSummary(pushed = toPush.size, pulled = toApply.size)
    }
}
