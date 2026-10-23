package com.byga.flowmanager.data

import androidx.room.withTransaction
import com.byga.flowmanager.domain.*
import com.byga.flowmanager.security.MetadataCipher
import kotlinx.coroutines.flow.map

class PocRepository(private val db: PocDatabase, private val cipher: MetadataCipher) {
    private val dao = db.dao()
    val slots = dao.observeSlots().map { rows ->
        listOf("A", "B", "C").map { id -> rows.find { it.id == id }?.decode() ?: AccountSlot(id) }
    }
    val trials = dao.observeTrials().map { rows -> rows.map { it.decode() } }

    suspend fun save(slot: AccountSlot) = db.withTransaction {
        require(slot.id in listOf("A", "B", "C"))
        require(IdentityRules.isValid(slot.email)) { "Alamat email tidak valid." }
        val normalized = IdentityRules.normalize(slot.email)
        require(dao.slots().none { it.id != slot.id && it.decode().email == normalized }) { "Email sudah digunakan slot lain." }
        dao.saveSlot(SlotEntity(slot.id, cipher.encrypt(normalized, "slot:${slot.id}"), slot.method.name, slot.provider))
    }

    suspend fun begin(trial: Trial) = db.withTransaction {
        dao.abandonPending()
        dao.insertTrial(TrialEntity(trial.id, trial.slot, cipher.encrypt(trial.expected, "expected:${trial.id}"),
            trial.method.name, trial.provider, trial.providerVersion, trial.profile, trial.scenario, trial.run,
            trial.startedAt, Outcome.STARTING.name))
    }
    suspend fun finishLaunch(id: String, outcome: Outcome, code: String?) = dao.finishLaunch(id, outcome.name, code)

    suspend fun review(id: String, observation: Observation, observed: String) = db.withTransaction {
        val latest = requireNotNull(dao.latest()?.decode()) { "Percobaan tidak tersedia." }
        require(latest.id == id) { "Percobaan sudah berubah; periksa pembukaan terbaru." }
        val outcome = IdentityRules.review(latest.outcome, latest.expected, observed, observation)
        val encrypted = if (observation == Observation.IDENTITY)
            cipher.encrypt(IdentityRules.normalize(observed), "observed:$id") else null
        check(dao.review(id, outcome.name, encrypted, System.currentTimeMillis()) == 1)
    }

    suspend fun allTrials(): List<Trial> = dao.trials().map { it.decode() }
    suspend fun clear() = db.withTransaction { dao.deleteTrials(); dao.deleteSlots() }

    private fun SlotEntity.decode() = AccountSlot(id, cipher.decrypt(emailCipher, "slot:$id"), LaunchMethod.valueOf(method), provider)
    private fun TrialEntity.decode() = Trial(id, slot, cipher.decrypt(expectedCipher, "expected:$id"),
        LaunchMethod.valueOf(method), provider, providerVersion, profile, scenario, run, startedAt,
        Outcome.valueOf(outcome), reviewedAt, observedCipher?.let { cipher.decrypt(it, "observed:$id") }, errorCode)
}
