package com.byga.flowmanager.launcher

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.UserManager
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import com.byga.flowmanager.domain.LaunchMethod
import com.byga.flowmanager.domain.Outcome

data class Provider(val packageName: String, val label: String, val version: String, val customTabs: Boolean)
data class Capability(val available: Boolean, val reason: String, val provider: String, val version: String)
data class LaunchResult(val outcome: Outcome, val code: String? = null)

/** Only public Android launcher / HTTPS intents; never sends the requested email to Google. */
class FlowLauncher(private val context: Context) {
    private val pm = context.packageManager
    val managedProfile: Boolean get() = context.getSystemService(UserManager::class.java).isManagedProfile
    val profile: String get() = if (managedProfile) "WORK" else "PERSONAL"

    @Suppress("DEPRECATION")
    fun browsers(): List<Provider> {
        // Query a neutral HTTPS URL, so domain-specific Flow handlers are not treated as browsers.
        val intent = Intent(Intent.ACTION_VIEW, "https://example.org/".toUri()).addCategory(Intent.CATEGORY_BROWSABLE)
        val tabs = pm.queryIntentServices(Intent("android.support.customtabs.action.CustomTabsService"), 0)
            .map { it.serviceInfo.packageName }.toSet()
        return pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .map { it.activityInfo.packageName }.distinct().map { pkg ->
                val app = pm.getApplicationInfo(pkg, 0)
                Provider(pkg, pm.getApplicationLabel(app).toString(), version(pkg), pkg in tabs)
            }.sortedBy { it.label }
    }

    fun capability(method: LaunchMethod, provider: String): Capability {
        if (method == LaunchMethod.NATIVE) {
            val exists = pm.getLaunchIntentForPackage(NATIVE_PACKAGE) != null
            return Capability(exists, if (exists) "Layar utama tersedia; pemilihan akun belum terbukti."
                else "Aplikasi Flow tidak terpasang atau tidak dapat dibuka dalam profil ini.", NATIVE_PACKAGE, version(NATIVE_PACKAGE))
        }
        if (method == LaunchMethod.WORK_PROFILE && !managedProfile)
            return Capability(false, "Buka salinan BYGA di profil kerja yang sudah disediakan admin. Tidak ada perpindahan profil otomatis.", provider, "")
        val chosen = browsers().find { it.packageName == provider }
        if (chosen == null) return Capability(false, "Pilih browser yang terpasang dalam profil ini.", provider, "")
        if (method == LaunchMethod.CUSTOM_TAB && !chosen.customTabs)
            return Capability(false, "Browser ini tidak menyediakan layanan Custom Tabs.", provider, chosen.version)
        return Capability(true, "Provider tersedia; identitas dan isolasi sesi belum diverifikasi.", provider, chosen.version)
    }

    fun open(activity: Activity, method: LaunchMethod, provider: String): LaunchResult {
        val capability = capability(method, provider)
        if (!capability.available) return LaunchResult(Outcome.BLOCKED, "PROVIDER_UNAVAILABLE")
        return try {
            when (method) {
                LaunchMethod.NATIVE -> {
                    val intent = pm.getLaunchIntentForPackage(NATIVE_PACKAGE)
                        ?: return LaunchResult(Outcome.BLOCKED, "NATIVE_UNAVAILABLE")
                    activity.startActivity(intent)
                }
                LaunchMethod.CUSTOM_TAB -> {
                    val tab = CustomTabsIntent.Builder().setShowTitle(true)
                        .setShareState(CustomTabsIntent.SHARE_STATE_OFF).build()
                    tab.intent.setPackage(provider)
                    tab.launchUrl(activity, FLOW_URL.toUri())
                }
                LaunchMethod.BROWSER, LaunchMethod.WORK_PROFILE -> activity.startActivity(
                    Intent(Intent.ACTION_VIEW, FLOW_URL.toUri())
                        .addCategory(Intent.CATEGORY_BROWSABLE).setPackage(provider))
            }
            // startActivity success proves dispatch only. No automatic authentication inference.
            LaunchResult(Outcome.UNVERIFIED)
        } catch (_: ActivityNotFoundException) {
            LaunchResult(Outcome.FAILED, "ACTIVITY_NOT_FOUND")
        } catch (_: SecurityException) {
            LaunchResult(Outcome.FAILED, "POLICY_DENIED")
        }
    }

    @Suppress("DEPRECATION")
    private fun version(pkg: String): String = try { pm.getPackageInfo(pkg, 0).versionName.orEmpty() }
        catch (_: PackageManager.NameNotFoundException) { "" }

    companion object {
        const val NATIVE_PACKAGE = "com.google.android.apps.labs.whisk"
        const val FLOW_URL = "https://labs.google/fx/tools/flow"
    }
}
