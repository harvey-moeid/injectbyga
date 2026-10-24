# Scope yang berlaku: satu browser, akun dipilih manual di Flow

Keputusan pengguna terbaru: **“satu browser saja”**. Ini mengikuti penjelasan bahwa satu browser memerlukan pergantian akun manual di Flow. Pengguna sebelumnya juga memilih “pergantian akun manual di Flow”; pilihan banyak browser di antara keduanya telah digantikan. Pengembangan organizer/dashboard lengkap diizinkan dalam scope ini.

1. BYGA menyimpan metadata akun lokal: email, alias, grup, favorit, catatan proyek dan riwayat.
2. Satu browser dipilih pada Pengaturan untuk seluruh akun. Tidak ada pemetaan satu browser per akun pada produk ini.
3. Memilih akun menampilkan target email dan peringatan, membuka Flow pada browser pilihan, lalu pengguna memilih akun yang benar di Flow sebelum membuat proyek.
4. Setelah kembali, pengguna dapat mencatat email yang benar-benar terlihat. Hasilnya historis per pembukaan; sesi saat ini selalu belum diketahui BYGA.
5. Tambah manual, pemilih akun Android berbasis persetujuan, impor CSV/TXT dengan preview/validasi/deduplikasi, grup, favorit, pencarian, sorting, list/grid, edit/hapus dan riwayat dibangun dalam scope baru.
6. BYGA tidak membaca/menyalin/menyimpan password, OTP, cookie atau token Google, tidak mengakses browser private data, dan tidak memakai login WebView. Semua login terjadi pada browser/Google resmi.

## Perubahan dari baseline

Keputusan #1 Android APK, #3 seluruh metode metadata, dan #7 dashboard tetap berlaku. #2 sesi independen dan #6 workspace independen diganti dengan pengelolaan metadata dan pergantian akun manual. #4 menggunakan launcher browser resmi tanpa jaminan fullscreen/target akun otomatis; #5 tetap melarang root/ADB/Shizuku/bypass, tanpa isolation engine untuk versi ini.

FR-005 diterima bila target ditampilkan dan browser pilihan dibuka dengan instruksi pemilihan akun manual; dispatch tidak boleh mengklaim identitas berhasil. FR-006 isolasi ditunda, bukan dipenuhi. Pemulihan proyek/halaman private dan progres video background tidak dijanjikan. Pemulihan workspace lokal hanya preferensi tampilan/filter.

## Acceptance versi manual

- Metadata 50 akun dapat dikelola, disimpan dan difilter secara lokal tanpa jaringan; performa HP tetap diukur manual.
- Impor defensif, validasi, duplicate detection, preview dan laporan penolakan bekerja tanpa kolom rahasia.
- Browser yang sama dipakai semua akun; browser hilang tidak dialihkan diam-diam.
- Target akun jelas sebelum launch; actual Flow account bisa berbeda. Konfirmasi cocok/salah hanya berdasarkan laporan pengguna, bukan verifikasi otomatis.
- Riwayat, preferensi dan metadata bertahan pada storage lokal; hapus akun hanya menghapus data BYGA.
- Database PoC lama dan bukti historis dipertahankan terpisah. Pengguna dapat menyalin metadata slot PoC secara eksplisit, tanpa menyatakan akun sudah login.
- Hasil isolasi lama tetap UNVERIFIED; persetujuan scope tidak mengubahnya menjadi GO. Release produksi masih memerlukan review dan gate terpisah; tidak ada publikasi otomatis.
