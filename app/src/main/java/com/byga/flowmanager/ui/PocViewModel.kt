package com.byga.flowmanager.ui

import android.app.Activity
import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.byga.flowmanager.BygaApplication
import com.byga.flowmanager.data.EvidenceExport
import com.byga.flowmanager.data.PocSettings
import com.byga.flowmanager.domain.*
import com.byga.flowmanager.launcher.Provider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class PocUiState(
    val loading: Boolean = true,
    val busy: Boolean = false,
    val slots: List<AccountSlot> = emptyList(),
    val trials: List<Trial> = emptyList(),
    val settings: PocSettings = PocSettings(),
    val providers: List<Provider> = emptyList(),
    val error: String? = null,
    val message: String? = null
)

class PocViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as BygaApplication
    private val repository = app.repository
    private val preferences = app.preferences
    val launcher = app.launcher
    private val mutable = MutableStateFlow(PocUiState())
    val state = mutable.asStateFlow()
    private var observing: Job? = null
    init { reload() }

    fun reload() {
        observing?.cancel()
        mutable.update { it.copy(loading = true, error = null) }
        observing = viewModelScope.launch {
            combine(repository.slots, repository.trials, preferences.settings) { slots, trials, settings ->
                Triple(slots, trials, settings)
            }.flowOn(Dispatchers.IO).catch {
                mutable.update { it.copy(loading = false, error = "Metadata tidak dapat dibaca. Coba lagi atau hapus data PoC melalui Pengaturan Android. Detail pribadi tidak dicatat.") }
            }.collect { (slots, trials, settings) ->
                mutable.update { it.copy(loading = false, slots = slots, trials = trials, settings = settings) }
            }
        }
        refreshProviders()
    }

    fun refreshProviders() = operation(reportFailure = "Daftar provider tidak dapat dibaca.", busy = false) {
        val providers = withContext(Dispatchers.IO) { launcher.browsers() }
        mutable.update { it.copy(providers = providers) }
    }

    fun save(slot: AccountSlot, done: () -> Unit) = operation("Konfigurasi gagal disimpan. Periksa email dan duplikat.") {
        withContext(Dispatchers.IO) { repository.save(slot) }
        done()
    }

    fun launch(activity: Activity, slot: AccountSlot) = operation("Pembukaan tidak selesai. Status identitas tetap belum diketahui.") {
        require(IdentityRules.isValid(slot.email))
        val capability = withContext(Dispatchers.IO) { launcher.capability(slot.method, slot.provider) }
        val settings = state.value.settings
        val trial = Trial(UUID.randomUUID().toString(), slot.id, slot.email, slot.method, capability.provider,
            capability.version, launcher.profile, settings.scenario.name, settings.run, System.currentTimeMillis(), Outcome.STARTING)
        withContext(Dispatchers.IO) { repository.begin(trial) }
        val result = launcher.open(activity, slot.method, slot.provider)
        withContext(Dispatchers.IO) { repository.finishLaunch(trial.id, result.outcome, result.code) }
        mutable.update { it.copy(message = if (result.outcome == Outcome.UNVERIFIED)
            "Tautan dikirim. Periksa email aktif di Flow, lalu kembali untuk mencatat hasil."
            else "Metode tidak dapat dibuka. Cek konfigurasi provider dan profil; tidak ada fallback otomatis.") }
    }

    fun review(id: String, observation: Observation, observed: String, done: () -> Unit) = operation("Konfirmasi ditolak. Periksa email yang terlihat dan percobaan terbaru.") {
        withContext(Dispatchers.IO) { repository.review(id, observation, observed) }
        done()
    }
    fun newRun(scenario: Scenario) = operation("Seri uji gagal disimpan.") { preferences.newRun(scenario) }
    fun showGuide(value: Boolean) = operation("Preferensi gagal disimpan.") { preferences.showGuide(value) }

    fun export(uri: Uri) = operation("Ekspor gagal. Pilih lokasi file yang dapat ditulis.") {
        withContext(Dispatchers.IO) {
            val report = EvidenceExport.json(repository.allTrials())
            val output = app.contentResolver.openOutputStream(uri, "wt") ?: error("No output stream")
            output.bufferedWriter(Charsets.UTF_8).use { it.write(report) }
        }
        mutable.update { it.copy(message = "Laporan JSON tersimpan tanpa email. Gate tetap UNVERIFIED.") }
    }
    fun clear() = operation("Data PoC gagal dihapus.") {
        withContext(Dispatchers.IO) { repository.clear() }
        preferences.newRun(Scenario.ABC)
        mutable.update { it.copy(message = "Metadata BYGA dihapus. Akun dan sesi Google tidak diubah.") }
    }
    fun dismissMessage() { mutable.update { it.copy(message = null, error = null) } }

    private fun operation(reportFailure: String, busy: Boolean = true, block: suspend () -> Unit) {
        if (busy && state.value.busy) return
        if (busy) mutable.update { it.copy(busy = true) }
        viewModelScope.launch {
            try { block() }
            catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (_: Exception) { mutable.update { it.copy(error = reportFailure) } }
            finally { if (busy) mutable.update { it.copy(busy = false) } }
        }
    }
}
