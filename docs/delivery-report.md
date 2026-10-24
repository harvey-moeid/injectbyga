# Laporan implementasi — satu browser/manual

Keputusan pengguna terbaru **“satu browser saja”** diterapkan sebagai organizer dengan pemilihan akun manual di Flow. Addendum PRD dan scope-manual-switching.md mengatasi syarat isolasi otomatis baseline; gate isolasi tetap UNVERIFIED, bukan GO baru.

## Implementasi

- Dashboard akun dengan alias/email/avatar inisial/warna, grup, favorit, catatan proyek, search, filter, sorting dan list/grid persisten.
- Tambah manual, Android Google account chooser berbasis persetujuan (metadata saja), impor CSV/TXT defensif dengan preview, validation, duplicates dan laporan penolakan.
- CRUD akun/grup serta bulk move/delete; hapus grup tidak menghapus akun, hapus akun menghapus riwayat dashboard terkait, bukan Google Account/browser/PoC/file ekspor.
- Satu browser global untuk seluruh akun. Dialog menampilkan target dan mengharuskan pemilihan akun manual di Flow. Dispatch hanya UNVERIFIED, tidak ada fallback atau klaim login otomatis.
- Riwayat snapshot terenkripsi; pengamatan actual email cocok/manual, mismatch, login prompt, expiry eksplisit dan unknown. Pending lama tidak bisa dikonfirmasi setelah pembukaan baru.
- Room database organizer terpisah, DataStore preferensi dan AES-GCM Android Keystore untuk metadata sensitif. Shared key generation disinkronkan lintas instance.
- Kunci dashboard/Lab opsional memakai biometrik atau credential perangkat. Tidak menyimpan PIN/password sendiri.
- CSV export email/alias/grup dengan peringatan plaintext dan formula neutralization. Tidak mengandung sesi/login Google; bukan full backup favorit/catatan/history.
- Database/lab PoC lama dipertahankan. Penyalinan metadata slot lama melalui preview eksplisit dan deduplikasi.
- CI debug APK, lint, JVM dan instrumentation emulator; produksi tetap diblokir dan tidak merge otomatis.

## Validasi dan artifact

Build lokal **berhasil**, lint **No issues found**, 22 unit tests **lulus** dengan0 failures/errors/skipped. APK debug dan instrumentation APK terbentuk; apksigner verify lulus. Bukti: [organizer-local-checks.txt](build-evidence/organizer-local-checks.txt). Hasil CI/emulator aktual dan lokasi unduhan dicatat pada [testing.md](testing.md) setelah tersedia.

Hasil final [CI40047505432](https://github.com/harvey-moeid/injectbyga/actions/runs/40047505432): kedua job **success**, unit test/build lulus dan **11 tes emulator Android12/API31 benar-benar dijalankan serta lulus**. Lint CI0 error/14 warning versi/target yang tetap terlihat. [Unduh APK v0.2.0-manual](https://github.com/harvey-moeid/injectbyga/actions/runs/40047505432/artifacts/12542067641). Bukti: [organizer-ci-checks.txt](build-evidence/organizer-ci-checks.txt). Commit kode tervalidasi560fbaf; perubahan berikutnya hanya dokumentasi hasil.

APK lokal: app/build/outputs/apk/debug/app-debug.apk. Nama artifact CI: BYGA-Flow-Manager-Debug-APK. Semua kode/dokumen relevan dikomit/push pada branch codex/byga-phase0-poc dan [PR #1](https://github.com/harvey-moeid/injectbyga/pull/1) diperbarui tanpa merge. Tidak menerbitkan APK produksi.

## Batas nyata

Tidak ada pengujian Google login/identitas aktual oleh Codex. Pemilih akun perangkat nyata, browser/Flow sebenarnya, PIN/biometrik/OEM, force-stop/reboot, upgrade signature sama, SAF providers, aksesibilitas/performa entry-level masih perlu diuji di HP. Android emulator menggunakan metadata sintetik dan tidak membuktikan sesi Google.

Sesi independen, pergantian akun otomatis, pemulihan proyek dan progress video background bukan kemampuan scope manual. Laporan isolasi beserta pengamatan pengguna sebelumnya tetap tersedia di poc-results.md. Versi ini tidak diklaim memenuhi seluruh acceptance isolasi baseline.

## File dibuat/diubah sejak versi PoC

```text
.github/workflows/android-ci.yml
AGENTS.md
PRD.md
README.md
app/build.gradle.kts
app/schemas/com.byga.flowmanager.organizer.data.OrganizerDatabase/1.json
app/src/androidTest/java/com/byga/flowmanager/PocUiSmokeTest.kt
app/src/androidTest/java/com/byga/flowmanager/organizer/BrowserIntentTest.kt
app/src/androidTest/java/com/byga/flowmanager/organizer/DashboardPreferencesTest.kt
app/src/androidTest/java/com/byga/flowmanager/organizer/DashboardUiTest.kt
app/src/androidTest/java/com/byga/flowmanager/organizer/OrganizerPersistenceTest.kt
app/src/main/AndroidManifest.xml
app/src/main/java/com/byga/flowmanager/BygaApplication.kt
app/src/main/java/com/byga/flowmanager/MainActivity.kt
app/src/main/java/com/byga/flowmanager/PocActivity.kt
app/src/main/java/com/byga/flowmanager/SecureBygaActivity.kt
app/src/main/java/com/byga/flowmanager/launcher/FlowLauncher.kt
app/src/main/java/com/byga/flowmanager/organizer/data/DashboardPreferences.kt
app/src/main/java/com/byga/flowmanager/organizer/data/OrganizerDatabase.kt
app/src/main/java/com/byga/flowmanager/organizer/data/OrganizerRepository.kt
app/src/main/java/com/byga/flowmanager/organizer/domain/AccountImport.kt
app/src/main/java/com/byga/flowmanager/organizer/domain/OrganizerModels.kt
app/src/main/java/com/byga/flowmanager/organizer/ui/DashboardScreen.kt
app/src/main/java/com/byga/flowmanager/organizer/ui/DashboardViewModel.kt
app/src/main/java/com/byga/flowmanager/organizer/ui/OrganizerComponents.kt
app/src/main/java/com/byga/flowmanager/organizer/ui/OrganizerDialogs.kt
app/src/main/java/com/byga/flowmanager/security/MetadataCipher.kt
app/src/test/java/com/byga/flowmanager/organizer/AccountImportTest.kt
app/src/test/java/com/byga/flowmanager/organizer/OrganizerRulesTest.kt
docs/acceptance-checklist.md
docs/architecture.md
docs/build-evidence/organizer-ci-checks.txt
docs/build-evidence/organizer-local-checks.txt
docs/delivery-report.md
docs/feasibility-gate.json
docs/phase0-architecture.md
docs/phase0-delivery-report.md
docs/phase0-testing.md
docs/poc-results.md
docs/prd-baseline-v1.md
docs/scope-manual-switching.md
docs/security.md
docs/testing.md
scripts/check_feasibility_gate.py
```
