package com.byga.flowmanager.organizer.ui

import android.app.Activity
import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.byga.flowmanager.BygaApplication
import com.byga.flowmanager.domain.IdentityRules
import com.byga.flowmanager.domain.LaunchMethod
import com.byga.flowmanager.domain.Observation
import com.byga.flowmanager.domain.Outcome
import com.byga.flowmanager.launcher.Provider
import com.byga.flowmanager.organizer.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction

data class DashboardState(
    val loading: Boolean = true, val busy: Boolean = false, val failed: Boolean = false,
    val accounts: List<ManagedAccount> = emptyList(), val groups: List<AccountGroup> = emptyList(),
    val launches: List<LaunchEntry> = emptyList(), val providers: List<Provider> = emptyList(),
    val settings: DashboardSettings = DashboardSettings(), val query: String = "", val favoritesOnly: Boolean = false,
    val preview: ImportPreview? = null, val pickedEmail: String? = null, val message: String? = null,
    val unlocked: Boolean = false
) {
    val visible: List<ManagedAccount> get() = OrganizerRules.filter(accounts, query, settings.groupId, favoritesOnly, settings.sort)
}

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as BygaApplication
    private val repo = app.organizer
    private val prefs = app.dashboardPreferences
    private val mutable = MutableStateFlow(DashboardState())
    val state = mutable.asStateFlow()
    private var observer: Job? = null
    init { reload() }

    fun reload() {
        observer?.cancel()
        mutable.update { it.copy(loading = true, failed = false) }
        observer = viewModelScope.launch {
            combine(repo.accounts, repo.groups, repo.launches, prefs.settings) { accounts, groups, launches, settings ->
                DashboardState(loading = false, accounts = accounts, groups = groups, launches = launches,
                    settings = settings.copy(groupId = settings.groupId?.takeIf { id -> groups.any { it.id == id } }))
            }.flowOn(Dispatchers.IO).catch {
                mutable.update { it.copy(loading = false, failed = true, message = "Metadata tidak dapat dibaca. Coba muat ulang; kunci/data yang rusak tidak dipulihkan sebagai plaintext.") }
            }.collect { data -> mutable.update { it.copy(loading = false, failed = false,
                accounts = data.accounts, groups = data.groups, launches = data.launches, settings = data.settings) } }
        }
        refreshBrowsers()
    }

    fun query(value: String) { mutable.update { it.copy(query = value) } }
    fun favorites(value: Boolean) { mutable.update { it.copy(favoritesOnly = value) } }
    fun notify(message: String) { mutable.update { it.copy(message = message) } }
    fun dismissMessage() { mutable.update { it.copy(message = null) } }
    fun pickedEmail(email: String?) {
        if (email != null && IdentityRules.isValid(email)) mutable.update { it.copy(pickedEmail = IdentityRules.normalize(email)) }
        else notify("Pemilih akun tidak mengembalikan alamat email yang valid. Gunakan tambah manual.")
    }
    fun consumePickedEmail() { mutable.update { it.copy(pickedEmail = null) } }
    fun refreshBrowsers() = operation("Daftar browser tidak dapat dibaca.", false) {
        val browsers = withContext(Dispatchers.IO) { app.launcher.browsers() }
        mutable.update { it.copy(providers = browsers) }
    }
    fun display(mode: DisplayMode) = operation("Tampilan gagal disimpan.") { prefs.display(mode) }
    fun sort(mode: SortMode) = operation("Urutan gagal disimpan.") { prefs.sort(mode) }
    fun filterGroup(id: String?) = operation("Filter grup gagal disimpan.") { prefs.group(id) }
    fun browser(packageName: String) = operation("Browser gagal disimpan.") {
        require(withContext(Dispatchers.IO) { app.launcher.browsers().any { it.packageName == packageName } })
        prefs.browser(packageName)
    }
    fun setLock(enabled: Boolean) = operation("Pengaturan kunci gagal disimpan.") { prefs.lock(enabled); unlock() }
    fun unlock() { mutable.update { it.copy(unlocked = true) } }
    fun lockNow() { mutable.update { it.copy(unlocked = false) } }

    fun save(account: ManagedAccount, done: () -> Unit) = operation("Akun gagal disimpan. Periksa email, duplikat, alias dan grup.") {
        withContext(Dispatchers.IO) { repo.save(account) }; done()
    }
    fun favorite(account: ManagedAccount) = operation("Favorit gagal disimpan.") {
        withContext(Dispatchers.IO) { repo.favorite(account.id, !account.favorite) }
    }
    fun delete(ids: Set<String>, done: () -> Unit) = operation("Metadata akun gagal dihapus.") {
        withContext(Dispatchers.IO) { repo.delete(ids.toList()) }; done()
        notify("Metadata BYGA dihapus. Akun Google dan sesi browser tidak diubah.")
    }
    fun move(ids: Set<String>, group: String?, done: () -> Unit) = operation("Akun gagal dipindahkan ke grup.") {
        withContext(Dispatchers.IO) { repo.move(ids.toList(), group) }; done()
    }
    fun saveGroup(id: String?, name: String, done: () -> Unit) = operation("Grup gagal disimpan. Periksa nama atau duplikat grup.") {
        withContext(Dispatchers.IO) { repo.group(id, name) }; done()
    }
    fun deleteGroup(id: String) = operation("Grup gagal dihapus.") {
        withContext(Dispatchers.IO) { repo.deleteGroup(id) }
        if (state.value.settings.groupId == id) prefs.group(null)
        notify("Grup dihapus; akun di dalamnya tetap tersimpan tanpa grup.")
    }

    fun importDocument(uri: Uri) = operation("File tidak dapat diimpor. Gunakan UTF-8 CSV email,alias,group atau TXT satu email per baris; maksimal 2 MB/5.000 akun, tanpa kolom rahasia.") {
        val preview = withContext(Dispatchers.IO) {
            val name = app.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }.orEmpty()
            require(name.endsWith(".csv", true) || name.endsWith(".txt", true))
            val input = requireNotNull(app.contentResolver.openInputStream(uri))
            val bytes = input.use {
                val output = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                while (output.size() <= AccountImport.MAX_BYTES) {
                    val read = it.read(buffer, 0, minOf(buffer.size, AccountImport.MAX_BYTES + 1 - output.size()))
                    if (read < 0) break
                    output.write(buffer, 0, read)
                }
                output.toByteArray()
            }
            require(bytes.size <= AccountImport.MAX_BYTES)
            val text = Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString()
            AccountImport.preview(text, name.endsWith(".csv", true), state.value.accounts.map { it.email }.toSet())
        }
        mutable.update { it.copy(preview = preview) }
    }
    fun previewPocAccounts() = operation("Metadata PoC tidak dapat dibaca.") {
        val existing = state.value.accounts.map { it.email }.toSet()
        val slots = withContext(Dispatchers.IO) { app.repository.slots.first() }
        val text = "email,alias,group\n" + slots.filter { it.email.isNotBlank() }.joinToString("\n") { "${it.email},Account ${it.id},PoC" }
        val preview = AccountImport.preview(text, true, existing)
        mutable.update { it.copy(preview = preview) }
    }
    fun cancelImport() { if (!state.value.busy) mutable.update { it.copy(preview = null) } }
    fun commitImport() = operation("Impor gagal disimpan. Tidak ada perubahan parsial yang diterapkan.") {
        val candidates = requireNotNull(state.value.preview).valid
        val result = withContext(Dispatchers.IO) { repo.importAccounts(candidates) }
        mutable.update { it.copy(preview = null, message = "${result.inserted} akun ditambahkan; ${result.skipped} duplikat terbaru dilewati. Ini metadata, bukan login Google.") }
    }
    fun exportCsv(uri: Uri) = operation("Ekspor gagal. Pilih lokasi yang dapat ditulis.") {
        withContext(Dispatchers.IO) {
            val accounts = repo.accounts.first()
            val groups = repo.groups.first()
            val output = requireNotNull(app.contentResolver.openOutputStream(uri, "wt"))
            output.bufferedWriter(Charsets.UTF_8).use { it.write(AccountImport.csv(accounts, groups)) }
        }
        notify("Daftar email, alias dan grup diekspor. File metadata ini berisi email; simpan di lokasi pribadi.")
    }

    fun launch(activity: Activity, account: ManagedAccount) = operation("Pembukaan tidak selesai. Identitas Flow tetap belum diketahui.") {
        val browser = state.value.settings.browser
        require(browser.isNotBlank())
        val capability = withContext(Dispatchers.IO) { app.launcher.capability(LaunchMethod.BROWSER, browser) }
        val entry = withContext(Dispatchers.IO) { repo.begin(account.id, browser, capability.version, account.email) }
        val result = app.launcher.open(activity, LaunchMethod.BROWSER, browser)
        withContext(Dispatchers.IO) { repo.finish(entry, result.outcome, result.code) }
        notify(if (result.outcome == Outcome.UNVERIFIED) "Flow dibuka. Pilih akun yang dimaksud secara manual di Flow, lalu periksa identitasnya."
            else "Browser pilihan tidak dapat membuka Flow. Tidak ada pengalihan ke browser lain.")
    }
    fun review(id: String, observation: Observation, observed: String, done: () -> Unit) = operation("Konfirmasi ditolak. Periksa email aktual dan pembukaan terbaru.") {
        withContext(Dispatchers.IO) { repo.review(id, observation, observed) }; done()
    }
    private fun operation(failure: String, busy: Boolean = true, block: suspend () -> Unit) {
        if (busy && state.value.busy) return
        if (busy) mutable.update { it.copy(busy = true) }
        viewModelScope.launch {
            try { block() }
            catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (_: Exception) { notify(failure) }
            finally { if (busy) mutable.update { it.copy(busy = false) } }
        }
    }
}
