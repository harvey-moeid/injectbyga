package com.byga.flowmanager.organizer.domain

import com.byga.flowmanager.domain.IdentityRules
import com.byga.flowmanager.domain.Outcome
import java.util.Locale

data class ManagedAccount(
    val id: String, val email: String, val alias: String, val groupId: String? = null,
    val favorite: Boolean = false, val note: String = "", val color: Int = 0,
    val createdAt: Long = 0, val updatedAt: Long = 0, val lastOpenedAt: Long? = null
)
data class AccountGroup(val id: String, val name: String)
data class LaunchEntry(
    val id: String, val accountId: String, val expected: String, val alias: String,
    val provider: String, val providerVersion: String, val startedAt: Long,
    val outcome: Outcome, val observed: String? = null, val reviewedAt: Long? = null,
    val errorCode: String? = null
)
enum class DisplayMode(val title: String) { LIST("Daftar"), GRID("Grid") }
enum class SortMode(val title: String) { NAME("Nama"), EMAIL("Email"), RECENT("Terakhir dibuka"), FAVORITE("Favorit dahulu") }
data class DashboardSettings(
    val displayMode: DisplayMode = DisplayMode.LIST, val sort: SortMode = SortMode.NAME,
    val groupId: String? = null, val browser: String = "", val lockEnabled: Boolean = false
)
data class ImportCandidate(val email: String, val alias: String, val group: String = "")
data class ImportIssue(val line: Int, val reason: String)
data class ImportPreview(
    val valid: List<ImportCandidate>, val rejected: List<ImportIssue>, val duplicates: List<ImportIssue>, val total: Int
)
data class ImportResult(val inserted: Int, val skipped: Int)

object OrganizerRules {
    fun validate(email: String, alias: String, note: String = "") {
        require(IdentityRules.isValid(email)) { "Alamat email tidak valid." }
        require(alias.trim().length in 1..80 && alias.none { it.isISOControl() }) { "Alias wajib diisi, maksimal 80 karakter." }
        require(note.length <= 1000) { "Catatan maksimal 1.000 karakter." }
    }
    fun validGroup(name: String): Boolean = name.trim().length in 1..60 && name.none { it.isISOControl() }
    fun defaultAlias(email: String): String = email.substringBefore('@').take(80)
    fun filter(
        accounts: List<ManagedAccount>, query: String, group: String?, favoritesOnly: Boolean, sort: SortMode
    ): List<ManagedAccount> {
        val q = query.trim().lowercase(Locale.ROOT)
        val filtered = accounts.filter {
            (!favoritesOnly || it.favorite) && (group == null || it.groupId == group) &&
                (q.isEmpty() || it.alias.lowercase(Locale.ROOT).contains(q) || it.email.lowercase(Locale.ROOT).contains(q))
        }
        val byName = compareBy<ManagedAccount> { it.alias.lowercase(Locale.ROOT) }.thenBy { it.email }.thenBy { it.id }
        return filtered.sortedWith(when (sort) {
            SortMode.NAME -> byName
            SortMode.EMAIL -> compareBy<ManagedAccount> { it.email }.then(byName)
            SortMode.RECENT -> compareByDescending<ManagedAccount> { it.lastOpenedAt ?: Long.MIN_VALUE }.then(byName)
            SortMode.FAVORITE -> compareByDescending<ManagedAccount> { it.favorite }.then(byName)
        })
    }
}
