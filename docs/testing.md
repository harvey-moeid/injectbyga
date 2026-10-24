# Validasi organizer satu browser — v0.2.0-manual

Scope: pengguna menyetujui satu browser dengan pergantian akun manual di Flow. Pengujian engineering tidak membuktikan login Google atau isolasi otomatis. Laporan Phase0 sebelumnya disimpan di [phase0-testing.md](phase0-testing.md).

## Lingkungan dan perintah

Codex Cloud Linux x86_64, JDK17 lengkap, Android SDK35/build-tools35.0.0, Gradle wrapper8.11.1. Compiler/configuration errors diperbaiki dari sumber; tidak memakai lint baseline/suppression untuk menutupi error. Proxy/CA lingkungan dipertahankan; cache SDK/Gradle berada pada lokasi writable. Tidak ada HP fisik/akun Google di cloud dan /dev/kvm tidak tersedia untuk emulator lokal.

```bash
./gradlew --no-daemon --stacktrace help lintDebug testDebugUnitTest assembleDebug assembleDebugAndroidTest
python3 scripts/check_feasibility_gate.py
python3 scripts/check_feasibility_gate.py --require-go
```

Perintah --require-go tetap harus gagal ketika isolasi UNVERIFIED; persetujuan organizer manual tidak menaikkan gate atau mengizinkan produksi.

## Tes yang tersedia

- IdentityRulesTest (7) dan MetadataCipherTest (4): state manual/mismatch/expiry, penolakan stale outcome, email validation dan AES-GCM IV/tamper/AAD/wrong-key.
- AccountImportTest (8): BOM/CRLF, quoted comma dan escaped quotes, duplicate detection, invalid-row reporting tanpa raw rejected content, credential header rejection, malformed CSV, batas ukuran/baris, formula-safe export, empty preview.
- OrganizerRulesTest (3): gabungan search/favorite/group, sorting deterministik dan validasi metadata.
- PocPersistenceTest (2) dan PocUiSmokeTest (1) tetap menguji lab lama menggunakan metadata sintetik.
- OrganizerPersistenceTest (5): enkripsi/reopen, duplicate recheck saat import, delete group tanpa kehilangan akun, same-browser history/mismatch/stale review, failed/stale launch, 50 akun serta bulk move/delete.
- DashboardPreferencesTest (1): satu browser global dan display/sort/filter dipersistenkan antar instance repository; bukan bukti reboot perangkat.
- BrowserIntentTest (1): Intent mempin provider eksplisit dan tidak berisi email/authuser/token/query/extras.
- DashboardUiTest (1): tambah/hapus metadata, search, grid dan dialog yang mengharuskan memilih akun manual di Flow. Tidak membuka Google/browser.

Total sumber tes: 22 JVM unit tests, 11 Android instrumentation tests. Kompilasi APK tes tidak sama dengan menjalankan tes Android.

## Hasil aktual saat pengembangan

Build lokal organizer pertama berhasil dalam1m27s; 22 unit tests dijalankan dengan0 failures/errors/skipped. APK debug dan instrumentation APK terbentuk; apksigner verify exit0. Lint pertama melaporkan0 error dan1 warning ukuran window; penggunaan LocalWindowInfo serta icon AutoMirrored diperbaiki, lalu pemeriksaan diulang. Pemeriksaan ulang setelah perbaikan berhasil dalam1m12s: lint **No issues found**, 22 unit tests lulus (0 failures/errors/skipped), debug APK dan test APK terbentuk. Bukti: [organizer-local-checks.txt](build-evidence/organizer-local-checks.txt). Run CI/emulator akan dicatat setelah tersedia.

Laporan lokal: app/build/reports/lint-results-debug.html, app/build/reports/tests/testDebugUnitTest/index.html, app/build/test-results/testDebugUnitTest/*.xml. APK: app/build/outputs/apk/debug/app-debug.apk. Log build sementara: /tmp/byga-tools/organizer-build.log.

## Pengujian HP yang masih diperlukan

- Google account picker pada perangkat dengan akun Google sungguhan dan pembatalan/penolakan kebijakan OEM.
- Flow dibuka oleh browser pilihan, pengguna memilih A/B/C secara manual dan memeriksa identitas sebelum membuat proyek. Ini bukan acceptance isolasi otomatis.
- Permintaan login ulang, expiry aktual, browser dihapus/nonaktif, offline behavior provider.
- Room/DataStore/Keystore setelah force-stop/reboot, upgrade APK dengan signature yang sama, kemampuan OEM backup exclusion.
- Biometrik/PIN/pola perangkat: enable/cancel, lock setelah background, balik dari browser/file picker, akses Lab, proses mati dan rotasi.
- Font besar, screen reader, ukuran layar kecil/tablet, scroll 50 akun dan performa HP entry-level.
- SAF import/export ke provider file nyata; ekspor CSV plaintext hanya setelah konfirmasi, tanpa password/OTP/session data.

Tidak ada klaim tested successfully untuk skenario-skenario tersebut. Persetujuan scope manual dipertahankan terpisah dari hasil isolasi UNVERIFIED.

Pemeriksaan kode final `560fbaf`: `lintDebug assembleDebug assembleDebugAndroidTest` berhasil dalam55s (12 task dieksekusi,65 up-to-date). Lint lokal tetap No issues found dan signature APK valid. 22 hasil unit test sebelumnya tetap berlaku karena logika domain tidak berubah.

## CI kode final dan unduhan

Run [40047505432](https://github.com/harvey-moeid/injectbyga/actions/runs/40047505432), commit kode `560fbafb75785c1d52ea6102d9327c48888b8e6e`: job debug-apk **success**. SDK/Gradle/lint/unit/build debug dan test APK lulus. Lint runner memiliki informasi versi yang lebih baru dan melaporkan **0 errors,14 warnings** (1 OldTargetApi,13 GradleDependency), seluruhnya terlihat di log/report; error asli tetap fatal. Tidak memakai baseline/suppression atau continue-on-error untuk menyembunyikan error.

**Run selesai: kedua job success. Sebanyak11 instrumentation tests benar-benar dijalankan dan lulus di emulator Android12/API31** (01:05 UTC,24 Oktober2026). Log mencatat Starting11 tests, Finished11 tests dan BUILD SUCCESSFUL dalam5m7s. Ini mencakup test database50 akun/bulk, duplicate recheck, cascade delete, snapshot/stale confirmation/mismatch, DataStore, Intent browser tanpa auth data, dashboard CRUD/search/grid/warning dan lab lama. Tidak ada akun Google sungguhan atau autentikasi Google yang diuji. Perubahan setelah commit kode CI hanya laporan dokumentasi.

- [BYGA-Flow-Manager-Debug-APK](https://github.com/harvey-moeid/injectbyga/actions/runs/40047505432/artifacts/12542067641), ID12542067641, ZIP16.983.454 bytes. SHA256 arsip ZIP: `5299a69c25a22568f0a2d311cfa4ed4bc965c26857c19aa3a307a7662fd80301`. Ini hash ZIP artifact CI, berbeda dari raw APK lokal/signature debug cloud.
- [Laporan lint/JUnit dan test APK](https://github.com/harvey-moeid/injectbyga/actions/runs/40047505432/artifacts/12542012916).
- [Laporan11 tes emulator](https://github.com/harvey-moeid/injectbyga/actions/runs/40047505432/artifacts/12542441141).

Bukti ringkas final: [organizer-ci-checks.txt](build-evidence/organizer-ci-checks.txt). Pengujian HP yang masih diperlukan di atas tetap belum dinyatakan lulus oleh hasil emulator ini.

Artifact run ini berlaku sampai22 Januari2027 dan dapat memerlukan login GitHub. Kode setelah commit CI tersebut hanya diperbarui bila ada error; pembaruan hasil akhir dokumentasi tidak mengubah aplikasi.
