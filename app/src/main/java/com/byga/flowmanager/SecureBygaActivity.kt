package com.byga.flowmanager

import android.os.Bundle
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.byga.flowmanager.organizer.ui.DashboardViewModel

abstract class SecureBygaActivity : FragmentActivity() {
    protected lateinit var dashboardVm: DashboardViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dashboardVm = ViewModelProvider(this)[DashboardViewModel::class.java]
    }
    protected fun authenticate(enableLock: Boolean = false) {
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        if (BiometricManager.from(this).canAuthenticate(authenticators) != BiometricManager.BIOMETRIC_SUCCESS) {
            dashboardVm.notify("Kunci perangkat/biometrik belum tersedia. Atur PIN, pola atau biometrik pada Pengaturan Android.")
            return
        }
        BiometricPrompt(this, ContextCompat.getMainExecutor(this), object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                if (enableLock) dashboardVm.setLock(true) else dashboardVm.unlock()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                dashboardVm.notify("Autentikasi dibatalkan atau tidak tersedia. Metadata tetap terkunci bila kunci diaktifkan.")
            }
        }).authenticate(BiometricPrompt.PromptInfo.Builder().setTitle("Buka BYGA Flow Manager")
            .setSubtitle("Gunakan kunci perangkat atau biometrik untuk mengakses metadata lokal")
            .setAllowedAuthenticators(authenticators).build())
    }
    override fun onStop() {
        if (!isChangingConfigurations) dashboardVm.lockNow()
        super.onStop()
    }
}
