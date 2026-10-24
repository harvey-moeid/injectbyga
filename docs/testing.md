# Validasi engineering Phase 0

Pengujian di bawah tidak membuktikan identitas Google atau sesi terisolasi. Untuk itu lihat `poc-results.md` dan jalankan pengujian HP nyata di `poc-test-plan.md`.

## Lingkungan lokal Codex Cloud

Linux x86_64. Runtime bawaan Java 21 tidak menyediakan `javac`, sehingga percobaan awal build gagal pada resolusi task compiler. Toolchain JDK 17 lengkap dipasang pada direktori temp; SDK platform 35, build-tools 35.0.0, platform-tools dipasang lewat sdkmanager. Gradle wrapper 8.11.1 dengan checksum distribusi dipin. Cache Android dan Gradle diarahkan ke direktori temp writable, proxy/CA lingkungan dipertahankan. Tidak ada HP fisik/akun Google/Flow/browser Android terpasang; `/dev/kvm` tidak tersedia.

## Perintah reproduksi

Dengan JDK 17 lengkap dan Android SDK tersedia:

```bash
./gradlew --no-daemon --stacktrace help lintDebug testDebugUnitTest assembleDebug assembleDebugAndroidTest
python3 scripts/check_feasibility_gate.py
python3 scripts/check_feasibility_gate.py --require-go
```

Perintah terakhir **harus exit nonzero** saat gate UNVERIFIED. Ini guard rilis yang diharapkan, bukan kegagalan login atau build.

## Cakupan otomatis

- `IdentityRulesTest`: memilih B tetapi melihat A menghasilkan MISMATCH; normalisasi yang terbatas; email tidak valid ditolak; status login prompt berbeda dari expiry; state gagal/blocked/confirmed tidak dapat direview kembali; UNKNOWN tidak menjadi verified.
- `MetadataCipherTest`: AES-GCM roundtrip, IV acak, tamper rejection, AAD mencegah pertukaran field/slot, kunci berbeda ditolak. Unit test memakai kunci JCA sintetik; bukan bukti hardware Keystore Android.
- `PocPersistenceTest` (Android instrumentation): kunci Android Keystore dan Room reopen mempertahankan metadata sintetik, ciphertext tidak plaintext; launch baru membatalkan pending lama, stale review ditolak dan B→A dicatat MISMATCH.
- `PocUiSmokeTest` (Android instrumentation): layar gate, konfigurasi slot email sintetik, dialog peringatan launch dan cancel. Tidak membuka Google.

`assembleDebugAndroidTest` hanya mengompilasi tes perangkat. `connectedDebugAndroidTest` baru menjalankan tes jika emulator/perangkat tersedia. GitHub Actions juga menyiapkan job emulator API 31 untuk instrumentation; hasil aktual harus diambil dari run, bukan diasumsikan.

## Hasil aktual

Clean build pada 23 Oktober 2026 UTC benar-benar dijalankan:

| Check | Hasil aktual |
|---|---|
| Gradle `clean help lintDebug testDebugUnitTest assembleDebug assembleDebugAndroidTest` | **BUILD SUCCESSFUL**, 1m 13s, 85 task dieksekusi |
| Lint Android | **Lulus**, `No issues found.` |
| JVM unit tests | **11 lulus**, 0 failures, 0 errors, 0 skipped (7 identity rules, 4 crypto) |
| APK debug | Terbentuk, 29.147.557 bytes; package/minSdk/targetSdk diverifikasi dengan aapt |
| APK signing debug | `apksigner verify --verbose` exit 0; signature v2 valid |
| Instrumentation APK | Berhasil dikompilasi; ini belum berarti tes berjalan |
| Workflow syntax | actionlint 1.7.7 exit 0 untuk kedua workflow |
| Manifest feasibility | Valid; `--require-go` exit 1 sesuai harapan karena UNVERIFIED |
| PRD | `cmp` identik dengan lampiran asli |

Setelah penyesuaian sinkronisasi tes UI, `lintDebug assembleDebugAndroidTest` dijalankan kembali: **BUILD SUCCESSFUL**, 36s (8 task dieksekusi, 51 up-to-date). Kode aplikasi dan hash APK debug tetap sama.

Bukti ringkas: [build-evidence/local-checks.txt](build-evidence/local-checks.txt). Laporan lokal: `app/build/reports/lint-results-debug.html`, `app/build/reports/tests/testDebugUnitTest/index.html`, dan XML `app/build/test-results/testDebugUnitTest/`. Build log cloud tersimpan sementara di `/tmp/byga-tools/build.log`.

APK lokal: `app/build/outputs/apk/debug/app-debug.apk`.

SHA-256 APK lokal (signature debug cloud; hash artifact runner CI dapat berbeda karena debug key):

```text
2aadf21adbd0cd90b592ab82b8955ec76fa0729eebbb79703ae5b3f1b694ce63
```

Perbaikan selama validasi: runtime Java bawaan tidak memiliki javac (diselesaikan dengan JDK 17); cache SDK dipindahkan ke direktori writable; empat temuan strict lint (qualifier resource di bawah minSdk dan helper URI) diperbaiki tanpa suppression/baseline. Setelah resource dipindahkan, incremental resource linking sempat membawa state lama; clean build berhasil. Warning strip simbol library native pada debug APK tidak menghambat pengemasan.

CI run awal [40041786133](https://github.com/harvey-moeid/injectbyga/actions/runs/40041786133) gagal pada setup SDK karena action meminta package lama `tools` yang tidak tersedia. Workflow diperbaiki dengan command-line tools dipin dan `packages: platform-tools`, lalu dipush ulang. Run perbaikan [40041893069](https://github.com/harvey-moeid/injectbyga/actions/runs/40041893069) berhasil melewati setup SDK, tetapi job debug gagal pada lint: 14 temuan diperlakukan sebagai error oleh `warningsAsErrors`, dengan `OldTargetApi` sebagai temuan pertama. Runner memiliki informasi versi/target lebih baru daripada pemeriksaan lokal.

Kebijakan diperbaiki menjadi `abortOnError = true`, `warningsAsErrors = false`: error lint asli tetap fatal, warning tetap ada dalam report dan kini dicetak penuh pada log CI, tanpa suppression atau baseline. Toolchain stabil Phase 0 sengaja dipin; pembaruan target SDK/dependensi dan pengujian kompatibilitas perangkat lebih baru perlu ditinjau sebelum produksi setelah GO. Status akhir run berikutnya dan artifact dicatat setelah tersedia; run yang masih berjalan tidak dianggap lulus.

Job `android-persistence` pada run **40041893069** benar-benar menjalankan `connectedDebugAndroidTest` di emulator Android 12/API 31: **3 tes selesai, BUILD SUCCESSFUL**, job conclusion **success** (23:55 UTC). Dua tes Room/Keystore dan satu tes UI memakai metadata sintetik; tidak ada akun Google atau login yang diuji. Log emulator mengandung warning console/GPU, tetapi tidak menggagalkan ketiga tes. Laporan tersedia sebagai artifact `BYGA-Phase0-Android-Test-Reports` pada run tersebut.

Setelah perubahan kebijakan lint, seluruh `help lintDebug testDebugUnitTest assembleDebug assembleDebugAndroidTest` lokal diulang: **BUILD SUCCESSFUL**, 37s (9 task dieksekusi, 75 up-to-date). Check unit test yang tidak berubah menggunakan hasil valid yang up-to-date; tidak dihitung sebagai pengujian login baru.

Run CI kode final [40042430787](https://github.com/harvey-moeid/injectbyga/actions/runs/40042430787), commit `107d7da907e8d09b6c705c72ae14967785a3e24c`: job **debug-apk success** (24 Oktober 2026 UTC). Setup SDK, validasi wrapper/Gradle, lint, unit tests, assembleDebug dan assembleDebugAndroidTest lulus. Lint runner melaporkan **0 errors, 14 warnings**: 1 `OldTargetApi` dan 13 rekomendasi `GradleDependency`. Warning terbuka pada log dan artifact; tidak ada error lint yang diabaikan. Perubahan setelah commit tersebut hanya dokumentasi hasil, bukan kode aplikasi.

Artifact APK yang benar-benar tersedia:

- [BYGA-Flow-Manager-Debug-APK](https://github.com/harvey-moeid/injectbyga/actions/runs/40042430787/artifacts/12540280828), ID `12540280828`, ZIP 10.212.145 bytes (APK di dalam ZIP). SHA-256 **arsip ZIP**: `845f9fb44d50d53dfb757833258e77ee72ceebe2a06905647e69a7eeaabd7a4c`; ini berbeda dari hash APK lokal di atas.
- [BYGA-Phase0-Test-Reports](https://github.com/harvey-moeid/injectbyga/actions/runs/40042430787/artifacts/12540325664), berisi lint/JUnit report dan test APK.
- [BYGA-Phase0-Android-Test-Reports](https://github.com/harvey-moeid/injectbyga/actions/runs/40042430787/artifacts/12539707575), hasil 3 tes emulator Android 12/API 31 pada kode final.

**Run 40042430787 selesai dengan kedua job success.** Job `android-persistence` menjalankan dan menyelesaikan 3 tes pada emulator Android 12 (00:02 UTC); `connectedDebugAndroidTest` melaporkan BUILD SUCCESSFUL dalam 3m 44s. Ini mengulang konfirmasi engineering pada kode final, tetap tanpa akun/login Google. Tidak ada pengujian isolasi identitas sungguhan yang dilakukan.

Artifact Actions memiliki masa berlaku (run ini hingga 21 Januari 2027); unduh dari run yang berhasil. Ini APK eksperimen debug, bukan rilis produksi. Lihat juga [build-evidence/ci-checks.txt](build-evidence/ci-checks.txt).

## Tes Android nyata yang wajib

Belum tersedia bukti untuk login Google, A/B/C, 20 perpindahan, reboot, expiry, sesi independen, offline UI provider, dan aksesibilitas/performa entry-level. Status gate tetap **UNVERIFIED** meskipun seluruh tes engineering lulus. Manual tester harus mencatat versi provider, run IDs, screenshot/video tersamarkan, dan semua intervensi.

## CI

`android-ci.yml` memvalidasi Gradle, lint, unit tests, assembleDebug, compile instrumentation APK, upload `BYGA-Flow-Manager-Debug-APK` serta laporan. Job kedua memakai emulator API 31 untuk persistence/UI tests sintetik. Setiap langkah menggunakan stacktrace; laporan diunggah dengan `if: always()`.

`apk-release.yml` adalah workflow manual: memerlukan GO dengan evidence file sebelum mengakses environment/signing secret. Tidak membuat publikasi produksi otomatis. File gate ditinjau bersama bukti; skrip bukan validator kebenaran observasi manusia.

## Batas / risiko yang masih ada

- Native launcher tidak memilih akun; Custom Tabs berbagi sesi. Tidak ada hasil perangkat yang membuktikan target produk.
- Browser/Flow dapat berubah versi/perilaku, menolak akses wilayah/akun, atau melakukan navigasi berikutnya sendiri.
- BYGA tidak mengamati jaringan/expiry provider secara otomatis. Dispatch Intent saat offline tetap bisa UNVERIFIED; tester harus mencatat apa yang terlihat di Flow.
- Work Profile memerlukan konteks instalasi kerja yang sudah dikelola admin dan tidak menjamin A/B/C atau puluhan sesi.
- Konfirmasi manual bukan bukti status sesi sekarang; bukti dapat salah bila tester salah memasukkan email.
- Keystore hilang/data rusak memerlukan reset lokal, bukan plaintext recovery. Ekspor yang sudah dibuat tidak dihapus oleh reset.
