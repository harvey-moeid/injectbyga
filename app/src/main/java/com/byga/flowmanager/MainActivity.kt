package com.byga.flowmanager

import android.accounts.AccountManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byga.flowmanager.organizer.ui.DashboardScreen
import com.byga.flowmanager.organizer.ui.OrganizerLockShell
import com.byga.flowmanager.ui.BygaTheme

class MainActivity : SecureBygaActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BygaTheme {
                val state by dashboardVm.state.collectAsStateWithLifecycle()
                val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let(dashboardVm::importDocument) }
                val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri -> uri?.let(dashboardVm::exportCsv) }
                val picker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == RESULT_OK) dashboardVm.pickedEmail(result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME))
                }
                OrganizerLockShell(state, { authenticate() }, dashboardVm::dismissMessage, dashboardVm::reload) {
                    DashboardScreen(state, dashboardVm,
                        openFlow = { dashboardVm.launch(this, it) },
                        pickAccount = {
                            try {
                                picker.launch(AccountManager.newChooseAccountIntent(null, null, arrayOf("com.google"), null, null, null, null))
                            } catch (_: ActivityNotFoundException) { dashboardVm.notify("Pemilih akun Android tidak tersedia. Gunakan tambah manual atau impor metadata.") }
                            catch (_: SecurityException) { dashboardVm.notify("Perangkat membatasi pemilih akun. Gunakan tambah manual atau impor metadata.") }
                        },
                        importFile = { filePicker.launch(arrayOf("text/*", "application/csv", "application/vnd.ms-excel")) },
                        exportFile = { export.launch("byga-daftar-akun.csv") },
                        enableLock = { authenticate(true) },
                        openLab = { startActivity(Intent(this, PocActivity::class.java)) })
                }
            }
        }
    }
}
