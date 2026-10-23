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

Hasil akhir build/check dan artifact dicatat setelah perintah selesai. Sampai ada hasil tersebut, tidak mengklaim kelulusan.

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
