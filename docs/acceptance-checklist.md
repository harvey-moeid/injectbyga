# Acceptance versi satu browser/manual

Keputusan produk aktif: scope-manual-switching.md dan addendum PRD. Checklist implementasi tidak menyatakan seluruh tes HP telah dijalankan.

## Diimplementasikan

- [x] Dashboard metadata banyak akun, avatar inisial/warna, email/alias/catatan, grup dan favorit.
- [x] Search, sorting nama/email/terakhir/favorit, filter grup/favorit, list/grid tersimpan.
- [x] Tambah/edit/hapus akun; aksi massal pindah grup/hapus dengan konfirmasi hapus.
- [x] Group create/rename/delete; hapus grup menjaga akun tetap ada.
- [x] Android account chooser resmi, metadata saja, pembatalan/fallback tambah manual.
- [x] Import CSV/TXT via SAF, UTF-8 ketat, preview, validation, duplicate detection/recheck dan report penolakan.
- [x] Satu browser global, target dan instruksi pemilihan akun manual sebelum setiap launch, tanpa fallback diam-diam.
- [x] Riwayat launch dan observasi manual; mismatch tidak ditampilkan sebagai cocok; konfirmasi lama tidak dianggap sesi sekarang.
- [x] Room, DataStore dan Keystore, enkripsi metadata sensitif; backup dikecualikan.
- [x] Kunci dashboard/Lab opsional dengan credential Android/biometrik, tanpa menyimpan PIN sendiri.
- [x] Ekspor CSV email/alias/grup dengan peringatan metadata plaintext dan formula neutralization.
- [x] PoC lama dipertahankan; salin metadata melalui preview eksplisit.
- [x] Workflow CI/debug artifact, signing secrets manual, tidak merge/publish otomatis.

## Bukti engineering

Lihat testing.md untuk hasil build, 22 JVM unit tests dan instrumentation yang benar-benar dijalankan. Jumlah tes yang tersedia bukan klaim semua tes telah berjalan.

- [x]22 unit tests lulus (0 failures/errors/skipped).
- [x]11 instrumentation tests dijalankan dan lulus pada emulator API31, run40047505432.
- [x]Debug APK berhasil diunggah sebagai BYGA-Flow-Manager-Debug-APK.

## Wajib pada HP sebelum menyebut versi manual siap produksi

- [ ] Flow/browser aktual, pemilih akun perangkat dan pemilihan akun manual benar.
- [ ] Force-stop/reboot/upgrade signature sama mempertahankan metadata.
- [ ] Biometrik/PIN, background-return, rotasi/proses mati dan OEM restrictions.
- [ ] Offline/import/export SAF, font besar, aksesibilitas dan performa50 akun pada entry-level.
- [ ] Review keamanan dan ketentuan rilis; produksi tetap diblokir.

## Isolasi baseline

- [ ] Sesi A/B/C independen dan auto target account: UNVERIFIED, bukan kemampuan versi manual.
- [ ]20 perpindahan/reboot/expiry tanpa intervensi: belum dibuktikan untuk isolasi.
- [ ] GO isolasi: tidak diberikan oleh persetujuan scope atau hasil unit/emulator.
