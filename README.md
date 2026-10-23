# BYGA Flow Manager

APK Android eksperimen **Phase 0** untuk meneliti pembukaan Google Flow dengan target akun A/B/C. **Feasibility gate: UNVERIFIED**. Belum ada bukti sesi Google Flow independen pada perangkat nyata; aplikasi penuh sesuai PRD belum dibangun.

PoC menyediakan tiga slot email, rute native/Custom Tabs/browser terpilih/profil kerja yang tersedia, peringatan sebelum membuka, konfirmasi email yang benar-benar dilihat tester, hasil MISMATCH, riwayat persisten, seri uji, serta ekspor JSON tanpa email. Tidak melakukan login Google dan tidak menjamin akun yang dipilih menjadi akun aktif Flow.

## Persyaratan

- HP Android 12+ (API 31), tiga akun Google milik tester yang memenuhi akses Flow, dan koneksi internet.
- Pasang [Google Flow resmi dari Google Labs](https://play.google.com/store/apps/details?id=com.google.android.apps.labs.whisk) untuk uji native terlebih dahulu; Chrome/browser kompatibel untuk uji browser.
- Work Profile hanya opsional jika telah disediakan admin. Tidak perlu root, ADB, Shizuku, backend, atau API key untuk memakai PoC.

## Instalasi APK eksperimen

1. Buka tab **Actions → Android CI** pada repository GitHub dan pilih run yang berhasil.
2. Unduh artifact **BYGA-Flow-Manager-Debug-APK** (ZIP) dan ekstrak `app-debug.apk`.
3. Pindahkan ke HP, buka APK, lalu izinkan pemasangan dari sumber tersebut melalui UI Android bila diminta. Ini debug build untuk eksperimen; bukan APK produksi.
4. Buka **BYGA Flow Manager**. Jika memperbarui build, tanda tangan debug dari runner lain mungkin berbeda; konflik tanda tangan membutuhkan uninstall yang menghapus metadata lokal. Ekspor bukti terlebih dahulu. Jangan mengganti signing produksi dengan debug key.

APK hasil build lokal berada di `app/build/outputs/apk/debug/app-debug.apk`. Status build aktual, lokasi artifact, checksum dan batas validasi dicatat di [docs/testing.md](docs/testing.md). Keberadaan workflow tidak berarti GitHub Actions sudah berhasil menjalankannya.

## Penggunaan PoC

1. Di Eksperimen, atur email yang diharapkan pada Account A/B/C dan metode pembukanya. Email hanyalah metadata target, bukan kredensial login.
2. Mulai seri uji baru (native lebih dahulu). Login awal dilakukan sendiri di Flow/browser resmi.
3. Tekan **Buka Flow**, baca peringatan, lalu periksa menu akun aktif di Flow sebelum mengganti akun secara manual.
4. Kembali ke BYGA dan tekan **Catat identitas yang terlihat**. Ketik email aktual, termasuk bila salah. Target B tetapi email A tercatat sebagai **MISMATCH**. Login prompt/expiry/identitas tak diketahui memiliki hasil tersendiri.
5. Lanjutkan urutan A→B→C, C→A→B, ≥20 perpindahan, restart aplikasi/Android dan expiry. Ikuti [protokol lengkap](docs/poc-test-plan.md); jangan memperbaiki akun diam-diam sebelum mencatat hasil.
6. Ekspor JSON dari Riwayat dan sertakan bukti layar/video tersamarkan untuk review. Konfirmasi cocok adalah historis; pembukaan berikutnya perlu diperiksa ulang. Gate tidak otomatis berubah menjadi GO.

Tidak ada password/OTP/cookie/token Google yang diminta atau disimpan. Hapus metadata hanya menghapus data BYGA, bukan akun Google atau sesi browser. Tidak ada jaminan generasi video terus berjalan di background.

## Build dan pengujian

Pasang JDK 17 dan Android SDK (compile 35, build-tools 35.0.0, platform-tools). Android Studio dapat menyediakannya. Atur `ANDROID_HOME` atau buat `local.properties` berisi `sdk.dir=/path/ke/android-sdk` (jangan commit file tersebut).

```bash
chmod +x gradlew
./gradlew --no-daemon --stacktrace help lintDebug testDebugUnitTest assembleDebug assembleDebugAndroidTest
python3 scripts/check_feasibility_gate.py
```

Windows: gunakan `gradlew.bat`. Wrapper Gradle disertakan dan checksum distribusi dipin. Akses Maven Central, Google Maven dan distribusi Gradle diperlukan untuk build awal. Build cloud memakai proxy/toolchain lokal lingkungan; tidak perlu mengubah repository untuk itu.

Uji persistence/Keystore pada perangkat uji atau emulator yang terhubung untuk pengembangan:

```bash
./gradlew connectedDebugAndroidTest
```

Perintah instrumentation bukan ketergantungan pengguna aplikasi. Tes ini memakai email sintetik dan **tidak menguji autentikasi Google atau isolasi sesi**. Pengujian identitas wajib HP nyata sesuai [poc-results.md](docs/poc-results.md).

## CI dan signing

[Android CI](.github/workflows/android-ci.yml) berjalan pada push dan PR: setup JDK/SDK, validasi wrapper/konfigurasi, lint, unit tests, assembleDebug, kompilasi test APK, upload APK dan laporan. Tidak memakai secret pada PR.

[Manual signed APK](.github/workflows/apk-release.yml) diblokir selama `docs/feasibility-gate.json` bukan GO. Setelah bukti ditinjau dan gate diperbarui, konfigurasikan environment GitHub `production` dengan required reviewer dan secrets:

- `BYGA_KEYSTORE_BASE64`
- `BYGA_KEYSTORE_PASSWORD`
- `BYGA_KEY_ALIAS`
- `BYGA_KEY_PASSWORD`

Keystore hanya didecode dalam runner temp dan dihapus setelah job. Workflow menghasilkan signed candidate artifact, tanpa GitHub Release/Play Store publishing dan tanpa merge otomatis. Jangan menaikkan gate hanya untuk membuka workflow.

## Struktur dan cakupan

`app/` berisi Compose → ViewModel → Repository → Room/DataStore/Keystore dan `FlowLauncher`. Android minSdk 31, package `com.byga.flowmanager`. Warna navy `#080D19`, blue `#246BFD`, cyan `#16DBEE`; UI Bahasa Indonesia.

Dokumen utama:

- [PRD asli — keputusan #1–#7](PRD.md)
- [Arsitektur dan batas Phase 0](docs/architecture.md)
- [Rencana uji perangkat nyata](docs/poc-test-plan.md)
- [Hasil PoC dan feasibility gate](docs/poc-results.md)
- [Keamanan](docs/security.md)
- [Hasil engineering dan petunjuk tes](docs/testing.md)
- [Checklist acceptance](docs/acceptance-checklist.md)

Dashboard lengkap, account picker, impor CSV/TXT, grup, favorit, pencarian dan workspace menunggu **GO berbukti**. Kartu A/B/C pada PoC tidak menggantikan kebutuhan isolasi sesi produk penuh.
