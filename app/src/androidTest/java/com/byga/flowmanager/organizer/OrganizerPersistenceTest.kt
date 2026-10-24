package com.byga.flowmanager.organizer

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.byga.flowmanager.domain.Observation
import com.byga.flowmanager.domain.Outcome
import com.byga.flowmanager.organizer.data.OrganizerDatabase
import com.byga.flowmanager.organizer.data.OrganizerRepository
import com.byga.flowmanager.organizer.domain.*
import com.byga.flowmanager.security.MetadataCipher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OrganizerPersistenceTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun account(email: String, alias: String = "Test") = ManagedAccount("", email, alias)

    @Test fun fiftyMetadataAccountsSupportSearchBulkMoveAndDelete() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, OrganizerDatabase::class.java).build()
        try {
            val repo = OrganizerRepository(db, MetadataCipher())
            val imported = repo.importAccounts((1..50).map { ImportCandidate("acct$it@example.com", "Studio $it", "Awal") })
            assertEquals(50, imported.inserted)
            val all = repo.accounts.first()
            assertEquals(1, OrganizerRules.filter(all, "acct49@", null, false, SortMode.EMAIL).size)
            val group = repo.group(null, "Pindahan")
            val ids = all.take(20).map { it.id }
            repo.move(ids, group)
            assertEquals(20, repo.accounts.first().count { it.groupId == group })
            repo.delete(ids)
            assertEquals(30, repo.accounts.first().size)
        } finally { db.close() }
    }

    @Test fun encryptedMetadataGroupsFavoritesAndHistorySurviveReopen() = runBlocking {
        val name = "organizer-persistence-test.db"
        context.deleteDatabase(name)
        var db = Room.databaseBuilder(context, OrganizerDatabase::class.java, name).build()
        try {
            var repo = OrganizerRepository(db, MetadataCipher())
            val group = repo.group(null, "Tim Test")
            val id = repo.save(account("a@example.com", "A").copy(groupId = group, favorite = true, note = "Proyek lokal"))
            val launch = repo.begin(id, "com.android.chrome", "synthetic")
            repo.finish(launch, Outcome.UNVERIFIED, null)
            repo.review(launch.id, Observation.IDENTITY, "a@example.com")
            assertFalse(db.dao().accounts().single().metadataCipher.contains("a@example.com"))
            db.close()
            db = Room.databaseBuilder(context, OrganizerDatabase::class.java, name).build()
            repo = OrganizerRepository(db, MetadataCipher())
            val restored = repo.accounts.first().single()
            assertEquals("Proyek lokal", restored.note)
            assertTrue(restored.favorite)
            assertEquals(group, restored.groupId)
            assertEquals(launch.startedAt, restored.lastOpenedAt)
            assertEquals(Outcome.VERIFIED_MANUAL, repo.launches.first().single().outcome)
        } finally { db.close(); context.deleteDatabase(name) }
    }

    @Test fun importRechecksDuplicatesAndGroupDeletionPreservesAccounts() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, OrganizerDatabase::class.java).build()
        try {
            val repo = OrganizerRepository(db, MetadataCipher())
            repo.save(account("a@example.com"))
            val result = repo.importAccounts(listOf(ImportCandidate("A@example.com", "Duplicate"), ImportCandidate("b@example.com", "B", "Tim")))
            assertEquals(1, result.inserted)
            assertEquals(1, result.skipped)
            val group = repo.groups.first().single()
            repo.deleteGroup(group.id)
            assertEquals(2, repo.accounts.first().size)
            assertTrue(repo.accounts.first().all { it.groupId == null })
            var rejected = false
            try { repo.save(account("B@EXAMPLE.COM")) } catch (_: IllegalArgumentException) { rejected = true }
            assertTrue(rejected)
        } finally { db.close() }
    }

    @Test fun browserIsSharedButIdentitiesAreNeverAutomaticallyVerifiedAndDeleteCascadesHistory() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, OrganizerDatabase::class.java).build()
        try {
            val repo = OrganizerRepository(db, MetadataCipher())
            val a = repo.save(account("a@example.com", "A"))
            val b = repo.save(account("b@example.com", "B"))
            val first = repo.begin(a, "com.android.chrome", "synthetic")
            repo.finish(first, Outcome.UNVERIFIED, null)
            val second = repo.begin(b, "com.android.chrome", "synthetic")
            repo.finish(second, Outcome.UNVERIFIED, null)
            var rejected = false
            try { repo.review(first.id, Observation.IDENTITY, "a@example.com") } catch (_: IllegalArgumentException) { rejected = true }
            assertTrue(rejected)
            repo.review(second.id, Observation.IDENTITY, "a@example.com")
            assertEquals(Outcome.MISMATCH, repo.launches.first().first().outcome)
            assertEquals(Outcome.ABANDONED, repo.launches.first().last().outcome)
            repo.delete(listOf(b))
            assertTrue(repo.launches.first().none { it.accountId == b })
            assertEquals(1, repo.accounts.first().size)
        } finally { db.close() }
    }

    @Test fun failedLaunchDoesNotChangeLastOpenedAndEditedTargetRejectsStaleLaunch() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, OrganizerDatabase::class.java).build()
        try {
            val repo = OrganizerRepository(db, MetadataCipher())
            val id = repo.save(account("a@example.com", "A"))
            val failed = repo.begin(id, "missing.browser", "")
            repo.finish(failed, Outcome.BLOCKED, "PROVIDER_UNAVAILABLE")
            assertNull(repo.accounts.first().single().lastOpenedAt)
            repo.save(repo.accounts.first().single().copy(email = "new@example.com"))
            var rejected = false
            try { repo.begin(id, "com.android.chrome", "synthetic", "a@example.com") } catch (_: IllegalArgumentException) { rejected = true }
            assertTrue(rejected)
        } finally { db.close() }
    }
}
