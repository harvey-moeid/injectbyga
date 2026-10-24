# Laporan implementasi Phase 0

**Status PoC: UNVERIFIED.** Kode eksperimen selesai dibangun; keberhasilan kebutuhan isolasi sesi belum terbukti. Full application tidak dilanjutkan sesuai feasibility gate. Keputusan PRD #1–#7 tetap berlaku.

## Yang benar-benar diimplementasikan

- APK Kotlin/Compose Material 3 dark premium, MVVM, min Android 12, package com.byga.flowmanager, UI Bahasa Indonesia.
- Tiga target Account A/B/C, validasi email/duplikat, konfigurasi rute/provider per slot, dialog risiko sebelum launch.
- Launcher native resmi melalui standard launcher activity; Custom Tabs provider eksplisit; browser HTTPS eksplisit; guard profil kerja dari salinan BYGA pada profil kerja yang telah tersedia.
- Riwayat snapshot per pembukaan, versi provider/profil, skenario dan run ID yang bertahan pada storage lokal.
- Konfirmasi tester dengan email aktual; B→A menghasilkan MISMATCH; pending lama tidak dapat dikonfirmasi setelah pembukaan baru. Sesi saat ini selalu belum diketahui.
- Room untuk metadata/history, DataStore untuk preferensi/seri, AES-GCM Android Keystore untuk email target/terlihat; backup dikecualikan; ekspor JSON tanpa email melalui pemilih file Android.
- Instruksi uji nyata, pencatatan UNKNOWN/NEEDS_AUTH/EXPIRED/FAILED/BLOCKED yang terpisah, loading/empty/error/busy states.
- CI push/PR untuk lint, unit test, build debug APK, artifact dan instrumentation emulator. Workflow release manual dengan signing secrets dan guard GO; tidak menerbitkan produksi.

## Bukti engineering dan artifact

Clean build lokal, lint, 11 unit test dan kompilasi test APK berhasil. Tanda tangan APK debug diverifikasi. Rincian perintah, hash, laporan dan status CI aktual ada di [testing.md](testing.md) dan [build-evidence/local-checks.txt](build-evidence/local-checks.txt).

[CI kode final](https://github.com/harvey-moeid/injectbyga/actions/runs/40042430787) menyelesaikan kedua job dengan **success**: build debug dan 3 tes emulator API 31. Lint CI: 0 error, 14 warning versi/target yang tetap terlihat. [Unduh artifact APK eksperimen](https://github.com/harvey-moeid/injectbyga/actions/runs/40042430787/artifacts/12540280828). Bukti detail: [ci-checks.txt](build-evidence/ci-checks.txt). Commit CI `107d7da` berisi kode final; perubahan setelahnya hanya laporan dokumentasi.

APK lokal: `app/build/outputs/apk/debug/app-debug.apk` (eksperimen). Artifact GitHub Actions: `BYGA-Flow-Manager-Debug-APK`. Pengujian emulator memakai metadata sintetik; hasil itu tidak menguji Google identity.

Branch: `codex/byga-phase0-poc`. [Draft PR #1](https://github.com/harvey-moeid/injectbyga/pull/1) dibuat tanpa merge. Semua sumber dan dokumentasi relevan dikomit; APK/cache/keystore tidak dikomit.

## Belum terpenuhi / keterbatasan

Tidak ada HP fisik atau akun Google di cloud. Belum ada bukti native Intent memilih identitas, sesi A/B/C independen, 20 perpindahan tanpa salah akun, persistensi sesi setelah reboot, atau isolasi saat expiry. Custom Tabs standar berbagi sesi browser. Profil kerja harus sudah ada dan jumlah profil tidak dapat diasumsikan. Tidak ada klaim progres video background atau verifikasi otomatis.

Dashboard lengkap, impor CSV/TXT, account picker, grup/favorit/search/list-grid dan multi-account workspace sengaja ditunda sampai GO. Tidak ada tombol palsu untuk fitur tersebut. Ikuti [poc-test-plan.md](poc-test-plan.md), isi [poc-results.md](poc-results.md) dengan bukti perangkat nyata, lalu review gate sebelum melanjutkan.

## File dibuat / diubah

Satu file awal README diperbarui; selebihnya merupakan file baru pada repository awal yang kosong. Inventori berikut mencakup sumber, resource, tes, build, CI, PRD, dan dokumentasi (artifact build lokal diabaikan Git):

```text
.gitattributes
.github/workflows/android-ci.yml
.github/workflows/apk-release.yml
.gitignore
AGENTS.md
PRD.md
README.md
app/build.gradle.kts
app/proguard-rules.pro
app/schemas/com.byga.flowmanager.data.PocDatabase/1.json
app/src/androidTest/java/com/byga/flowmanager/PocPersistenceTest.kt
app/src/androidTest/java/com/byga/flowmanager/PocUiSmokeTest.kt
app/src/main/AndroidManifest.xml
app/src/main/java/com/byga/flowmanager/BygaApplication.kt
app/src/main/java/com/byga/flowmanager/MainActivity.kt
app/src/main/java/com/byga/flowmanager/data/EvidenceExport.kt
app/src/main/java/com/byga/flowmanager/data/PocDatabase.kt
app/src/main/java/com/byga/flowmanager/data/PocPreferences.kt
app/src/main/java/com/byga/flowmanager/data/PocRepository.kt
app/src/main/java/com/byga/flowmanager/domain/PocModel.kt
app/src/main/java/com/byga/flowmanager/launcher/FlowLauncher.kt
app/src/main/java/com/byga/flowmanager/security/MetadataCipher.kt
app/src/main/java/com/byga/flowmanager/ui/BygaTheme.kt
app/src/main/java/com/byga/flowmanager/ui/PocScreen.kt
app/src/main/java/com/byga/flowmanager/ui/PocViewModel.kt
app/src/main/res/drawable/ic_flow.xml
app/src/main/res/drawable/icon_background.xml
app/src/main/res/mipmap-anydpi/ic_launcher.xml
app/src/main/res/values/strings.xml
app/src/main/res/values/themes.xml
app/src/main/res/xml/backup_rules.xml
app/src/main/res/xml/data_extraction_rules.xml
app/src/test/java/com/byga/flowmanager/IdentityRulesTest.kt
app/src/test/java/com/byga/flowmanager/MetadataCipherTest.kt
build.gradle.kts
docs/acceptance-checklist.md
docs/architecture.md
docs/build-evidence/ci-checks.txt
docs/build-evidence/local-checks.txt
docs/delivery-report.md
docs/feasibility-gate.json
docs/poc-results.md
docs/poc-test-plan.md
docs/security.md
docs/testing.md
gradle.properties
gradle/wrapper/gradle-wrapper.jar
gradle/wrapper/gradle-wrapper.properties
gradlew
gradlew.bat
scripts/check_feasibility_gate.py
settings.gradle.kts
```
