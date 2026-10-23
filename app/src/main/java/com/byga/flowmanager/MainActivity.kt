package com.byga.flowmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.byga.flowmanager.ui.BygaTheme
import com.byga.flowmanager.ui.PocScreen
import com.byga.flowmanager.ui.PocViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BygaTheme {
                val vm: PocViewModel = viewModel()
                val state by vm.state.collectAsStateWithLifecycle()
                val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
                    uri?.let(vm::export)
                }
                PocScreen(state, vm, { slot -> vm.launch(this, slot) }, { export.launch("byga-poc-evidence.json") })
            }
        }
    }
}
