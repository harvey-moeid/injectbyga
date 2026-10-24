# BYGA Flow Manager

Baca seluruh `PRD.md` sebelum mengubah produk. Addendum satu browser di awal PRD dan `docs/scope-manual-switching.md` mencatat keputusan terbaru yang mengatasi baseline.

- Fase saat ini: **Organizer dengan satu browser dan pergantian akun manual di Flow, disetujui pengguna**. Fitur dashboard/metadata lengkap diizinkan. Isolasi sesi tetap UNVERIFIED; jangan mengklaim GO.
- Jangan menyamakan build, dispatch Intent, unit test, atau satu konfirmasi manual dengan isolasi akun yang terbukti.
- Gunakan Kotlin, Compose Material 3, MVVM, Room, DataStore, Android Keystore; minSdk 31; UI Bahasa Indonesia.
- Jangan gunakan login WebView, root, Shizuku, ketergantungan ADB pengguna, cookie/token/password Google, private Intent, atau Accessibility automation.
- Semua verifikasi identitas PoC berasal dari tester; jangan menambahkan status terverifikasi otomatis tanpa API resmi dan bukti.
- Jangan mencatat email/credential di log, artifact CI, atau ekspor diagnostik. Data lokal BYGA tidak boleh memengaruhi akun Google.
- Jalankan `./gradlew --no-daemon help lintDebug testDebugUnitTest assembleDebug assembleDebugAndroidTest` dan `python3 scripts/check_feasibility_gate.py` bila lingkungan mendukung. Laporkan blocker dan hasil aktual.
- APK produksi tidak boleh dirilis sebelum GO. Jangan merge otomatis. Signing hanya melalui GitHub Secrets.
- Pertahankan database/riwayat PoC sebagai arsip terpisah saat upgrade. Penyalinan metadata PoC ke dashboard memerlukan tindakan pengguna.
- Browser pilihan berlaku untuk semua akun. Target selalu ditampilkan sebelum launch; tidak ada klaim akun aktif otomatis atau fallback tersembunyi.
