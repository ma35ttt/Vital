package com.vital.data.sync

import com.vital.data.entity.SyncCursor
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncEngineTest {

    @Serializable
    private data class Row(
        val id: String,
        val value: Int,
        val updatedAt: Long,
        val deleted: Boolean = false,
        val secret: String? = null
    )

    private class FakeCloudStore : CloudStore {
        val data = mutableMapOf<String, MutableMap<String, SyncRow>>()
        var pushCalls = 0

        override suspend fun push(uid: String, table: String, rows: List<SyncRow>) {
            pushCalls++
            val bucket = data.getOrPut("$uid/$table") { mutableMapOf() }
            rows.forEach { bucket[it.id] = it }
        }

        override suspend fun pull(uid: String, table: String, since: Long): List<SyncRow> =
            data["$uid/$table"]?.values?.filter { it.updatedAt > since } ?: emptyList()
    }

    private class FakeCursorStore : SyncCursorStore {
        val map = mutableMapOf<String, SyncCursor>()
        override suspend fun cursor(tableName: String) = map[tableName] ?: SyncCursor(tableName)
        override suspend fun save(cursor: SyncCursor) {
            map[cursor.tableName] = cursor
        }
    }

    /** In-memory table mirroring the Room behaviour: upsert replaces by id. */
    private class FakeTable(
        preferCloudOnFirstSync: Boolean = false,
        private val stripSecret: Boolean = true
    ) {
        val rows = mutableListOf<Row>()
        val table = SyncTable(
            name = "rows",
            serializer = Row.serializer(),
            idOf = { it.id },
            updatedAtOf = { it.updatedAt },
            deletedOf = { it.deleted },
            readAll = { rows.toList() },
            upsertAll = { incoming ->
                incoming.forEach { row ->
                    rows.removeAll { it.id == row.id }
                    rows.add(row)
                }
            },
            toCloud = { if (stripSecret) it.copy(secret = null) else it },
            mergeFromCloud = { incoming, existing -> incoming.copy(secret = existing?.secret) },
            preferCloudOnFirstSync = preferCloudOnFirstSync
        )

        fun row(id: String): Row? = rows.firstOrNull { it.id == id }
    }

    private fun engine(
        cloud: FakeCloudStore,
        cursors: FakeCursorStore,
        table: SyncTable<*>
    ) = SyncEngine(cloud, cursors, SyncTables(listOf(table)))

    @Test
    fun `first sync pushes every local row`() = runBlocking {
        val cloud = FakeCloudStore()
        val cursors = FakeCursorStore()
        val fake = FakeTable()
        fake.rows += Row("a", 1, 100)
        fake.rows += Row("b", 2, 200)

        engine(cloud, cursors, fake.table).sync("uid")

        assertEquals(2, cloud.data["uid/rows"]?.size)
        assertEquals(200L, cursors.map["rows"]?.lastPushAt)
    }

    @Test
    fun `pull applies a newer cloud row`() = runBlocking {
        val cloud = FakeCloudStore()
        val cursors = FakeCursorStore()
        val fake = FakeTable()
        fake.rows += Row("a", 1, 100)
        cloud.data["uid/rows"] = mutableMapOf(
            "a" to SyncRow("a", fake.table.encode(Row("a", 99, 300)), 300, false)
        )

        engine(cloud, cursors, fake.table).sync("uid")

        assertEquals(99, fake.row("a")?.value)
    }

    @Test
    fun `an older cloud row does not overwrite a newer local row`() = runBlocking {
        val cloud = FakeCloudStore()
        val cursors = FakeCursorStore()
        val fake = FakeTable()
        fake.rows += Row("a", 1, 300)
        cloud.data["uid/rows"] = mutableMapOf(
            "a" to SyncRow("a", fake.table.encode(Row("a", 9, 100)), 100, false)
        )

        engine(cloud, cursors, fake.table).sync("uid")

        assertEquals(1, fake.row("a")?.value)
        // The newer local row wins and is pushed, replacing the cloud copy.
        val pushed = fake.table.decode(cloud.data["uid/rows"]!!["a"]!!.json)
        assertEquals(1, pushed.value)
    }

    @Test
    fun `local deletes propagate as tombstones`() = runBlocking {
        val cloud = FakeCloudStore()
        val cursors = FakeCursorStore()
        val fake = FakeTable()
        fake.rows += Row("a", 1, 300, deleted = true)

        engine(cloud, cursors, fake.table).sync("uid")

        assertTrue(cloud.data["uid/rows"]!!["a"]!!.deleted)
    }

    @Test
    fun `cloud tombstones delete the local row`() = runBlocking {
        val cloud = FakeCloudStore()
        val cursors = FakeCursorStore()
        val fake = FakeTable()
        fake.rows += Row("a", 1, 100, deleted = false)
        cloud.data["uid/rows"] = mutableMapOf(
            "a" to SyncRow("a", fake.table.encode(Row("a", 1, 500, deleted = true)), 500, true)
        )

        engine(cloud, cursors, fake.table).sync("uid")

        assertTrue(fake.row("a")!!.deleted)
    }

    @Test
    fun `secrets are stripped on upload and preserved locally on restore`() = runBlocking {
        val cloud = FakeCloudStore()
        val cursors = FakeCursorStore()
        val fake = FakeTable()
        fake.rows += Row("a", 1, 100, secret = "s3cret")

        engine(cloud, cursors, fake.table).sync("uid")

        // Uploaded copy carries no secret...
        assertNull(fake.table.decode(cloud.data["uid/rows"]!!["a"]!!.json).secret)
        // ...but the local row keeps it.
        assertEquals("s3cret", fake.row("a")?.secret)

        // A second device with its own secret restores cloud data but keeps its own secret.
        val restored = FakeTable()
        restored.rows += Row("a", 0, 50, secret = "local-only")
        engine(cloud, cursors, restored.table).sync("uid")
        assertEquals("local-only", restored.row("a")?.secret)
    }

    @Test
    fun `a second sync pushes nothing when nothing changed`() = runBlocking {
        val cloud = FakeCloudStore()
        val cursors = FakeCursorStore()
        val fake = FakeTable()
        fake.rows += Row("a", 1, 100)
        val sync = engine(cloud, cursors, fake.table)

        sync.sync("uid")
        val callsAfterFirst = cloud.pushCalls
        sync.sync("uid")

        assertEquals(callsAfterFirst, cloud.pushCalls)
    }

    @Test
    fun `preferCloudOnFirstSync restores the server row over a fresher local default`() = runBlocking {
        val cloud = FakeCloudStore()
        val cursors = FakeCursorStore()
        val fake = FakeTable(preferCloudOnFirstSync = true)
        // Fresh device: local row created "now" (300) is newer than the cloud's 100.
        fake.rows += Row("a", 1, 300)
        cloud.data["uid/rows"] = mutableMapOf(
            "a" to SyncRow("a", fake.table.encode(Row("a", 9, 100)), 100, false)
        )

        engine(cloud, cursors, fake.table).sync("uid")

        assertEquals(9, fake.row("a")?.value)
    }

    @Test
    fun `after the first sync normal last-writer-wins applies`() = runBlocking {
        val cloud = FakeCloudStore()
        val cursors = FakeCursorStore()
        val fake = FakeTable(preferCloudOnFirstSync = true)
        fake.rows += Row("a", 1, 100)
        cloud.data["uid/rows"] = mutableMapOf(
            "a" to SyncRow("a", fake.table.encode(Row("a", 1, 100)), 100, false)
        )
        val sync = engine(cloud, cursors, fake.table)
        sync.sync("uid")

        // A later local edit must win over the (now first-synced) cloud copy.
        fake.rows.removeAll { it.id == "a" }
        fake.rows += Row("a", 42, 400)
        sync.sync("uid")

        assertEquals(42, fake.table.decode(cloud.data["uid/rows"]!!["a"]!!.json).value)
        assertFalse(cloud.data["uid/rows"]!!["a"]!!.deleted)
    }
}
