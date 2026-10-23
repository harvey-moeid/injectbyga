# Checklist penerimaan

## Phase 0 engineering

- [x] PRD dibaca dan disalin utuh; keputusan #1–#7 dipertahankan.
- [x] Kotlin + Compose Material 3, min API 31, package `com.byga.flowmanager`.
- [x] Tiga slot A/B/C, target email, konfigurasi rute/provider dan Buka Flow.
- [x] Native, Custom Tabs, browser eksplisit, serta guard profil kerja.
- [x] Peringatan kemampuan sebelum launch; tidak ada fallback otomatis yang mengubah rute.
- [x] Identity confirmation manual dengan input email aktual dan MISMATCH bila salah.
- [x] Tidak ada klaim verifikasi otomatis atau isolasi palsu.
- [x] Room history/config, DataStore preferensi/seri, Keystore untuk enkripsi email.
- [x] Rencana pengujian dan laporan UNVERIFIED/BLOCKED yang jujur.
- [x] Ekspor JSON tanpa email melalui pemilih lokasi Android.
- [x] Workflow push/PR untuk lint, tests, debug APK artifact.
- [x] Workflow signing manual diblokir sebelum GO; tanpa publikasi otomatis.

## Bukti wajib sebelum GO (belum terpenuhi)

- [ ] A → B → C dan C → A → B, identitas aktual benar semua.
- [ ] ≥20 perpindahan berurutan tanpa salah akun/koreksi manual.
- [ ] Sesi independen dan persistensi setelah restart BYGA/Android.
- [ ] Expiry satu akun tidak mengganggu lainnya.
- [ ] Bukti layar/video dan JSON dikaitkan dengan versi perangkat/provider.
- [ ] Hasil per rute ditinjau; GO penuh atau batas CONDITIONAL GO dicatat.
- [ ] Uji aksesibilitas/font besar/responsivitas/performa pada HP entry-level.

## Full application (ditunda sesuai gate)

- [ ] Dashboard 50 akun, avatar/alias, grup/favorit/search/sort/list-grid.
- [ ] Manual add / Android account picker / preview CSV dan TXT dengan validasi.
- [ ] Workspace/restore yang sesuai kemampuan terbukti dan re-auth resmi.
- [ ] Pengujian acceptance PRD §15 dan rilis setelah GO.

Status build, unit test, lint, instrumentation, dan artifact aktual: lihat `testing.md`. Checklist kode tersedia bukan klaim fitur sudah diuji pada Android atau memenuhi scope penuh.
