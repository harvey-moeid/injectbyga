# Rencana uji PoC di perangkat nyata

Semua pemeriksaan identitas dalam dokumen ini **belum dijalankan** di Codex Cloud. Unit test, lint, emulator tanpa akun, dan build APK bukan bukti login atau isolasi.

## Persiapan

- HP Android 12+ dengan Google Play, RAM minimal sesuai persyaratan Flow. Minimal satu HP entry-level dan satu pembanding untuk evaluasi lanjutan PRD.
- Tiga akun Google A/B/C milik tester yang memenuhi akses Flow (umur/wilayah/langganan sesuai persyaratan Google). Jangan menaruh password, OTP, cookie, atau token pada BYGA, repository, laporan, atau log.
- Flow resmi dari tautan Play dalam PRD, Chrome dan minimal satu browser alternatif jika tersedia. Admin menyediakan Work Profile bila ingin diuji; bukan ketergantungan pengguna.
- Catat model, versi/build Android, versi BYGA, versi Flow dan semua browser, profil pribadi/kerja, pengaturan browser default, serta hash APK.
- Perbarui `docs/poc-results.md` dengan tanggal dan ID bukti. Video/screenshot tersamarkan harus memperlihatkan identitas aktif sebelum koreksi; simpan bukti identitas lengkap secara privat bila diperlukan untuk audit.

## Prosedur tiap rute

1. Uji **native terlebih dahulu**. Atur email A/B/C dan rute Aplikasi Flow. Login awal sah di Flow diperbolehkan. BYGA hanya membuka launcher activity, tanpa kontrak pemilih akun.
2. Mulai seri baru pada skenario yang dipilih. Jangan mengganti run ID ketika menguji persistensi setelah restart.
3. Ketuk Buka Flow pada slot yang diminta dan baca peringatan. Setelah terbuka, buka menu profil Flow; periksa email aktif **sebelum** melakukan pergantian akun manual. Jangan menganggap avatar atau akun browser sama dengan akun aktif Flow.
4. Kembali ke BYGA, ketuk Catat identitas yang terlihat. Ketik email aktual, termasuk ketika salah. Salah email harus menjadi MISMATCH. Login prompt tanpa identitas bukan PASS. Jika tak dapat diperiksa, pilih UNKNOWN.
5. Simpan screenshot/video dan catat setiap intervensi manual. Memperbaiki akun setelah salah tidak mengubah percobaan gagal menjadi PASS.
6. Ulangi memakai Custom Tabs provider yang sama, browser eksplisit yang sama, lalu browser berbeda per slot bila terpasang. Dokumentasikan pemetaan A/B/C ke browser/profil. Browser berbeda hanya hipotesis isolasi; harus mengulang semua matriks.
7. Untuk Work Profile, admin memasang salinan BYGA + browser/Flow dalam profil kerja yang sudah tersedia. Buka salinan BYGA berbadge kerja dan catat profil WORK. Ekspor masing-masing profil. Tidak ada pembuatan profil atau transfer bukti otomatis. Jika profil tidak tersedia, tulis BLOCKED, jangan FAIL.

## Matriks wajib (ulangi setiap pemetaan rute/provider)

| ID | Skenario | Langkah dan kriteria |
|---|---|---|
| T01 / POC-01,02 | A → B → C | Periksa dan catat setiap identitas; semuanya harus sesuai, tanpa koreksi akun antar pembukaan setelah setup. |
| T02 | C → A → B | Urutan balik; sesi A tetap benar setelah B/C. |
| T03 / POC-03 | 20 perpindahan | Minimal 20 pergantian berurutan, disarankan 20 siklus A/B/C (60 pembukaan). Nol mismatch, UNKNOWN, atau koreksi manual. Hitung dari JSON, bukan dari ingatan. |
| T04 / POC-04 | Restart BYGA | Catat baseline, paksa berhenti dari Pengaturan Android, buka BYGA. Metadata/config/run/history tetap; buka A/B/C kembali dan periksa identitas baru. |
| T05 / POC-05 | Restart Android | Restart HP melalui UI sistem; tunggu unlock/profile aktif; ulang A/B/C. Riwayat lama tidak cukup. |
| T06 / POC-06 | Sesi habis | Gunakan mekanisme keamanan resmi/kejadian expiry nyata pada satu akun; bedakan logout sengaja dari expiry aktual. Verifikasi akun lain tetap benar. Tanpa pesan/sinyal expiry yang jelas hasil expiry UNVERIFIED. |
| T07 | Akun berbeda / salah identitas | Coba B ketika provider sebelumnya memakai A; jika A tetap tampil, simpan A dan FAIL. Tidak boleh mengubah ke B sebelum observasi. |
| T08 / POC-07 | Offline | Aktifkan mode pesawat lewat UI Android. Metadata tetap terbaca. Intent bisa tetap terkirim; periksa pesan jaringan di Flow/browser dan catat UNKNOWN/temuan terpisah. Dispatch tidak membuktikan jaringan berhasil. |
| T09 / POC-08,09 | Native + browser/CCT | Catat dukungan provider aktual, versi, sharing sesi, re-login, serta konsistensi pemilihan akun. Tidak ada private extra/authuser yang ditebak. |
| T10 / POC-10 | Work Profile | Catat jumlah profil yang benar-benar tersedia; bandingkan pribadi dan kerja; apakah A/B/C dapat diisolasi tetap harus diuji. |

## Bukti dan keputusan

Ekspor JSON lewat Storage Access Framework di Riwayat; laporan sengaja tidak mencantumkan email. Kaitkan slot pseudonim dengan bukti privat yang diperiksa reviewer. Isi tabel hasil, versi, hash, run IDs, jumlah pembukaan/mismatch, file bukti, catatan intervensi, dan identitas tester/reviewer (secukupnya tanpa PII publik). Laporkan PASS, FAIL, UNVERIFIED, BLOCKED per skenario **dan rute**, jangan mencampur bukti beberapa konfigurasi.

- **PASS:** hasil nyata sesuai kriteria; sertakan bukti. Satu match manual hanya PASS untuk observasi tersebut.
- **FAIL:** hasil nyata melanggar kriteria, misalnya B membuka A.
- **UNVERIFIED:** belum diuji / bukti tidak lengkap / identitas tidak terlihat.
- **BLOCKED:** mekanisme/perangkat/provider/prasyarat tidak tersedia.

GO memerlukan metode sah dengan A/B/C persisten, urutan dan restart/expiry teruji, nol salah identitas, serta bukti ditinjau. CONDITIONAL GO harus menjelaskan batas akun/profil yang nyata dan tidak mengizinkan full scope otomatis. NO-GO memerlukan bukti kegagalan/ketidaktersediaan yang cukup; ketiadaan perangkat hanya UNVERIFIED. Jangan mengubah keputusan #1–#7 tanpa persetujuan perubahan produk.
