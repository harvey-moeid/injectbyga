package com.byga.flowmanager.organizer.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.byga.flowmanager.ui.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateFormat = DateTimeFormatter.ofPattern("dd MMM · HH:mm").withZone(ZoneId.systemDefault())
internal fun date(value: Long?): String = value?.let { dateFormat.format(Instant.ofEpochMilli(it)) } ?: "Belum pernah"
internal val AvatarColors = listOf(Cyan, Color(0xFF8CAEFF), Color(0xFFCE9FFF), Color(0xFFFFB780), Color(0xFF7EE1B3), Color(0xFFFFA4D0))

@Composable internal fun OrganizerPanel(modifier: Modifier = Modifier, border: Color? = null, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier.fillMaxWidth().animateContentSize(), color = Panel, shape = RoundedCornerShape(22.dp),
        border = border?.let { BorderStroke(1.dp, it) }) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable internal fun <T> Picker(label: String, value: String, items: List<T>, title: (T) -> String,
    enabled: Boolean = true, select: (T) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Muted)
        Box {
            OutlinedButton(onClick = { open = true }, enabled = enabled && items.isNotEmpty(),
                shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text(value, Modifier.weight(1f)); Text("⌄")
            }
            DropdownMenu(expanded = open, onDismissRequest = { open = false }, modifier = Modifier.heightIn(max = 360.dp)) {
                items.forEach { item -> DropdownMenuItem(text = { Text(title(item)) }, onClick = { select(item); open = false }) }
            }
        }
    }
}

@Composable fun OrganizerLockShell(state: DashboardState, unlock: () -> Unit, dismissMessage: () -> Unit, retry: () -> Unit, content: @Composable () -> Unit) {
    if (!state.loading && !state.failed && (!state.settings.lockEnabled || state.unlocked)) {
        content()
    } else {
        val snack = remember { SnackbarHostState() }
        LaunchedEffect(state.message) { state.message?.let { snack.showSnackbar(it); dismissMessage() } }
        Scaffold(containerColor = Navy, snackbarHost = { SnackbarHost(snack) }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                Text("BYGA FLOW", style = MaterialTheme.typography.headlineMedium, color = Cyan)
                Spacer(Modifier.height(20.dp))
                if (state.loading) {
                    CircularProgressIndicator(color = Cyan)
                    Text("Memuat metadata lokal…", Modifier.padding(top = 16.dp), color = Muted)
                } else if (state.failed) {
                    Text("Metadata belum dapat dibaca", style = MaterialTheme.typography.titleLarge)
                    Text("Tutup dan buka kembali aplikasi. Jika kunci lokal hilang, reset data melalui Pengaturan Android akan menghapus metadata BYGA.", Modifier.padding(vertical = 16.dp), color = Muted)
                    OutlinedButton(onClick = retry) { Text("Muat ulang metadata") }
                } else {
                    Text("Dashboard terkunci", style = MaterialTheme.typography.titleLarge)
                    Text("Gunakan kunci perangkat atau biometrik untuk melanjutkan.", Modifier.padding(vertical = 16.dp), color = Muted)
                    Button(onClick = unlock, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Buka dengan kunci perangkat") }
                }
            }
        }
    }
}
