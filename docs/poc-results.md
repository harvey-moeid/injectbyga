# Hasil PoC — BYGA Flow Manager

**Feasibility gate: UNVERIFIED. Full application tidak dilanjutkan.**

Tanggal investigasi: 23 Oktober 2026 UTC (24 Oktober 2026 WIB). PRD asli disalin utuh ke `PRD.md`. Lingkungan: Codex Cloud Linux, tanpa HP Android fisik, tanpa akun Google uji, tanpa profil kerja atau Flow terpasang. Tidak ada pengujian identitas nyata yang dijalankan. Tidak ada video/screenshot akun yang bisa dijadikan bukti isolasi. Status ini bukan NO-GO teknis: belum ada bukti cukup untuk keputusan tersebut.

## Investigasi mekanisme resmi

| Metode | Temuan sumber resmi dan implementasi | Hasil identitas/isolasi |
|---|---|---|
| Flow Android resmi | Google Help merekomendasikan aplikasi Android; Play mencantumkan package `com.google.android.apps.labs.whisk`. PoC membuka launcher activity publik. Dokumentasi yang diperiksa tidak memberikan kontrak pemilih akun via Intent pihak ketiga. | **UNVERIFIED**; provider tidak tersedia di lingkungan cloud. |
| Intent / deep link | Standard launcher Intent dan ACTION_VIEW HTTPS tersedia sebagai mekanisme Android. Tidak ditemukan kontrak resmi account-targeted Flow deep link dalam sumber yang diperiksa/pencarian dokumentasi. Tidak menebak private extra, `authuser`, atau parameter login. | **BLOCKED** untuk verifikasi/pemilihan identitas otomatis: mekanisme resmi yang diperlukan belum ditemukan. Ini bukan bukti seluruh kemungkinan API tidak ada. |
| Custom Tabs standar | Dokumentasi Chrome menyatakan state dibagi dengan browser. PoC mempin provider yang mengekspos layanan Custom Tabs. Tab berbeda tidak diperlakukan sebagai cookie jar independen. | **UNVERIFIED** pada Flow/perangkat; tidak ada dasar mengklaim isolasi per slot. |
| Browser eksternal terpilih | PoC memakai ACTION_VIEW HTTPS dengan package eksplisit. Browser berbeda per slot dapat diteliti, tetapi belum terbukti menjaga identitas/persistensi Flow. | **UNVERIFIED**. |
| Work Profile yang tersedia | Android Enterprise mendokumentasikan pemisahan data personal/kerja dan pembatasan Intent antar profil. PoC hanya berjalan dalam konteks profil saat ini; rute kerja BLOCKED bila bukan managed profile. | **BLOCKED** di cloud; isolasi Flow A/B/C **UNVERIFIED**. Tidak ada provisioning/multi-profile engine. |
| Ephemeral Custom Tabs | Data sesi dihapus ketika tab ditutup menurut dokumentasi Chrome. Tidak memenuhi target sesi persisten dan tidak dipakai sebagai solusi. | **BLOCKED** untuk target persistensi berdasarkan batas mekanisme, bukan FAIL hasil tes Flow. |
| Fullscreen dalam aplikasi | Tidak ada integrasi fullscreen resmi untuk situs Flow pihak ketiga yang dibuktikan dalam investigasi. Login WebView dilarang PRD/kebijakan Google. | **BLOCKED** sampai tersedia mekanisme sah dan bukti. |

## Matriks acceptance perangkat nyata

Kolom hasil di bawah berlaku untuk setiap rute; belum ada satu pun rute yang diuji memakai akun nyata.

| ID | Skenario | Status | Bukti / alasan |
|---|---|---|---|
| T01 | A → B → C | **UNVERIFIED** | Tidak ada HP/akun Google uji. |
| T02 | C → A → B | **UNVERIFIED** | Tidak ada HP/akun Google uji. |
| T03 | Pergantian berulang ≥20 kali | **UNVERIFIED** | 0 pembukaan Flow nyata; 0 observasi identitas. |
| T04 | Restart BYGA, lalu buka ulang A/B/C | **UNVERIFIED** | Belum dijalankan di Android. |
| T05 | Restart Android | **UNVERIFIED** | Belum dijalankan di Android. |
| T06 | Sesi satu akun kedaluwarsa | **UNVERIFIED** | Tidak ada sesi nyata/sinyal expiry. |
| T07 | Memilih B tidak menghasilkan akun A | **UNVERIFIED** | Unit test mismatch hanya menguji aturan lokal; bukan identitas Flow. |
| T08 | Dashboard PoC offline / error Flow | **UNVERIFIED** | Perlu mode pesawat dan observasi UI nyata. |
| T09 | Native, Custom Tabs, browser berbeda | **UNVERIFIED** | Belum ada versi provider/perangkat yang diuji. |
| T10 | Work Profile | **BLOCKED** | Tidak tersedia managed profile di lingkungan ini. |

## Validasi engineering

Hasil build/check yang benar-benar dijalankan dicatat di `docs/testing.md`. Tidak satu pun hasil engineering menetapkan PASS untuk tabel identitas di atas. APK debug hanya alat eksperimen. Instrumentation persistence memakai data email sintetik, bukan login Google.

## Keputusan dan langkah lanjut

Gate tetap **UNVERIFIED**. Tidak membangun dashboard penuh, account import/picker, isolation engine, atau workspace berdasarkan asumsi sesi independen. Tidak memublikasikan APK produksi.

Tester perlu memasang APK eksperimen pada HP Android 12+, melakukan setup login A/B/C yang sah, mengikuti `docs/poc-test-plan.md`, mengekspor laporan pseudonim dan melampirkan bukti layar/video tersamarkan. Catat versi perangkat/Flow/browser, pemetaan slot ke provider/profil, run IDs, intervensi manual, dan hasil setiap urutan/restart/expiry. Jika metode terbukti memenuhi seluruh kriteria, reviewer memperbarui hasil serta `feasibility-gate.json` dengan GO dan lokasi bukti. Bila hanya sebagian terbukti, dokumentasikan CONDITIONAL GO dan batasannya; full scope tetap tidak boleh dianggap terpenuhi.

## Template bukti perangkat (belum diisi)

| Field | Nilai |
|---|---|
| Tester / reviewer | Belum tersedia |
| Model / Android build | Belum tersedia |
| Flow package/version | Belum tersedia |
| Browser package/version | Belum tersedia |
| Profil dan pemetaan A/B/C | Belum tersedia |
| Hash APK / commit | Diisi tester sesuai build terpasang |
| Run IDs / lampiran JSON | Belum tersedia |
| Video/screenshot tersamarkan | Belum tersedia |
| Jumlah pembukaan / mismatch / koreksi manual | Belum diuji |
| Hasil per T01–T10 / keterbatasan | Belum diuji |

## Referensi resmi yang diperiksa

- [Google Flow Android Help](https://support.google.com/flow/answer/16353333?co=GENIE.Platform%3DAndroid&hl=en)
- [Google Flow di Google Play — Google Labs](https://play.google.com/store/apps/details?id=com.google.android.apps.labs.whisk)
- [Android Custom Tabs overview](https://developer.chrome.com/docs/android/custom-tabs)
- [Ephemeral Custom Tabs](https://developer.chrome.com/docs/android/custom-tabs/guide-ephemeral-tab)
- [Android Enterprise developer guide](https://developer.android.com/work/guide)
- [Google OAuth 2.0 policies](https://developers.google.com/identity/protocols/oauth2/policies)

Sumber-sumber tersebut menjelaskan kemampuan/batas platform; tidak membuktikan hasil pengujian BYGA. Dokumentasi dapat berubah dan perlu ditinjau ulang bersama versi provider saat tes nyata.
