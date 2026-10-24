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
