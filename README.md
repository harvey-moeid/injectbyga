# BYGA Flow Manager

Aplikasi Android lokal untuk mengelola banyak akun dan membuka Google Flow melalui **satu browser pilihan**. Pengguna **memilih akun aktif secara manual di Flow**. Ini keputusan terbaru pengguna; lihat [scope yang disetujui](docs/scope-manual-switching.md).

BYGA menyimpan metadata, bukan sesi Google. Memilih kartu tidak menjamin Flow sudah memakai akun tersebut. Tidak ada password, OTP, cookie/token Google, login WebView, root, atau backend.

## Fitur versi 0.2.0-manual

- Dashboard dengan avatar inisial/warna, alias/email, catatan proyek, grup, favorit, pencarian, pengurutan nama/email/terakhir/favorit, list/grid dan aksi massal pindah/hapus.
- Tambah manual; pemilih akun Google Android berbasis persetujuan (metadata saja); impor CSV/TXT dengan preview, validasi, duplicate detection dan laporan penolakan. Batas 2 MB/5.000 baris per impor.
- Satu browser terpilih berlaku untuk semua akun. Target dan instruksi pemilihan manual selalu ditampilkan sebelum Flow dibuka. Tidak ada fallback browser diam-diam.
- Riwayat dispatch dan konfirmasi identitas manual; email aktual yang berbeda menjadi MISMATCH. Tidak menganggap konfirmasi historis sebagai sesi sekarang.
- Room untuk metadata/history, DataStore untuk preferensi, Android Keystore AES-GCM untuk email/alias/catatan/nama grup dan snapshot identitas.
- Kunci dashboard opsional memakai biometrik atau PIN/pola/password perangkat, tanpa PIN Google di BYGA. Aktif setelah autentikasi Android dan mengunci kembali saat aplikasi ditinggalkan.
- Ekspor CSV email/alias/grup melalui pemilih lokasi Android. File ini mengandung metadata pribadi dalam plaintext dan tidak mencakup favorit/catatan/warna/history.
- Lab PoC lama tetap ada di Pengaturan. Metadata/riwayat PoC tidak dihapus ketika upgrade; tersedia preview penyalinan akun PoC ke dashboard.

## Pasang dan mulai

Android minimum 12/API 31. Pasang browser kompatibel dari sumber resmi, lalu login ke akun Google/Flow melalui browser tersebut. BYGA tidak melakukan login Google.

1. Unduh artifact **BYGA-Flow-Manager-Debug-APK** dari run **Android CI** yang berhasil pada [GitHub Actions](https://github.com/harvey-moeid/injectbyga/actions). Ekstrak ZIP dan pasang `app-debug.apk` melalui UI Android. Debug APK adalah build uji, bukan publikasi produksi.
2. Buka Pengaturan BYGA dan pilih browser yang akan dipakai semua akun.
3. Tambah metadata akun melalui tombol Tambah akun atau impor. Jika memperbarui PoC, gunakan Pengaturan → Preview salin akun PoC lama.
4. Pilih kartu akun, baca target email dan tekan Buka Flow. **Pilih akun tersebut secara manual di Flow** dan periksa email aktif sebelum membuat proyek.
5. Kembali ke BYGA untuk mencatat email yang benar-benar terlihat atau permintaan login/expiry. Konfirmasi bukan verifikasi otomatis.

Apabila signature debug dari runner berbeda, Android mungkin meminta uninstall sebelum memasang build lain. Uninstall menghapus metadata BYGA; ekspor daftar terlebih dahulu jika diperlukan. Akun Google/sesi browser tetap dimiliki Google/browser. Satu browser dapat meminta login ulang sesuai kebijakan Google; BYGA tidak menjanjikan persistensi sesi atau generasi video background.

## Impor dan ekspor

CSV UTF-8 dengan header (alias/group opsional):

```csv
email,alias,group
akun1@gmail.com,Flow Utama,Konten
akun2@gmail.com,Flow Eksperimen,Eksperimen
```

TXT UTF-8: satu email per baris. Baris invalid/duplikat dilaporkan dalam preview tanpa menampilkan isi baris ditolak. Header tak dikenal, termasuk kolom password/OTP/token/cookie, menolak seluruh file. Impor tidak menimpa akun yang sudah ada. CSV mendukung kutipan, koma dan escaped quotes; formula spreadsheet pada ekspor dinetralkan dengan awalan apostrof (awalan literal dapat terbawa jika diimpor ulang).

Penghapusan akun hanya menghapus metadata dashboard dan riwayat terkait. Penghapusan grup memindahkan anggotanya menjadi tanpa grup. Data PoC lama, akun Google, sesi browser dan file ekspor yang sudah dibuat tidak ikut dihapus.

## Build

Gunakan JDK 17 lengkap, Android SDK platform 35 dan build-tools 35.0.0; minimum runtime Android 31. Atur ANDROID_HOME atau local.properties dengan sdk.dir (jangan commit local.properties).

```bash
./gradlew --no-daemon --stacktrace help lintDebug testDebugUnitTest assembleDebug assembleDebugAndroidTest
python3 scripts/check_feasibility_gate.py
```

Windows gunakan gradlew.bat. APK lokal: `app/build/outputs/apk/debug/app-debug.apk`. Untuk perangkat/emulator pengembangan:

```bash
./gradlew connectedDebugAndroidTest
```

Tes otomatis memakai metadata sintetik dan tidak membuktikan login Google. Hasil aktual dan artifact: [docs/testing.md](docs/testing.md).

## CI, scope dan rilis

CI push/PR: JDK/SDK, validasi wrapper/Gradle, lint (error fatal; warning terlihat), unit test, build APK, upload artifact, serta instrumentation pada emulator API 31. Signing candidate manual membutuhkan GitHub Secrets BYGA_KEYSTORE_BASE64, BYGA_KEYSTORE_PASSWORD, BYGA_KEY_ALIAS, BYGA_KEY_PASSWORD dan gate produksi yang ditinjau. Tidak ada keystore/password di repository, merge otomatis atau publikasi produksi.

Gate **isolasi otomatis tetap UNVERIFIED**. Persetujuan satu-browser/manual membuka pengembangan organizer; tidak menyatakan kebutuhan isolasi baseline telah terpenuhi. Workflow produksi tetap diblokir sebelum GO/review. PRD baseline dan bukti Phase 0 dipertahankan sebagai arsip.

- [PRD dan addendum](PRD.md)
- [Scope satu browser yang disetujui](docs/scope-manual-switching.md)
- [Arsitektur](docs/architecture.md)
- [Keamanan](docs/security.md)
- [Testing](docs/testing.md)
- [Acceptance](docs/acceptance-checklist.md)
- [Laporan implementasi](docs/delivery-report.md)
- [Hasil isolasi/PoC](docs/poc-results.md)
