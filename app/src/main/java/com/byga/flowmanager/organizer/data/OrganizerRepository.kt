package com.byga.flowmanager.organizer.data

import androidx.room.withTransaction
import com.byga.flowmanager.domain.IdentityRules
import com.byga.flowmanager.domain.Observation
import com.byga.flowmanager.domain.Outcome
import com.byga.flowmanager.organizer.domain.*
import com.byga.flowmanager.security.MetadataCipher
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import java.util.UUID

class OrganizerRepository(private val db: OrganizerDatabase, private val cipher: MetadataCipher) {
    private val dao = db.dao()
    val accounts = dao.observeAccounts().map { it.map { row -> row.decode() } }
    val groups = dao.observeGroups().map { it.map { row -> row.decode() } }
    val launches = dao.observeLaunches().map { it.map { row -> row.decode() } }

    suspend fun save(account: ManagedAccount): String = db.withTransaction {
        OrganizerRules.validate(account.email, account.alias, account.note)
        val email = IdentityRules.normalize(account.email)
        val existing = dao.accounts().map { it.decode() }
        require(existing.none { it.id != account.id && it.email == email }) { "Email sudah ada pada dashboard." }
        require(account.groupId == null || dao.groups().any { it.id == account.groupId }) { "Grup sudah dihapus." }
        val old = existing.find { it.id == account.id }
        require(account.id.isEmpty() || old != null) { "Akun sudah dihapus." }
        val id = old?.id ?: UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val metadata = JSONObject().put("email", email).put("alias", account.alias.trim()).put("note", account.note.trim()).toString()
        dao.saveAccount(AccountRow(id, cipher.encrypt(metadata, "account:$id"), account.groupId, account.favorite,
            account.color.coerceIn(0, 5), old?.createdAt ?: now, now, old?.lastOpenedAt))
        id
    }

    suspend fun favorite(id: String, value: Boolean) = dao.favorite(id, value, System.currentTimeMillis())
    suspend fun delete(ids: List<String>) = dao.deleteAccounts(ids)
    suspend fun move(ids: List<String>, group: String?) = db.withTransaction {
        require(group == null || dao.groups().any { it.id == group }) { "Grup sudah dihapus." }
        dao.move(ids, group, System.currentTimeMillis())
    }
    suspend fun group(id: String?, name: String): String = db.withTransaction {
        require(OrganizerRules.validGroup(name)) { "Nama grup wajib diisi, maksimal 60 karakter." }
        val existing = dao.groups().map { it.decode() }
        require(id == null || existing.any { it.id == id }) { "Grup sudah dihapus." }
        require(existing.none { it.id != id && it.name.equals(name.trim(), ignoreCase = true) }) { "Nama grup sudah digunakan." }
        val key = id ?: UUID.randomUUID().toString()
        dao.saveGroup(GroupRow(key, cipher.encrypt(name.trim(), "group:$key")))
        key
    }
    suspend fun deleteGroup(id: String) = dao.deleteGroup(id)

    suspend fun importAccounts(candidates: List<ImportCandidate>): ImportResult = db.withTransaction {
        require(candidates.size <= AccountImport.MAX_ROWS)
        val seen = dao.accounts().map { it.decode().email }.toMutableSet()
        val availableGroups = dao.groups().map { it.decode() }.toMutableList()
        var inserted = 0
        var skipped = 0
        for (candidate in candidates) {
            OrganizerRules.validate(candidate.email, candidate.alias)
            val email = IdentityRules.normalize(candidate.email)
            if (!seen.add(email)) { skipped++; continue }
            val groupId = candidate.group.trim().takeIf { it.isNotEmpty() }?.let { name ->
                require(OrganizerRules.validGroup(name))
                availableGroups.find { it.name.equals(name, ignoreCase = true) }?.id ?: run {
                    val id = UUID.randomUUID().toString()
                    dao.saveGroup(GroupRow(id, cipher.encrypt(name, "group:$id")))
                    availableGroups += AccountGroup(id, name)
                    id
                }
            }
            val id = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            val metadata = JSONObject().put("email", email).put("alias", candidate.alias.trim()).put("note", "").toString()
            dao.saveAccount(AccountRow(id, cipher.encrypt(metadata, "account:$id"), groupId, false, inserted % 6, now, now))
            inserted++
        }
        ImportResult(inserted, skipped)
    }

    suspend fun begin(accountId: String, browser: String, version: String, expectedEmail: String? = null): LaunchEntry = db.withTransaction {
        val account = requireNotNull(dao.accounts().find { it.id == accountId }) { "Akun sudah dihapus." }.decode()
        require(expectedEmail == null || IdentityRules.normalize(expectedEmail) == account.email) { "Identitas target berubah; buka dialog kembali." }
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val target = JSONObject().put("email", account.email).put("alias", account.alias).toString()
        dao.abandonPending()
        dao.insertLaunch(LaunchRow(id, account.id, cipher.encrypt(target, "launch:$id"), browser, version, now, Outcome.STARTING.name))
        LaunchEntry(id, account.id, account.email, account.alias, browser, version, now, Outcome.STARTING)
    }
    suspend fun finish(entry: LaunchEntry, outcome: Outcome, code: String?) = db.withTransaction {
        dao.finishLaunch(entry.id, outcome.name, code)
        if (outcome == Outcome.UNVERIFIED) dao.opened(entry.accountId, entry.startedAt)
    }
    suspend fun review(id: String, observation: Observation, observed: String) = db.withTransaction {
        val latest = requireNotNull(dao.latest()) { "Tidak ada pembukaan untuk dikonfirmasi." }.decode()
        require(latest.id == id) { "Pembukaan sudah berubah; periksa pembukaan terbaru." }
        val outcome = IdentityRules.review(latest.outcome, latest.expected, observed, observation)
        val encrypted = if (observation == Observation.IDENTITY) cipher.encrypt(IdentityRules.normalize(observed), "launch-observed:$id") else null
        check(dao.review(id, outcome.name, encrypted, System.currentTimeMillis()) == 1)
    }

    private fun AccountRow.decode(): ManagedAccount {
        val data = JSONObject(cipher.decrypt(metadataCipher, "account:$id"))
        return ManagedAccount(id, data.getString("email"), data.getString("alias"), groupId, favorite,
            data.optString("note"), color, createdAt, updatedAt, lastOpenedAt)
    }
    private fun GroupRow.decode() = AccountGroup(id, cipher.decrypt(nameCipher, "group:$id"))
    private fun LaunchRow.decode(): LaunchEntry {
        val data = JSONObject(cipher.decrypt(targetCipher, "launch:$id"))
        return LaunchEntry(id, accountId, data.getString("email"), data.getString("alias"), provider, providerVersion,
            startedAt, Outcome.valueOf(outcome), observedCipher?.let { cipher.decrypt(it, "launch-observed:$id") }, reviewedAt, errorCode)
    }
}
