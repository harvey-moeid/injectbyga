package com.byga.flowmanager

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.byga.flowmanager.data.PocDatabase
import com.byga.flowmanager.data.PocRepository
import com.byga.flowmanager.domain.*
import com.byga.flowmanager.security.MetadataCipher
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Local persistence tests only. These do NOT test Google login or session isolation. */
@RunWith(AndroidJUnit4::class)
class PocPersistenceTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun realKeystoreMetadataSurvivesDatabaseReopen() = runBlocking {
        val name = "poc-persistence-test.db"
        context.deleteDatabase(name)
        var db = Room.databaseBuilder(context, PocDatabase::class.java, name).build()
        try {
            var repo = PocRepository(db, MetadataCipher())
            repo.save(AccountSlot("A", "a@example.com"))
            repo.begin(trial("trial-1", "A", "a@example.com", 1))
            repo.finishLaunch("trial-1", Outcome.UNVERIFIED, null)
            repo.review("trial-1", Observation.IDENTITY, "a@example.com")
            db.close()
            db = Room.databaseBuilder(context, PocDatabase::class.java, name).build()
            repo = PocRepository(db, MetadataCipher())
            assertEquals(Outcome.VERIFIED_MANUAL, repo.allTrials().single().outcome)
            assertEquals("a@example.com", repo.allTrials().single().observed)
            assertFalse(db.dao().slots().single().emailCipher.contains("a@example.com"))
        } finally { db.close(); context.deleteDatabase(name) }
    }

    @Test fun newestLaunchInvalidatesOlderPendingConfirmationAndRejectsStaleDialog() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, PocDatabase::class.java).build()
        try {
            val repo = PocRepository(db, MetadataCipher())
            // Wall clock changes must not allow an older trial to become the active confirmation.
            repo.begin(trial("old", "A", "a@example.com", 100))
            repo.finishLaunch("old", Outcome.UNVERIFIED, null)
            repo.begin(trial("new", "B", "b@example.com", 2))
            repo.finishLaunch("new", Outcome.UNVERIFIED, null)
            var rejected = false
            try { repo.review("old", Observation.IDENTITY, "a@example.com") }
            catch (_: IllegalArgumentException) { rejected = true }
            assertTrue(rejected)
            repo.review("new", Observation.IDENTITY, "a@example.com")
            val results = repo.allTrials()
            assertEquals(Outcome.MISMATCH, results.first().outcome)
            assertEquals(Outcome.ABANDONED, results.last().outcome)
        } finally { db.close() }
    }
    private fun trial(id: String, slot: String, expected: String, time: Long) = Trial(id, slot, expected,
        LaunchMethod.NATIVE, "native", "test", "PERSONAL", "ABC", "run", time, Outcome.STARTING)
}
