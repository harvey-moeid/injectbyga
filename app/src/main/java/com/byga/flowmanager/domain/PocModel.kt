package com.byga.flowmanager.domain

import java.util.Locale

enum class LaunchMethod(val title: String, val warning: String) {
    NATIVE("Aplikasi Flow", "Membuka layar utama Flow. Tidak ada pemilih akun lewat Intent yang telah terbukti."),
    CUSTOM_TAB("Custom Tabs", "Tab berbagi sesi dengan browser terpilih. Tab baru bukan sesi akun baru."),
    BROWSER("Browser eksternal", "Sesi dikelola browser terpilih. Browser yang sama dapat membuka akun terakhir."),
    WORK_PROFILE("Profil kerja", "Hanya berjalan dari salinan BYGA di profil kerja yang sudah tersedia. Tidak membuat profil baru.")
}

enum class Observation(val title: String) {
    IDENTITY("Saya melihat email aktif di Flow"),
    NEEDS_AUTH("Flow meminta login / pemilihan akun"),
    EXPIRED("Flow secara jelas menyatakan sesi berakhir"),
    UNKNOWN("Identitas tidak dapat diperiksa")
}

enum class Outcome(val title: String) {
    STARTING("Pembukaan terputus / belum diketahui"),
    UNVERIFIED("Perlu konfirmasi akun di Flow"),
    VERIFIED_MANUAL("Cocok — konfirmasi manual historis"),
    MISMATCH("FAIL — akun yang terlihat berbeda"),
    NEEDS_AUTH("Perlu login resmi"),
    EXPIRED("Sesi berakhir — laporan tester"),
    UNKNOWN("UNVERIFIED — tidak dapat diperiksa"),
    BLOCKED("BLOCKED — metode tidak tersedia"),
    FAILED("FAIL — peluncuran ditolak"),
    ABANDONED("UNVERIFIED — digantikan pembukaan baru")
}

enum class Scenario(val title: String) {
    ABC("A → B → C"), CAB("C → A → B"),
    REPEAT_20("20 perpindahan: A → B → C berulang"),
    APP_RESTART("Restart aplikasi"), ANDROID_RESTART("Restart Android"),
    EXPIRATION("Sesi kedaluwarsa"), WRONG_ACCOUNT("Akun berbeda tanpa salah identitas"),
    OFFLINE("Tanpa jaringan"), EXPLORATION("Eksplorasi metode")
}

object IdentityRules {
    private val emailPattern = Regex("^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)+$")
    fun normalize(email: String): String = email.trim().lowercase(Locale.ROOT)
    fun isValid(email: String): Boolean {
        val normalized = normalize(email)
        val local = normalized.substringBefore('@')
        return normalized.length <= 254 && local.length in 1..64 &&
            !local.startsWith('.') && !local.endsWith('.') && !local.contains("..") &&
            emailPattern.matches(normalized)
    }
    fun review(current: Outcome, expected: String, observed: String, observation: Observation): Outcome {
        require(current == Outcome.UNVERIFIED) { "Percobaan ini tidak menunggu konfirmasi." }
        return when (observation) {
            Observation.IDENTITY -> {
                require(isValid(expected) && isValid(observed)) { "Isi email yang benar-benar terlihat di Flow." }
                if (normalize(expected) == normalize(observed)) Outcome.VERIFIED_MANUAL else Outcome.MISMATCH
            }
            Observation.NEEDS_AUTH -> Outcome.NEEDS_AUTH
            Observation.EXPIRED -> Outcome.EXPIRED
            Observation.UNKNOWN -> Outcome.UNKNOWN
        }
    }
}

data class AccountSlot(val id: String, val email: String = "", val method: LaunchMethod = LaunchMethod.NATIVE, val provider: String = "")

data class Trial(
    val id: String,
    val slot: String,
    val expected: String,
    val method: LaunchMethod,
    val provider: String,
    val providerVersion: String,
    val profile: String,
    val scenario: String,
    val run: String,
    val startedAt: Long,
    val outcome: Outcome,
    val reviewedAt: Long? = null,
    val observed: String? = null,
    val errorCode: String? = null
)
