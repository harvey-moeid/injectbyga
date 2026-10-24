package com.byga.flowmanager.organizer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.byga.flowmanager.domain.IdentityRules
import com.byga.flowmanager.domain.Observation
import com.byga.flowmanager.organizer.domain.*
import com.byga.flowmanager.ui.*

@Composable internal fun AccountEditor(account: ManagedAccount, state: DashboardState, vm: DashboardViewModel, close: () -> Unit) {
    var email by rememberSaveable(account.id) { mutableStateOf(account.email) }
    var alias by rememberSaveable(account.id) { mutableStateOf(account.alias) }
    var note by rememberSaveable(account.id) { mutableStateOf(account.note) }
    var group by rememberSaveable(account.id) { mutableStateOf(account.groupId) }
    var favorite by rememberSaveable(account.id) { mutableStateOf(account.favorite) }
    var color by rememberSaveable(account.id) { mutableIntStateOf(account.color) }
    val duplicate = state.accounts.any { it.id != account.id && it.email == IdentityRules.normalize(email) }
    val valid = IdentityRules.isValid(email) && alias.trim().length in 1..80 && alias.none { it.isISOControl() } && !duplicate
    AlertDialog(onDismissRequest = { if (!state.busy) close() },
        title = { Text(if (account.id.isEmpty()) "Tambah akun" else "Edit metadata akun") },
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Hanya metadata BYGA. Login dan pemilihan akun dilakukan di Flow.", color = Muted)
            OutlinedTextField(email, { email = it.take(254) }, label = { Text("Email Google") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), isError = duplicate || (email.isNotEmpty() && !IdentityRules.isValid(email)),
                supportingText = { if (duplicate) Text("Email ini sudah ada.") }, modifier = Modifier.fillMaxWidth().testTag("account-email"))
            OutlinedTextField(alias, { alias = it.take(80) }, label = { Text("Alias") }, singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("account-alias"))
            Picker("Grup", state.groups.find { it.id == group }?.name ?: "Tanpa grup", listOf<AccountGroup?>(null) + state.groups,
                { it?.name ?: "Tanpa grup" }, !state.busy) { group = it?.id }
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Favorit", Modifier.padding(top = 12.dp)); Switch(favorite, { favorite = it })
            }
            Picker("Warna avatar", "Warna ${color + 1}", (0..5).toList(), { "Warna ${it + 1}" }, !state.busy) { color = it }
            OutlinedTextField(note, { note = it.take(1000) }, label = { Text("Catatan proyek (opsional)") },
                supportingText = { Text("Jangan isi password, OTP atau token.") }, maxLines = 4, modifier = Modifier.fillMaxWidth())
            if (account.id.isNotEmpty()) Text("Terakhir dibuka: ${date(account.lastOpenedAt)}\nSesi aktif Flow: tidak diketahui BYGA", color = Muted, style = MaterialTheme.typography.bodySmall)
        } },
        confirmButton = { TextButton(onClick = { vm.save(account.copy(email = email, alias = alias, note = note,
            groupId = group, favorite = favorite, color = color), close) }, enabled = valid && !state.busy) { Text("Simpan akun") } },
        dismissButton = { TextButton(onClick = close, enabled = !state.busy) { Text("Batal") } })
}

@Composable internal fun GroupEditor(group: AccountGroup?, busy: Boolean, save: (String) -> Unit, close: () -> Unit) {
    var name by rememberSaveable(group?.id) { mutableStateOf(group?.name.orEmpty()) }
    AlertDialog(onDismissRequest = close, title = { Text(if (group == null) "Grup baru" else "Ubah nama grup") },
        text = { OutlinedTextField(name, { name = it.take(60) }, label = { Text("Nama grup") }, singleLine = true, modifier = Modifier.testTag("group-name")) },
        confirmButton = { TextButton(onClick = { save(name) }, enabled = !busy && OrganizerRules.validGroup(name)) { Text("Simpan grup") } },
        dismissButton = { TextButton(onClick = close) { Text("Batal") } })
}

@Composable internal fun ImportDialog(preview: ImportPreview, busy: Boolean, commit: () -> Unit, close: () -> Unit) {
    AlertDialog(onDismissRequest = { if (!busy) close() }, title = { Text("Preview impor metadata") },
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("${preview.total} baris · ${preview.valid.size} siap ditambahkan", color = Cyan)
            Text("${preview.duplicates.size} duplikat dilewati · ${preview.rejected.size} baris ditolak", color = Amber)
            Text("Impor tidak melakukan login. Akun yang sudah ada tidak ditimpa. Grup baru akan dibuat bila diperlukan.", color = Muted)
            preview.valid.take(30).forEach { item -> Text("${item.alias}\n${item.email}${if (item.group.isBlank()) "" else " · ${item.group}"}", style = MaterialTheme.typography.bodySmall) }
            if (preview.valid.size > 30) Text("…dan ${preview.valid.size - 30} akun lainnya.", color = Muted)
            (preview.rejected + preview.duplicates).sortedBy { it.line }.take(40).forEach { Text("Baris ${it.line}: ${it.reason}", color = Amber, style = MaterialTheme.typography.bodySmall) }
            if (preview.rejected.size + preview.duplicates.size > 40) Text("Rincian ditampilkan untuk 40 masalah pertama.", color = Muted)
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        } },
        confirmButton = { TextButton(onClick = commit, enabled = !busy && preview.valid.isNotEmpty()) { Text("Impor ${preview.valid.size} akun") } },
        dismissButton = { TextButton(onClick = close, enabled = !busy) { Text("Batal") } })
}

@Composable internal fun ObservationDialog(entry: LaunchEntry, busy: Boolean, confirm: (Observation, String) -> Unit, close: () -> Unit) {
    var observation by rememberSaveable(entry.id) { mutableStateOf(Observation.IDENTITY) }
    var email by rememberSaveable(entry.id) { mutableStateOf("") }
    AlertDialog(onDismissRequest = close, title = { Text("Konfirmasi akun yang terlihat") },
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Target: ${entry.alias}\n${entry.expected}")
            Text("Pilih akun secara manual di Flow, periksa email aktif, lalu catat apa yang benar-benar terlihat. Jika berbeda, masukkan email yang berbeda tersebut.", color = Muted)
            Picker("Hasil pengamatan", observation.title, Observation.entries, { it.title }, !busy) { observation = it }
            if (observation == Observation.IDENTITY) OutlinedTextField(email, { email = it.take(254) }, label = { Text("Email aktual di Flow") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth().testTag("observed-email"))
            if (observation == Observation.EXPIRED) Text("Gunakan hanya jika ada pesan sesi berakhir yang jelas. Permintaan login biasa bukan bukti expiry.", color = Amber)
            Text("Konfirmasi ini historis dan manual. BYGA tidak membaca akun aktif atau menjamin sesi pembukaan berikutnya.", color = Amber)
        } },
        confirmButton = { TextButton(onClick = { confirm(observation, email) }, enabled = !busy &&
            (observation != Observation.IDENTITY || IdentityRules.isValid(email))) { Text("Simpan konfirmasi") } },
        dismissButton = { TextButton(onClick = close, enabled = !busy) { Text("Batal") } })
}
