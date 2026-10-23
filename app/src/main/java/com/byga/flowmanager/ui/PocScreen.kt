package com.byga.flowmanager.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.byga.flowmanager.domain.*
import com.byga.flowmanager.launcher.Provider
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val clockFormat = DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm:ss").withZone(ZoneId.systemDefault())
private fun time(value: Long) = clockFormat.format(Instant.ofEpochMilli(value))

@Composable
fun PocScreen(state: PocUiState, vm: PocViewModel, launch: (AccountSlot) -> Unit, export: () -> Unit) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    var editSlot by remember { mutableStateOf<AccountSlot?>(null) }
    var requested by remember { mutableStateOf<AccountSlot?>(null) }
    var reviewing by remember { mutableStateOf<Trial?>(null) }
    var clearDialog by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.message, state.error) {
        (state.error ?: state.message)?.let {
            snackbar.showSnackbar(it, duration = SnackbarDuration.Long)
            vm.dismissMessage()
        }
    }
    Scaffold(
        containerColor = Navy,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            NavigationBar(containerColor = Navy) {
                listOf("Eksperimen", "Riwayat", "Panduan").forEachIndexed { index, label ->
                    NavigationBarItem(selected = page == index, onClick = { page = index },
                        icon = { Text(listOf("◎", "≡", "i")[index], style = MaterialTheme.typography.titleLarge) },
                        label = { Text(label) }, colors = NavigationBarItemDefaults.colors(indicatorColor = Panel,
                            selectedIconColor = Cyan, selectedTextColor = Cyan))
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            LazyColumn(Modifier.widthIn(max = 840.dp).fillMaxWidth().testTag("poc-list"),
                contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item { BrandHeader() }
                item { GateCard() }
                if (state.busy) item { LinearProgressIndicator(Modifier.fillMaxWidth(), color = Cyan) }
                if (state.loading) {
                    item { Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Cyan)
                        Text("Memuat metadata lokal…", Modifier.padding(16.dp))
                    } }
                } else if (state.slots.isEmpty()) {
                    item { PanelCard {
                        Text("Metadata belum tersedia", style = MaterialTheme.typography.titleMedium)
                        Text("Coba muat ulang. Jika data tidak dapat dibaca, lihat pesan kesalahan.", color = Muted)
                        Button(onClick = vm::reload) { Text("Muat ulang") }
                    } }
                } else when (page) {
                    0 -> {
                        if (state.settings.showGuide) item { PanelCard {
                            Text("Uji identitas, satu pembukaan setiap kali", color = Cyan, fontWeight = FontWeight.SemiBold)
                            Text("Atur email A/B/C. Buka Flow, periksa email aktif sebelum mengganti akun di Flow, lalu kembali dan catat hasil. BYGA tidak melakukan login.", color = Muted)
                            TextButton(onClick = { vm.showGuide(false) }) { Text("Saya mengerti · sembunyikan") }
                        } }
                        item { ScenarioPanel(state, vm) }
                        val pending = state.trials.firstOrNull()?.takeIf { it.outcome == Outcome.UNVERIFIED }
                        if (pending != null) item { PanelCard(border = Amber) {
                            Text("MENUNGGU PEMERIKSAAN", color = Amber, style = MaterialTheme.typography.labelMedium)
                            Text("Account ${pending.slot} · ${pending.expected}", fontWeight = FontWeight.SemiBold)
                            Text("${pending.method.title} · ${time(pending.startedAt)}", color = Muted)
                            Text("Tautan terkirim tidak membuktikan akun yang aktif.", color = Muted)
                            Button(onClick = { reviewing = pending }, enabled = !state.busy,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Catat identitas yang terlihat") }
                        } }
                        items(state.slots, key = { it.id }) { slot ->
                            val previous = state.trials.firstOrNull { it.slot == slot.id }
                            AccountCard(slot, previous, state.busy, { editSlot = slot }, { requested = slot })
                        }
                        item { Text("Semua rute bersifat eksperimen. Tidak ada sesi Google yang disimpan atau dikendalikan BYGA.", color = Muted, style = MaterialTheme.typography.bodySmall) }
                    }
                    1 -> {
                        item { PanelCard {
                            Text("Catatan pembukaan", style = MaterialTheme.typography.titleLarge)
                            Text("${state.trials.size} percobaan tersimpan. Tampilan dibatasi 100 terbaru; ekspor mencakup semuanya. Konfirmasi selalu historis, bukan status sesi sekarang.", color = Muted)
                            OutlinedButton(onClick = export, enabled = !state.busy && state.trials.isNotEmpty(),
                                modifier = Modifier.fillMaxWidth()) { Text("Ekspor bukti JSON tanpa email") }
                        } }
                        if (state.trials.isEmpty()) item { PanelCard {
                            Text("Belum ada percobaan")
                            Text("Konfigurasikan Account A dan buka Flow dari tab Eksperimen.", color = Muted)
                        } }
                        items(state.trials.take(100), key = { it.id }) { trial -> HistoryCard(trial) }
                    }
                    2 -> {
                        item { GuidePanel(vm.launcher.profile) }
                        item { PanelCard {
                            Text("Kontrol lokal", style = MaterialTheme.typography.titleMedium)
                            OutlinedButton(onClick = { vm.refreshProviders() }, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text("Periksa ulang browser terpasang") }
                            OutlinedButton(onClick = { vm.showGuide(true); page = 0 }, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text("Tampilkan petunjuk eksperimen") }
                            OutlinedButton(onClick = { clearDialog = true }, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text("Hapus semua metadata PoC") }
                        } }
                    }
                }
            }
        }
    }
    editSlot?.let { slot -> ConfigDialog(slot, state, vm, { editSlot = null }) }
    requested?.let { slot ->
        AlertDialog(onDismissRequest = { requested = null },
            title = { Text("Buka eksperimen Account ${slot.id}?") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Akun yang diminta: ${slot.email}")
                Text(slot.method.warning)
                Text("Flow mungkin tetap memakai akun lain. Periksa email aktif sebelum membuat proyek. Pembukaan baru membatalkan pemeriksaan yang belum dicatat.", color = Amber)
            } },
            confirmButton = { TextButton(onClick = { requested = null; launch(slot) }, enabled = !state.busy) { Text("Buka untuk diuji") } },
            dismissButton = { TextButton(onClick = { requested = null }) { Text("Batal") } })
    }
    reviewing?.let { trial -> ReviewDialog(trial, state.busy,
        { observation, email -> vm.review(trial.id, observation, email) { reviewing = null } }, { reviewing = null }) }
    if (clearDialog) AlertDialog(onDismissRequest = { clearDialog = false }, title = { Text("Hapus data PoC?") },
        text = { Text("Email, konfigurasi dan riwayat lokal BYGA akan dihapus. Akun Google dan sesi browser tetap dikelola Google/browser. Ekspor bukti terlebih dahulu bila diperlukan.") },
        confirmButton = { TextButton(onClick = { clearDialog = false; vm.clear() }) { Text("Hapus metadata lokal") } },
        dismissButton = { TextButton(onClick = { clearDialog = false }) { Text("Batal") } })
}

@Composable private fun BrandHeader() {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(48.dp).background(Brush.linearGradient(listOf(Blue, Color(0xFF064F8C))), RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) {
            Text("B", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        }
        Column(Modifier.weight(1f)) {
            Text("BYGA FLOW", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("MANAGER / LAB 00", color = Cyan, style = MaterialTheme.typography.labelSmall)
        }
        Text("PoC", color = Cyan, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable private fun GateCard() {
    PanelCard(border = Color(0xFF275582)) {
        Text("PHASE 0  /  FEASIBILITY GATE", color = Cyan, style = MaterialTheme.typography.labelMedium)
        Text("Buktikan akun.\nBaru lanjutkan.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("UNVERIFIED", color = Amber, style = MaterialTheme.typography.titleSmall)
        Text("Isolasi sesi belum terbukti. APK ini adalah alat pengujian, bukan aplikasi multi-akun yang sudah lolos.", color = Muted)
    }
}

@Composable private fun PanelCard(border: Color? = null, content: @Composable ColumnScope.() -> Unit) {
    Surface(Modifier.fillMaxWidth().animateContentSize(), shape = RoundedCornerShape(22.dp), color = Panel,
        border = border?.let { BorderStroke(1.dp, it) }) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable private fun ScenarioPanel(state: PocUiState, vm: PocViewModel) {
    var selected by remember(state.settings.scenario) { mutableStateOf(state.settings.scenario) }
    PanelCard {
        Text("Seri uji perangkat", style = MaterialTheme.typography.titleMedium)
        ChoiceMenu("Skenario", selected.title, Scenario.entries, { it.title }, !state.busy) { selected = it }
        Text("Seri aktif: ${state.settings.scenario.title}\nID: ${state.settings.run.take(8)} · Profil: ${vm.launcher.profile}", color = Muted, style = MaterialTheme.typography.bodySmall)
        OutlinedButton(onClick = { vm.newRun(selected) }, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text("Mulai seri baru") }
        Text("Untuk uji restart, pertahankan ID seri yang sama sebelum dan sesudah restart.", color = Muted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable private fun AccountCard(slot: AccountSlot, trial: Trial?, busy: Boolean, edit: () -> Unit, open: () -> Unit) {
    PanelCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = RoundedCornerShape(14.dp), color = Blue.copy(alpha = 0.18f)) {
                Text(slot.id, Modifier.padding(horizontal = 18.dp, vertical = 12.dp), color = Cyan, style = MaterialTheme.typography.titleLarge)
            }
            Column(Modifier.weight(1f)) {
                Text("Account ${slot.id}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(slot.email.ifEmpty { "Email belum diatur" }, color = Muted)
            }
        }
        Text(slot.method.title, color = Cyan, style = MaterialTheme.typography.labelLarge)
        if (slot.provider.isNotEmpty() && slot.method != LaunchMethod.NATIVE) Text(slot.provider, color = Muted, style = MaterialTheme.typography.bodySmall)
        Text("Sesi saat ini: belum diketahui", color = Amber)
        trial?.let { Text("Hasil terakhir (historis): ${it.outcome.title}\n${time(it.startedAt)}", color = Muted, style = MaterialTheme.typography.bodySmall) }
        Button(onClick = open, enabled = !busy && slot.email.isNotEmpty(), modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Buka Flow · Account ${slot.id}") }
        OutlinedButton(onClick = edit, enabled = !busy, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Atur email & metode") }
    }
}

@Composable private fun HistoryCard(trial: Trial) {
    PanelCard(border = if (trial.outcome == Outcome.MISMATCH) MaterialTheme.colorScheme.error else null) {
        Text("Account ${trial.slot} · ${trial.method.title}", fontWeight = FontWeight.SemiBold)
        Text(trial.outcome.title, color = if (trial.outcome == Outcome.VERIFIED_MANUAL) Cyan else Amber)
        Text("Diminta: ${trial.expected}", color = Muted)
        trial.observed?.let { Text("Dilihat tester: $it", color = Muted) }
        Text("${trial.provider} ${trial.providerVersion}\nProfil ${trial.profile} · ${trial.scenario}\n${time(trial.startedAt)} · Seri ${trial.run.take(8)}", color = Muted, style = MaterialTheme.typography.bodySmall)
        trial.reviewedAt?.let { Text("Konfirmasi manual: ${time(it)}", color = Muted, style = MaterialTheme.typography.bodySmall) }
        trial.errorCode?.let { Text("Kode diagnostik: $it", color = Amber, style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable private fun ConfigDialog(slot: AccountSlot, state: PocUiState, vm: PocViewModel, dismiss: () -> Unit) {
    var email by rememberSaveable(slot.id) { mutableStateOf(slot.email) }
    var method by rememberSaveable(slot.id) { mutableStateOf(slot.method) }
    var provider by rememberSaveable(slot.id) { mutableStateOf(slot.provider) }
    val providers = state.providers.filter { method != LaunchMethod.CUSTOM_TAB || it.customTabs }
    val duplicate = state.slots.any { it.id != slot.id && it.email.isNotEmpty() && it.email == IdentityRules.normalize(email) }
    val valid = IdentityRules.isValid(email) && !duplicate
    AlertDialog(onDismissRequest = dismiss, title = { Text("Atur Account ${slot.id}") },
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Email hanya target pemeriksaan. Ini tidak menambahkan login Google.", color = Muted)
            OutlinedTextField(value = email, onValueChange = { email = it.take(254) }, label = { Text("Email yang diharapkan") },
                singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = email.isNotEmpty() && !valid, supportingText = { if (duplicate) Text("Email sudah digunakan slot lain.") }, modifier = Modifier.fillMaxWidth())
            ChoiceMenu("Metode", method.title, LaunchMethod.entries, { it.title }, !state.busy) { method = it }
            Text(method.warning, color = Amber)
            if (method != LaunchMethod.NATIVE) {
                ChoiceMenu("Browser", providers.find { it.packageName == provider }?.label ?: "Pilih browser", providers,
                    { "${it.label} · ${it.packageName}" }, !state.busy) { provider = it.packageName }
                if (providers.isEmpty()) Text("Tidak ada browser kompatibel ditemukan. Pasang browser, lalu periksa ulang.", color = Amber)
            }
            if (method == LaunchMethod.WORK_PROFILE && !vm.launcher.managedProfile)
                Text("BLOCKED: BYGA sedang berada di profil pribadi. Gunakan salinan profil kerja yang disediakan admin.", color = Amber)
            Text("Provider tidak tersedia akan dicatat BLOCKED saat diuji; tidak ada fallback tersembunyi.", color = Muted, style = MaterialTheme.typography.bodySmall)
        } },
        confirmButton = { TextButton(onClick = { vm.save(slot.copy(email = email, method = method, provider = provider), dismiss) }, enabled = valid && !state.busy) { Text("Simpan konfigurasi") } },
        dismissButton = { TextButton(onClick = dismiss) { Text("Batal") } })
}

@Composable private fun ReviewDialog(trial: Trial, busy: Boolean, confirm: (Observation, String) -> Unit, dismiss: () -> Unit) {
    var observation by rememberSaveable(trial.id) { mutableStateOf(Observation.IDENTITY) }
    var observed by rememberSaveable(trial.id) { mutableStateOf("") }
    val valid = observation != Observation.IDENTITY || IdentityRules.isValid(observed)
    AlertDialog(onDismissRequest = dismiss, title = { Text("Hasil pemeriksaan Account ${trial.slot}") },
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Diminta: ${trial.expected}\n${trial.method.title} · ${time(trial.startedAt)}")
            Text("Periksa menu akun aktif di Flow sebelum mengubahnya. Masukkan email yang benar-benar terlihat, meskipun berbeda dari target.", color = Muted)
            ChoiceMenu("Pengamatan tester", observation.title, Observation.entries, { it.title }, !busy) { observation = it }
            if (observation == Observation.IDENTITY) OutlinedTextField(value = observed, onValueChange = { observed = it.take(254) },
                label = { Text("Email yang terlihat di Flow") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())
            if (observation == Observation.EXPIRED) Text("Pilih hanya bila Flow memberikan pesan sesi berakhir yang jelas. Permintaan login biasa dicatat 'Perlu login'.", color = Amber)
            Text("Konfirmasi manual ini hanya untuk pembukaan tersebut. Tidak membuktikan isolasi sesi atau menjamin pembukaan berikutnya.", color = Amber)
        } },
        confirmButton = { TextButton(onClick = { confirm(observation, observed) }, enabled = valid && !busy) { Text("Simpan pengamatan tester") } },
        dismissButton = { TextButton(onClick = dismiss) { Text("Batal") } })
}

@Composable private fun <T> ChoiceMenu(label: String, value: String, options: List<T>, title: (T) -> String, enabled: Boolean, choose: (T) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Muted)
        Box {
            OutlinedButton(onClick = { expanded = true }, enabled = enabled && options.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), shape = RoundedCornerShape(12.dp)) { Text(value, Modifier.weight(1f)); Text("⌄") }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.heightIn(max = 350.dp)) {
                options.forEach { option -> DropdownMenuItem(text = { Text(title(option)) }, onClick = { choose(option); expanded = false }) }
            }
        }
    }
}

@Composable private fun GuidePanel(profile: String) {
    PanelCard {
        Text("Protokol pengujian nyata", style = MaterialTheme.typography.titleLarge)
        Text("Profil BYGA saat ini: $profile", color = Cyan)
        listOf(
            "1. Siapkan Android 12+ dan tiga akun milik sendiri yang dapat mengakses Flow. Pasang Flow resmi; uji metode native terlebih dahulu.",
            "2. Atur email A/B/C dan provider per slot. Lakukan login awal secara manual di aplikasi/browser resmi. BYGA tidak meminta password atau OTP.",
            "3. Mulai seri A → B → C, kemudian C → A → B. Pada setiap pembukaan, periksa email aktif SEBELUM memperbaiki akun secara manual dan catat hasil yang sebenarnya.",
            "4. Ulangi paling sedikit 20 perpindahan berturut-turut (disarankan 20 siklus A/B/C). Nol salah akun diperlukan. Berganti akun manual setiap kali tidak membuktikan isolasi.",
            "5. Paksa berhenti BYGA, buka kembali, lalu restart Android. Pertahankan ID seri dan periksa identitas lagi; riwayat lama bukan bukti sesi sekarang.",
            "6. Uji kedaluwarsa melalui mekanisme resmi. Catat permintaan login atau pesan sesi berakhir sesuai yang terlihat. Uji mode pesawat; kegagalan jaringan diamati tester di Flow.",
            "7. Untuk profil kerja, gunakan salinan BYGA dan browser/Flow di profil kerja yang sudah dikelola admin. Ekspor bukti dari kedua profil; jangan mengasumsikan tiga atau puluhan profil tersedia.",
            "8. Ekspor JSON dari tab Riwayat. Lampirkan video/screenshot tersamarkan, versi Flow/browser, langkah login awal, hasil restart dan intervensi manual ke docs/poc-results.md. Gate tetap UNVERIFIED sampai bukti ditinjau."
        ).forEach { Text(it, color = Muted) }
        Text("Dilarang: WebView login, akses cookie/token, root, ketergantungan ADB/Shizuku, otomasi Accessibility, dan Intent autentikasi privat. Tidak ada jaminan generasi video berjalan di background.", color = Amber)
    }
}
