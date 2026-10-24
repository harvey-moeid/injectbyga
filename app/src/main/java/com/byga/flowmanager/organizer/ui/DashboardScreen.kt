package com.byga.flowmanager.organizer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.byga.flowmanager.BuildConfig
import com.byga.flowmanager.domain.Outcome
import com.byga.flowmanager.organizer.domain.*
import com.byga.flowmanager.organizer.domain.DisplayMode
import com.byga.flowmanager.ui.*

@Composable
fun DashboardScreen(
    state: DashboardState, vm: DashboardViewModel, openFlow: (ManagedAccount) -> Unit,
    pickAccount: () -> Unit, importFile: () -> Unit, exportFile: () -> Unit, enableLock: () -> Unit, openLab: () -> Unit
) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    var addOptions by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<ManagedAccount?>(null) }
    var target by remember { mutableStateOf<ManagedAccount?>(null) }
    var observation by remember { mutableStateOf<LaunchEntry?>(null) }
    var deleteIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    var moveDialog by remember { mutableStateOf(false) }
    var groupDialog by remember { mutableStateOf(false) }
    var editingGroup by remember { mutableStateOf<AccountGroup?>(null) }
    var deletingGroup by remember { mutableStateOf<AccountGroup?>(null) }
    var exportDialog by remember { mutableStateOf(false) }
    val snack = remember { SnackbarHostState() }
    val visible = state.visible
    val width = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.width.toDp() }
    val columns = if (width >= 650.dp) 3 else 2
    LaunchedEffect(state.message) { state.message?.let { snack.showSnackbar(it, duration = SnackbarDuration.Long); vm.dismissMessage() } }
    LaunchedEffect(state.pickedEmail) { state.pickedEmail?.let { email ->
        editing = ManagedAccount("", email, OrganizerRules.defaultAlias(email)); vm.consumePickedEmail()
    } }
    LaunchedEffect(state.accounts) { selected = selected.intersect(state.accounts.map { it.id }.toSet()) }

    Scaffold(containerColor = Navy, snackbarHost = { SnackbarHost(snack) }, bottomBar = {
        NavigationBar(containerColor = Navy) {
            val icons = listOf(Icons.Default.Dashboard, Icons.Default.Folder, Icons.Default.History, Icons.Default.Settings)
            listOf("Akun", "Grup", "Riwayat", "Pengaturan").forEachIndexed { i, label ->
                NavigationBarItem(selected = page == i, onClick = { page = i }, icon = { Icon(icons[i], label) }, label = { Text(label) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Panel, selectedIconColor = Cyan, selectedTextColor = Cyan))
            }
        }
    }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            LazyColumn(Modifier.widthIn(max = 960.dp).fillMaxWidth().testTag("dashboard-list"), contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item { Brand(state.accounts.size) }
                if (state.busy) item { LinearProgressIndicator(Modifier.fillMaxWidth(), color = Cyan) }
                when (page) {
                    0 -> {
                        item { OrganizerPanel(border = Color(0xFF254F7B)) {
                            Text("SATU BROWSER · AKUN DIPILIH DI FLOW", color = Cyan, style = MaterialTheme.typography.labelSmall)
                            Text("Ruang kerja Anda,\nlebih tertata.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text("Pilih akun di BYGA, lalu pilih akun aktif secara manual di Flow. BYGA tidak membaca sesi Google.", color = Muted)
                            FilledTonalButton(onClick = { addOptions = true }, enabled = !state.busy,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("add-account")) {
                                Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Tambah akun")
                            }
                        } }
                        val pending = state.launches.firstOrNull()?.takeIf { it.outcome == Outcome.UNVERIFIED }
                        if (pending != null) item { OrganizerPanel(border = Amber) {
                            Text("PERIKSA AKUN DI FLOW", color = Amber, style = MaterialTheme.typography.labelMedium)
                            Text("${pending.alias}\n${pending.expected}", fontWeight = FontWeight.SemiBold)
                            Text("Setelah memilih akun yang benar di Flow, catat email aktual. Membuka tautan saja belum membuktikan identitas.", color = Muted)
                            Button(onClick = { observation = pending }, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text("Catat akun yang terlihat") }
                        } }
                        item { OutlinedTextField(state.query, vm::query, singleLine = true, label = { Text("Cari alias atau email") },
                            leadingIcon = { Icon(Icons.Default.Search, null) }, trailingIcon = {
                                if (state.query.isNotEmpty()) IconButton(onClick = { vm.query("") }) { Icon(Icons.Default.Close, "Hapus pencarian") }
                            }, modifier = Modifier.fillMaxWidth().testTag("account-search"), shape = RoundedCornerShape(16.dp)) }
                        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = !state.favoritesOnly, onClick = { vm.favorites(false) }, label = { Text("Semua ${state.accounts.size}") })
                            FilterChip(selected = state.favoritesOnly, onClick = { vm.favorites(!state.favoritesOnly) }, label = { Text("Favorit") },
                                leadingIcon = { Icon(Icons.Default.Star, null, Modifier.size(16.dp)) })
                            FilterChip(selected = state.settings.sort == SortMode.RECENT, onClick = { vm.sort(SortMode.RECENT) }, label = { Text("Terakhir") })
                        } }
                        item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Bottom) {
                            Column(Modifier.weight(1f)) { Picker("Grup", state.groups.find { it.id == state.settings.groupId }?.name ?: "Semua grup",
                                listOf<AccountGroup?>(null) + state.groups, { it?.name ?: "Semua grup" }, !state.busy) { vm.filterGroup(it?.id) } }
                            IconButton(onClick = { vm.display(if (state.settings.displayMode == DisplayMode.LIST) DisplayMode.GRID else DisplayMode.LIST) },
                                enabled = !state.busy, modifier = Modifier.size(48.dp).testTag("display-toggle")) {
                                Icon(if (state.settings.displayMode == DisplayMode.LIST) Icons.Default.GridView else Icons.AutoMirrored.Filled.ViewList,
                                    if (state.settings.displayMode == DisplayMode.LIST) "Tampilan grid" else "Tampilan daftar")
                            }
                        } }
                        item { Picker("Urutkan", state.settings.sort.title, SortMode.entries, { it.title }, !state.busy, vm::sort) }
                        if (selected.isNotEmpty()) item { OrganizerPanel(border = Cyan) {
                            Text("${selected.size} akun dipilih", color = Cyan)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { moveDialog = true }, enabled = !state.busy) { Text("Pindah grup") }
                                OutlinedButton(onClick = { deleteIds = selected }, enabled = !state.busy) { Text("Hapus") }
                            }
                            TextButton(onClick = { selected = emptySet() }) { Text("Batalkan pilihan") }
                        } }
                        item { Text("${visible.size} akun ditampilkan · ${state.settings.displayMode.title}", color = Muted, style = MaterialTheme.typography.bodySmall) }
                        if (visible.isEmpty()) item { OrganizerPanel {
                            Text(if (state.accounts.isEmpty()) "Belum ada akun" else "Tidak ada akun yang cocok", style = MaterialTheme.typography.titleLarge)
                            Text(if (state.accounts.isEmpty()) "Tambah manual, pilih dari perangkat, atau impor daftar CSV/TXT." else "Ubah pencarian, favorit atau filter grup.", color = Muted)
                            if (state.accounts.isNotEmpty()) TextButton(onClick = { vm.query(""); vm.favorites(false); vm.filterGroup(null) }) { Text("Reset filter") }
                        } }
                        if (state.settings.displayMode == DisplayMode.LIST) {
                            items(visible, key = { it.id }) { account ->
                                ManagedCard(account, state, selected, { target = account }, { editing = account }, { vm.favorite(account) },
                                    { selected = if (account.id in selected) selected - account.id else selected + account.id }, { deleteIds = setOf(account.id) })
                            }
                        } else {
                            items(visible.chunked(columns), key = { it.first().id }) { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                    row.forEach { account -> Column(Modifier.weight(1f)) {
                                        ManagedCard(account, state, selected, { target = account }, { editing = account }, { vm.favorite(account) },
                                            { selected = if (account.id in selected) selected - account.id else selected + account.id }, { deleteIds = setOf(account.id) }, compact = true)
                                    } }
                                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                                }
                            }
                        }
                    }
                    1 -> {
                        item { OrganizerPanel {
                            Text("Grup akun", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text("Rapikan proyek dan tim Anda. Menghapus grup tidak menghapus akun.", color = Muted)
                            Button(onClick = { editingGroup = null; groupDialog = true }, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text("Buat grup") }
                        } }
                        if (state.groups.isEmpty()) item { Text("Belum ada grup. Buat grup pertama untuk menata akun.", color = Muted) }
                        items(state.groups.sortedBy { it.name.lowercase() }, key = { it.id }) { group -> OrganizerPanel {
                            Text(group.name, style = MaterialTheme.typography.titleLarge)
                            Text("${state.accounts.count { it.groupId == group.id }} akun", color = Muted)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { vm.filterGroup(group.id); page = 0 }) { Text("Lihat akun") }
                                IconButton(onClick = { editingGroup = group; groupDialog = true }, enabled = !state.busy) { Icon(Icons.Default.Edit, "Ubah grup ${group.name}") }
                                IconButton(onClick = { deletingGroup = group }, enabled = !state.busy) { Icon(Icons.Default.Delete, "Hapus grup ${group.name}") }
                            }
                        } }
                    }
                    2 -> {
                        item { OrganizerPanel {
                            Text("Riwayat pembukaan", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text("${state.launches.size} catatan lokal. Tampilan 100 terbaru. Hasil konfirmasi bersifat historis, bukan status sesi sekarang.", color = Muted)
                        } }
                        if (state.launches.isEmpty()) item { Text("Belum ada pembukaan Flow dari dashboard.", color = Muted) }
                        items(state.launches.take(100), key = { it.id }) { entry -> OrganizerPanel(border = if (entry.outcome == Outcome.MISMATCH) Amber else null) {
                            Text(entry.alias, style = MaterialTheme.typography.titleLarge)
                            Text("Target: ${entry.expected}", color = Muted)
                            Text(entry.outcome.title, color = if (entry.outcome == Outcome.VERIFIED_MANUAL) Cyan else Amber)
                            entry.observed?.let { Text("Dilihat pengguna: $it", color = Muted) }
                            Text("${date(entry.startedAt)}\n${entry.provider} ${entry.providerVersion}", color = Muted, style = MaterialTheme.typography.bodySmall)
                            entry.reviewedAt?.let { Text("Konfirmasi manual: ${date(it)}", color = Muted, style = MaterialTheme.typography.bodySmall) }
                            entry.errorCode?.let { Text("Kode: $it", color = Amber, style = MaterialTheme.typography.bodySmall) }
                            if (state.launches.firstOrNull()?.id == entry.id && entry.outcome == Outcome.UNVERIFIED)
                                OutlinedButton(onClick = { observation = entry }, enabled = !state.busy) { Text("Catat akun yang terlihat") }
                        } }
                    }
                    3 -> {
                        item { OrganizerPanel {
                            Text("Satu browser untuk semua akun", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text("BYGA membuka Flow di browser ini. Pilih akun aktif secara manual di Flow; sesi Google tidak disalin ke BYGA.", color = Muted)
                            Picker("Browser pilihan", state.providers.find { it.packageName == state.settings.browser }?.label ?: "Pilih browser",
                                state.providers, { "${it.label} · ${it.packageName}" }, !state.busy) { vm.browser(it.packageName) }
                            if (state.providers.isEmpty()) Text("Browser belum terdeteksi. Pasang browser resmi lalu periksa ulang.", color = Amber)
                            if (state.settings.browser.isNotEmpty() && state.providers.none { it.packageName == state.settings.browser })
                                Text("Browser sebelumnya tidak tersedia. Pilih kembali; BYGA tidak mengalihkan secara otomatis.", color = Amber)
                            OutlinedButton(onClick = { vm.refreshBrowsers() }, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text("Periksa ulang browser") }
                        } }
                        item { OrganizerPanel {
                            Text("Tambah & pindahkan metadata", style = MaterialTheme.typography.titleLarge)
                            OutlinedButton(onClick = pickAccount, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text("Pilih akun Google dari perangkat") }
                            OutlinedButton(onClick = importFile, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text("Impor CSV / TXT") }
                            OutlinedButton(onClick = { vm.previewPocAccounts() }, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text("Preview salin akun PoC lama") }
                            OutlinedButton(onClick = { exportDialog = true }, enabled = !state.busy && state.accounts.isNotEmpty(), modifier = Modifier.fillMaxWidth()) { Text("Ekspor daftar CSV") }
                            Text("CSV: email,alias,group. TXT: satu email per baris. Maksimal 2 MB / 5.000 akun per impor. Impor hanya metadata, tanpa password atau OTP.", color = Muted, style = MaterialTheme.typography.bodySmall)
                        } }
                        item { OrganizerPanel {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) { Text("Kunci dashboard", style = MaterialTheme.typography.titleMedium); Text("PIN perangkat / biometrik", color = Muted) }
                                Switch(state.settings.lockEnabled, { if (it) enableLock() else vm.setLock(false) }, enabled = !state.busy)
                            }
                            Text("Jika aktif, BYGA mengunci metadata saat aplikasi ditinggalkan. Kunci ini tidak mengubah login Google.", color = Muted)
                        } }
                        item { OrganizerPanel {
                            Text("Tentang versi ini", style = MaterialTheme.typography.titleLarge)
                            Text("BYGA Flow Manager ${BuildConfig.VERSION_NAME}", color = Cyan)
                            Text("Organizer lokal dengan pergantian akun manual di Flow. Tidak ada klaim sesi independen, verifikasi otomatis, pemulihan proyek, atau progres video background.", color = Muted)
                            OutlinedButton(onClick = openLab, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text("Buka lab PoC terdahulu") }
                        } }
                    }
                }
                item { Spacer(Modifier.height(12.dp)) }
            }
        }
    }

    if (addOptions) AlertDialog(onDismissRequest = { addOptions = false }, title = { Text("Tambahkan metadata akun") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Menambah email tidak melakukan login ke Google atau memilih akun aktif Flow.", color = Muted)
            Button(onClick = { addOptions = false; editing = ManagedAccount("", "", "") }, modifier = Modifier.fillMaxWidth()) { Text("Tambah manual") }
            OutlinedButton(onClick = { addOptions = false; pickAccount() }, modifier = Modifier.fillMaxWidth()) { Text("Pilih dari perangkat") }
            OutlinedButton(onClick = { addOptions = false; importFile() }, modifier = Modifier.fillMaxWidth()) { Text("Impor CSV / TXT") }
        } }, confirmButton = { TextButton(onClick = { addOptions = false }) { Text("Tutup") } })
    editing?.let { AccountEditor(it, state, vm) { editing = null } }
    target?.let { account ->
        val provider = state.providers.find { it.packageName == state.settings.browser }
        AlertDialog(onDismissRequest = { target = null }, title = { Text("Pilih akun ini di Flow") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${account.alias}\n${account.email}", fontWeight = FontWeight.SemiBold)
                Text("Browser: ${provider?.label ?: "belum dipilih / tidak tersedia"}", color = Cyan)
                Text("Setelah Flow terbuka, pilih akun di atas melalui menu akun Flow dan periksa email aktif sebelum membuat proyek. BYGA tidak mengganti akun Google secara otomatis.", color = Amber)
                Text("Pembukaan baru menggantikan konfirmasi yang belum dicatat. Anda dapat kembali ke BYGA untuk mencatat identitas aktual.", color = Muted)
            } },
            confirmButton = { if (provider != null) TextButton(onClick = { target = null; openFlow(account) }, enabled = !state.busy) { Text("Buka Flow, lalu pilih akun") }
                else TextButton(onClick = { target = null; page = 3 }) { Text("Atur browser") } },
            dismissButton = { TextButton(onClick = { target = null }) { Text("Batal") } })
    }
    observation?.let { entry -> ObservationDialog(entry, state.busy, { choice, email -> vm.review(entry.id, choice, email) { observation = null } }) { observation = null } }
    state.preview?.let { ImportDialog(it, state.busy, vm::commitImport, vm::cancelImport) }
    if (groupDialog) GroupEditor(editingGroup, state.busy, { name -> vm.saveGroup(editingGroup?.id, name) { groupDialog = false } }) { groupDialog = false }
    deletingGroup?.let { group -> AlertDialog(onDismissRequest = { deletingGroup = null }, title = { Text("Hapus grup ${group.name}?") },
        text = { Text("Akun tetap tersimpan dan akan menjadi tanpa grup. Ini hanya menghapus grup lokal BYGA.") },
        confirmButton = { TextButton(onClick = { deletingGroup = null; vm.deleteGroup(group.id) }, enabled = !state.busy) { Text("Hapus grup") } },
        dismissButton = { TextButton(onClick = { deletingGroup = null }) { Text("Batal") } }) }
    if (deleteIds.isNotEmpty()) AlertDialog(onDismissRequest = { deleteIds = emptySet() }, title = { Text("Hapus ${deleteIds.size} metadata akun?") },
        text = { Text("Email, alias, catatan dan riwayat terkait di BYGA akan dihapus. Akun Google, sesi browser, data PoC lama dan file ekspor tidak ikut dihapus.") },
        confirmButton = { TextButton(onClick = { vm.delete(deleteIds) { deleteIds = emptySet(); selected = emptySet() } }, enabled = !state.busy) { Text("Hapus metadata lokal") } },
        dismissButton = { TextButton(onClick = { deleteIds = emptySet() }, enabled = !state.busy) { Text("Batal") } })
    if (moveDialog) AlertDialog(onDismissRequest = { moveDialog = false }, title = { Text("Pindahkan ${selected.size} akun") },
        text = { Column(Modifier.verticalScroll(rememberScrollState())) { (listOf<AccountGroup?>(null) + state.groups).forEach { group ->
            TextButton(onClick = { vm.move(selected, group?.id) { moveDialog = false; selected = emptySet() } }, enabled = !state.busy) { Text(group?.name ?: "Tanpa grup") }
        } } }, confirmButton = { TextButton(onClick = { moveDialog = false }) { Text("Batal") } })
    if (exportDialog) AlertDialog(onDismissRequest = { exportDialog = false }, title = { Text("Ekspor daftar akun?") },
        text = { Text("CSV berisi email, alias dan nama grup dalam teks biasa. Favorit, warna, catatan dan riwayat tidak disertakan. Pilih lokasi pribadi; file ini bukan ekspor sesi/login Google.") },
        confirmButton = { TextButton(onClick = { exportDialog = false; exportFile() }) { Text("Pilih lokasi ekspor") } },
        dismissButton = { TextButton(onClick = { exportDialog = false }) { Text("Batal") } })
}

@Composable private fun Brand(count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(48.dp).background(Brush.linearGradient(listOf(Blue, Color(0xFF135A79))), RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) {
            Text("B", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        }
        Column(Modifier.weight(1f)) { Text("BYGA FLOW", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text("MANAGER", color = Cyan, style = MaterialTheme.typography.labelSmall) }
        Text("$count akun", color = Muted, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable private fun ManagedCard(account: ManagedAccount, state: DashboardState, selected: Set<String>, open: () -> Unit,
    edit: () -> Unit, favorite: () -> Unit, select: () -> Unit, delete: () -> Unit, compact: Boolean = false) {
    var menu by remember { mutableStateOf(false) }
    val latest = state.launches.firstOrNull { it.accountId == account.id && it.expected == account.email }
    val color = AvatarColors[account.color.coerceIn(0, 5)]
    OrganizerPanel(border = if (account.id in selected) Cyan else null, modifier = Modifier.testTag("account-${account.id}")) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (selected.isNotEmpty()) Checkbox(account.id in selected, { select() }, enabled = !state.busy)
            Box(Modifier.size(40.dp).background(color.copy(alpha = 0.14f), CircleShape), contentAlignment = Alignment.Center) {
                Text(account.alias.take(1).uppercase(), color = color, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.weight(1f))
            if (!compact) IconButton(onClick = favorite, enabled = !state.busy) { Icon(if (account.favorite) Icons.Default.Star else Icons.Default.StarBorder,
                "Favorit ${account.alias}", tint = if (account.favorite) Amber else Muted) }
            Box {
                IconButton(onClick = { menu = true }, enabled = !state.busy) { Icon(Icons.Default.MoreVert, "Menu ${account.alias}") }
                DropdownMenu(menu, { menu = false }) {
                    DropdownMenuItem(text = { Text(if (account.favorite) "Hapus dari favorit" else "Jadikan favorit") }, onClick = { menu = false; favorite() })
                    DropdownMenuItem(text = { Text("Edit metadata") }, onClick = { menu = false; edit() })
                    DropdownMenuItem(text = { Text("Pilih untuk aksi massal") }, onClick = { menu = false; select() })
                    DropdownMenuItem(text = { Text("Hapus metadata") }, onClick = { menu = false; delete() })
                }
            }
        }
        Text(account.alias, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        if (compact && account.favorite) Text("★ Favorit", color = Amber, style = MaterialTheme.typography.labelSmall)
        Text(account.email, color = Muted, style = MaterialTheme.typography.bodySmall)
        Text(state.groups.find { it.id == account.groupId }?.name ?: "Tanpa grup", color = color, style = MaterialTheme.typography.labelMedium)
        Text("Terakhir dibuka: ${date(account.lastOpenedAt)}", color = Muted, style = MaterialTheme.typography.bodySmall)
        Text("Akun aktif Flow perlu diperiksa", color = Amber, style = MaterialTheme.typography.bodySmall)
        latest?.let { Text("Hasil historis: ${it.outcome.title}", color = Muted, style = MaterialTheme.typography.bodySmall) }
        Button(onClick = open, enabled = !state.busy, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("open-${account.id}")) { Text("Buka Flow") }
    }
}
