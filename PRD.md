# ADDENDUM v1.1 — SATU BROWSER, PILIH AKUN MANUAL DI FLOW

Keputusan pengguna terbaru pada 24 Oktober 2026: **“satu browser saja”**, setelah pilihan satu browser dengan pergantian manual dijelaskan. Ini menetapkan scope organizer/dashboard lengkap: semua akun memakai satu browser pilihan, sementara akun aktif dipilih dan diperiksa pengguna di Flow. Pilihan browser berbeda per akun sebelumnya telah digantikan.

Addendum ini menggantikan kewajiban isolasi otomatis pada keputusan #2/#6 dan membuka pembangunan fitur metadata/dashboard tanpa GO isolasi. Tidak ada klaim BYGA mengendalikan sesi Google, login otomatis ke akun tertentu, atau mempertahankan sesi independen. Ketentuan lengkap: [docs/scope-manual-switching.md](docs/scope-manual-switching.md). Addendum mendahului baseline yang bertentangan; baseline asli dipertahankan di [docs/prd-baseline-v1.md](docs/prd-baseline-v1.md).

**Gate isolasi tetap UNVERIFIED. Persetujuan organizer manual bukan hasil pengujian isolasi atau izin publikasi produksi.**

---

# PRODUCT REQUIREMENTS DOCUMENT (PRD)
# BYGA FLOW MANAGER — Android Multi-Account Launcher

| Metadata | Detail |
|---|---|
| Nama produk | BYGA Flow Manager |
| Dokumen | Product Requirements Document |
| Versi | 1.0 — Baseline keputusan #1–#7 |
| Tanggal | 24 Oktober 2026 (WIB) |
| Platform | Android APK |
| Status | Siap untuk desain teknis dan Proof of Concept (PoC); **belum lolos feasibility gate** |
| Sasaran | Pengguna yang memiliki banyak akun Google dan ingin mengakses Google Flow melalui pemilih akun terpusat |
| Bahasa produk | Bahasa Indonesia (default) |
| Prinsip utama | Pemilihan akun yang tepat, keamanan autentikasi, dan kejelasan status sesungguhnya |

> **PERINGATAN KELAYAKAN:** Target utama produk adalah membuka Google Flow dengan sesi berbeda untuk masing-masing akun tanpa login berulang selama sesi valid. Kemampuan ini **belum dapat dijanjikan di Android**. BYGA Flow Manager tidak memiliki kontrol atas cookie, autentikasi, atau pergantian akun milik browser maupun aplikasi Google Flow resmi. Pengembangan fitur lengkap hanya dilanjutkan setelah PoC membuktikan isolasi akun dengan metode yang sah dan kompatibel. Jangan mengganti kebutuhan ini secara diam-diam dengan launcher yang selalu membuka akun terakhir.

---

## 1. Ringkasan Eksekutif

BYGA Flow Manager adalah aplikasi Android yang menyediakan satu dashboard untuk mengelola daftar akun Google yang digunakan mengakses Google Flow. Pengguna menambah akun melalui metode manual, memilih akun yang tersedia secara sah pada Android, atau mengimpor daftar email CSV/TXT. Setiap akun memiliki identitas, alias, kelompok, favorit, riwayat penggunaan, dan status sesi yang dilaporkan secara jujur.

**Alur produk yang ditargetkan:**

1. Buka BYGA Flow Manager.
2. Pilih kartu akun, misalnya **Flow Konten**.
3. Sistem menentukan jalur pembukaan yang telah terbukti kompatibel.
4. Google Flow terbuka **menggunakan akun yang benar**, tanpa meminta login lagi bila sesi akun tersebut masih valid.
5. Pengguna kembali ke dashboard, lalu memilih akun lain.

Jika sistem tidak bisa menjamin bahwa akun yang dipilih akan terbuka, aplikasi tidak boleh menampilkan status “Berhasil masuk akun X” hanya karena tautan Flow sudah dibuka.

## 2. Keputusan Pengguna yang Sudah Disetujui

| # | Pilihan | Keputusan final | Implikasi |
|---|---|---|---|
| 1 | **A** | Android APK | Produk utama aplikasi native Android; bukan website/Windows. |
| 2 | **B** | Sesi terpisah per akun | Target login persisten dan akun A/B/C tidak saling menimpa; wajib dibuktikan dengan PoC. |
| 3 | **D** | Semua metode penambahan akun | Manual, pemilih akun Android yang diizinkan, dan impor CSV/TXT. |
| 4 | **D** | Smart Flow Launcher | Prioritaskan pengalaman Flow fullscreen dalam aplikasi bila kompatibel; jalur browser lain bisa digunakan tanpa melanggar isolasi. |
| 5 | **D** | Smart Isolation Engine | Prioritas tanpa root, ADB, atau Shizuku; Work Profile opsional; tidak bypass keamanan Google. |
| 6 | **D** | Smart Multi-Account Workspace | Sesi independen, pemulihan lokasi terakhir bila memungkinkan, perpindahan cepat, dan indikator status yang akurat. |
| 7 | **D** | Smart Account Dashboard | Kartu akun, pencarian, folder/grup, favorit, pengurutan, indikator sesi, terakhir dipakai, dan mode list/grid. |

Keputusan di atas adalah **kebutuhan produk**. Implementasi aktual yang bergantung pada Google/Android ditandai **[POC]**. Fitur-fitur rinci lain dalam dokumen ini adalah **usulan rancangan default**, bukan keputusan pengguna tambahan.

## 3. Latar Belakang dan Masalah

Pengguna yang mengelola banyak akun Google Flow saat ini harus mengingat akun mana yang sedang aktif dan sering berpindah melalui browser/aplikasi. Risiko utamanya adalah:

- Proyek/generasi video dibuat pada akun yang salah.
- Login ulang atau pergantian akun memakan langkah berulang.
- Akun menjadi sulit ditemukan saat jumlahnya puluhan.
- Sesi antar akun dalam browser standar dapat berbagi state, sehingga pilihan akun di launcher tidak selalu menentukan identitas login Flow.
- Pengguna sulit mengetahui apakah sesi tertentu masih berlaku.

**Problem statement:** Sediakan pengelola akun yang cepat dan tertata, dengan kemampuan masuk ke akun Flow yang dipilih **hanya jika dapat diverifikasi benar-benar menggunakan akun tersebut**.

## 4. Tujuan, Hasil, dan Bukan Tujuan

### 4.1 Tujuan utama (P0)

- Mendata dan menampilkan sekurangnya 50 akun dengan UI responsif (target kapasitas UI, bukan jaminan 50 sesi persisten).
- Membuka Google Flow berdasarkan akun yang dipilih dengan rute yang diuji.
- Membuktikan isolasi sesi A/B/C tanpa pertukaran akun yang keliru.
- Memiliki penanganan aman untuk sesi kedaluwarsa dan login ulang.
- Beroperasi lokal tanpa backend wajib pada MVP.

### 4.2 Tujuan sekunder (P1)

- Memudahkan pencarian, favorit, pengelompokan, dan impor metadata akun.
- Memulihkan halaman terakhir **bila browser/aplikasi dan URL mendukungnya**.
- Mengunci dashboard menggunakan PIN/biometrik.
- Mengekspor dan mengimpor daftar akun tanpa rahasia login.

### 4.3 Bukan tujuan (out of scope)

- Membuat/mendaftarkan akun Google otomatis.
- Login dengan cara mengumpulkan password, OTP, cookie, refresh token, atau passkey milik Google.
- Membuka akses fitur/credits Flow pada akun yang tidak memenuhi syarat.
- Otomasi pembuatan konten massal, bypass kuota/rate limit, atau bypass autentikasi.
- Mengutak-atik APK resmi Google Flow, menyuntik kode ke proses browser, atau reverse-engineering mekanisme login.
- Klaim bahwa proses generasi video selalu tetap berjalan di background.

## 5. Pengguna Sasaran dan Skenario

### Persona utama

Kreator konten/AI builder yang memiliki banyak akun untuk memisahkan pekerjaan, proyek, eksperimen, dan cadangan.

### Use case inti

- **UC-01 — Tambah akun:** Pengguna mendaftarkan email dan alias; identitas Google diverifikasi melalui mekanisme autentikasi resmi bila tersedia.
- **UC-02 — Buka Flow:** Pengguna menekan kartu, sistem memilih rute pembukaan sesuai hasil PoC, dan UI menjelaskan status hasilnya.
- **UC-03 — Berpindah akun:** Pengguna kembali, memilih akun lain, dan tidak masuk ke akun sebelumnya secara tak sengaja.
- **UC-04 — Mengelola akun banyak:** Pengguna mencari, mengelompokkan, memberi favorit, mengurutkan, mengedit alias, dan menghapus metadata akun.
- **UC-05 — Sesi habis:** Pengguna mendapatkan pesan bahwa autentikasi ulang diperlukan melalui halaman/aplikasi resmi.
- **UC-06 — Impor CSV/TXT:** Pengguna mengimpor email massal, melihat preview, menghapus duplikat, lalu melakukan autentikasi per akun ketika akan digunakan.

## 6. Rancangan UX / Navigasi

### 6.1 Halaman utama — Smart Account Dashboard

- Header: logo BYGA FLOW, jumlah akun, pencarian, tombol Tambah Akun, dan menu Pengaturan.
- Chip filter: **Semua | Favorit | Terakhir Dipakai | Grup**.
- Toggle tampilan: **List/Grid**.
- Kartu: avatar/ikon, alias, email, grup, indikator status autentikasi, terakhir dipakai, ikon favorit, menu tindakan, dan tombol **Buka Flow**.
- Aksi massal: pilih beberapa kartu lalu pindahkan grup atau hapus metadata setelah konfirmasi.
- Urutkan: nama, email, terbaru dibuka, favorit.
- Empty state: “Belum ada akun — tambah atau impor akun Google.”

### 6.2 Halaman tambah akun

Tab **Manual**, **Pilih dari perangkat**, **Impor CSV/TXT**. Impor hanya menambahkan **metadata email**, bukan login otomatis. Pemilih akun harus memperoleh persetujuan pengguna dan tidak boleh menganggap daftar semua akun sistem dapat diakses tanpa izin.

### 6.3 Halaman detail akun

Alias, email, catatan opsional, grup, warna/ikon, status sesi, metode pembuka yang disetujui, terakhir dibuka, hasil verifikasi terakhir, dan opsi **Buka Flow / Login ulang / Edit / Hapus**.

### 6.4 Tampilan peluncur Flow

- Tampilkan alias akun yang **diminta** untuk dibuka.
- Jika terdeteksi rute tidak mendukung pemilihan sesi per akun, tampilkan peringatan, **jangan diam-diam membuka akun terakhir**.
- Beri tombol “Kembali ke daftar”.
- Jika verifikasi otomatis tidak tersedia, tampilkan status **“Perlu konfirmasi akun di Flow”** dan instruksi pemeriksaan manual; jangan labeli “Terverifikasi otomatis”.

### 6.5 Arah desain visual (usulan)

- Tema dark premium.
- Warna utama: midnight navy `#080D19`, electric blue `#246BFD`, cyan `#16DBEE`.
- Tipografi jelas, target sentuh minimal ~48dp, dukung ukuran font sistem.
- Animasi sederhana, prioritas performa perangkat entry-level.

## 7. Kebutuhan Fungsional (FR)

| ID | Fitur | Prioritas | Kriteria penerimaan ringkas |
|---|---|---|---|
| FR-001 | Daftar akun | P0 | Menampilkan 50 entri dengan scroll mulus pada perangkat uji. |
| FR-002 | Akun manual | P0 | Email divalidasi, alias tersimpan, duplikat email dicegah/ditangani. |
| FR-003 | Pemilih akun Android | P0 | Memakai cara resmi berbasis persetujuan pengguna; bila tidak tersedia, alasan ditampilkan. |
| FR-004 | Impor CSV/TXT | P0 | Preview, validasi alamat, deduplikasi, laporan baris ditolak; tanpa password. |
| FR-005 | Buka Google Flow | P0 **[POC]** | Identitas akun aktual sesuai kartu yang diklik; buka dengan rute yang teruji. |
| FR-006 | Sesi independen | P0 **[POC]** | Login A tidak menimpa sesi B dan sebaliknya. |
| FR-007 | Login ulang aman | P0 | Mengarahkan ke autentikasi Google resmi, tidak menerima password di BYGA. |
| FR-008 | Status sesi | P0 | Bedakan terdaftar, belum diverifikasi, terverifikasi, butuh login, dan tidak didukung. |
| FR-009 | Edit/hapus akun | P0 | Edit hanya metadata lokal; hapus tidak menghapus akun Google. |
| FR-010 | Pencarian/filter | P0 | Cari email/alias dan filter grup/favorit. |
| FR-011 | Folder/grup | P1 | Buat, ubah, hapus grup tanpa menghapus akun. |
| FR-012 | Favorit dan urutkan | P1 | Favorit/urutan tersimpan lintas restart. |
| FR-013 | Mode list/grid | P1 | Pilihan tersimpan di pengaturan. |
| FR-014 | Riwayat pembukaan | P1 | `last_opened_at`, `last_launch_method`, hasil launch dicatat lokal. |
| FR-015 | Pemulihan halaman | P1 **[POC]** | Hanya jika provider memungkinkan URL/halaman kembali dengan aman. |
| FR-016 | PIN/biometrik | P1 | Kunci dashboard tanpa mengakses kredensial Google. |
| FR-017 | Backup metadata | P1 | Ekspor/impor tanpa cookie/token/password. |
| FR-018 | Status kegiatan background | P2 **[POC]** | Tidak menampilkan klaim progres/selesai tanpa integrasi sah dan bukti aktual. |

### Definisi status akun

- `REGISTERED`: email dicatat, tidak ada bukti login Flow.
- `NEEDS_AUTH`: memerlukan login atau pilih akun di Flow.
- `UNVERIFIED`: Flow terbuka, identitas akun sebenarnya belum terbukti.
- `VERIFIED_MANUAL`: pengguna mengonfirmasi akun yang tampil; tampilkan sumber dan waktu konfirmasi.
- `VERIFIED_AUTOMATED`: hanya bila ada mekanisme verifikasi resmi dan tepercaya yang terbukti; **tidak diasumsikan tersedia**.
- `EXPIRED`: sesi terbukti berakhir melalui sinyal sah/hasil cek yang jelas.
- `UNSUPPORTED`: metode isolasi/peluncuran untuk akun ini tidak didukung pada perangkat.

**Jangan menyimpulkan `EXPIRED` hanya dari waktu yang berlalu, atau `VERIFIED_AUTOMATED` hanya karena URL Flow berhasil dibuka.**

## 8. Rancangan Teknis dan Batasan Platform

### 8.1 Penemuan penting per 24 Oktober 2026

1. **Google Flow memiliki aplikasi Android resmi**. Dokumentasi Google menyebut Android 12+ dan RAM 2GB+; aplikasi native direkomendasikan untuk kestabilan mobile. Namun tidak ditemukan jaminan bahwa aplikasi pihak ketiga dapat memilih akun aktif di Flow melalui Intent publik.
2. **Google memblokir autentikasi Google OAuth melalui embedded WebView.** Jangan memakai WebView yang bisa membaca cookie atau menyuntik JavaScript sebagai jalur login Google.
3. **Custom Tabs standar berbagi state dengan browser.** Membuka Custom Tabs yang berbeda tidak otomatis memberi cookie jar independen untuk setiap akun.
4. **Ephemeral Custom Tabs menghapus data saat ditutup.** Cocok untuk privasi temporer, bukan solusi sesi persisten banyak akun.
5. **Work Profile memisahkan lingkungan tertentu**, tetapi ketersediaan, kontrol, dan jumlah profil tergantung perangkat/pengelolaan. Ini bukan janji mendukung puluhan profil di satu HP.

### 8.2 Kandidat Smart Flow Launcher yang HARUS diuji

| Kandidat | Kegunaan | Keterbatasan/hipotesis |
|---|---|---|
| L1 — Aplikasi Google Flow resmi | Kestabilan pengalaman mobile, pembukaan aplikasi Google Flow | **Tidak boleh mengasumsikan** akun tertentu dapat dipilih lewat Intent pihak ketiga. |
| L2 — Chrome Custom Tabs | Login aman dan pengalaman browser terintegrasi | Berbagi sesi browser; isolasi akun tidak otomatis. |
| L3 — Browser eksternal terpilih | Opsi kompatibilitas apabila pengguna mengelola sesi di lingkungan browser berbeda | Membutuhkan bukti pemilihan akun konsisten; pengendalian profil browser Android terbatas. |
| L4 — Work Profile yang sudah tersedia | Pemisahan profil kerja dan pribadi bila sistem mendukung | Tidak dapat diasumsikan tersedia atau dapat dibuat bebas per akun. |
| L5 — Fullscreen internal | UX ideal sesuai keputusan #4 | **Tidak boleh** melakukan login Google di WebView yang melanggar kebijakan; hanya diimplementasikan jika ada cara yang sah. |

Aturan pemilihan rute:

- Utamakan **kebenaran akun dan keamanan**, kemudian UX fullscreen dan kecepatan.
- Jangan fallback ke rute yang **tidak dapat menjaga akun yang dipilih** tanpa menjelaskan perubahan kemampuan secara eksplisit.
- Jika tidak ada kandidat yang lolos, tandai requirement utama **blocked**, hentikan klaim “one-tap multi-account persisten”, lalu laporkan hasil PoC untuk keputusan produk lebih lanjut.

### 8.3 Arsitektur aplikasi (rancangan)

```text
Android BYGA Flow Manager (Kotlin + Jetpack Compose)
  ├── Dashboard / Search / Groups / Favorites
  ├── Account Registry
  │     ├── Manual Add
  │     ├── Android Credential / Account Picker (user consent)
  │     └── CSV/TXT Metadata Import
  ├── Session & Launch Coordinator
  │     ├── Feasibility/Capability Registry
  │     ├── Account Route Resolver
  │     ├── Launch Flow (official app / Custom Tabs / supported browser)
  │     └── Verification Status (honest, evidence-linked)
  ├── Local Persistence
  │     ├── Room (account/group/launch metadata)
  │     └── DataStore (UI preferences)
  └── Security
        ├── Android Keystore
        └── Optional PIN/BiometricPrompt
```

**Tidak diperlukan backend Cloudflare, server, D1, atau Worker pada MVP.** Backend hanya dipertimbangkan jika kelak ada kebutuhan sinkronisasi multi-perangkat yang disetujui terpisah.

### 8.4 Stack yang diusulkan

- **Bahasa:** Kotlin.
- **UI:** Jetpack Compose + Material 3.
- **Arsitektur:** MVVM / Clean modular sederhana; coroutines/Flow.
- **Penyimpanan:** Room, Preferences DataStore, Android Keystore. Proteksi metadata lokal sesuai threat model; jangan menyimpan rahasia autentikasi Flow.
- **Akses akun Android:** Credential Manager atau pemilih akun resmi yang sesuai kebutuhan, dengan persetujuan pengguna. **Token login aplikasi BYGA tidak boleh diasumsikan memberikan sesi Google Flow.**
- **Browser:** Chrome Custom Tabs / browser eksternal bila terbukti kompatibel.
- **Build:** Gradle, GitHub Actions, APK debug dan release.
- **Persyaratan rilis awal yang diusulkan:** Android 12+ agar selaras dengan aplikasi Flow native; uji pada minimal satu perangkat Android entry-level dan satu perangkat pembanding.
- **Privasi:** Tidak ada analytics pihak ketiga pada MVP; logs diagnostik tanpa PII penuh.

## 9. Rancangan Data (lokal)

### 9.1 `accounts`

| Kolom | Tipe | Keterangan |
|---|---|---|
| `id` | UUID | Kunci lokal |
| `email` | string | Normalisasi dan deduplikasi tanpa mengubah makna email Google |
| `alias` | string | Nama panggilan bebas |
| `avatar_uri` | string? | Referensi lokal opsional |
| `group_id` | UUID? | Grup akun |
| `is_favorite` | boolean | Favorit |
| `sort_order` | integer | Posisi manual |
| `status` | enum | Status autentikasi/verifikasi |
| `preferred_launch_route` | enum? | Rute yang lolos PoC untuk lingkungan tersebut |
| `last_opened_at` | datetime? | Aktivitas launcher, **bukan** aktivitas akun Google |
| `last_verified_at` | datetime? | Waktu verifikasi jika tersedia |
| `verification_source` | enum? | `MANUAL` / `TRUSTED_OFFICIAL` / none |
| `created_at`, `updated_at` | datetime | Audit lokal |

### 9.2 `account_groups`

`id`, `name`, `sort_order`, `created_at`, `updated_at`.

### 9.3 `launch_history`

`id`, `account_id`, `route`, `started_at`, `outcome` (`OPENED`, `FAILED`, `UNVERIFIED`, dll.), `error_code` yang aman dan tanpa token, `user_confirmed_identity` (opsional).

### 9.4 `app_settings`

`theme`, `display_mode`, `sort_mode`, `lock_enabled`, `last_selected_group`.

**Tidak ada tabel** `passwords`, `cookies`, `oauth_refresh_tokens`, atau `google_sessions`. Browser/Google tetap pemilik kredensial dan sesi autentikasinya.

## 10. Spesifikasi Impor / Ekspor

**CSV yang didukung (contoh):**

```csv
email,alias,group
akun1@gmail.com,Flow Utama,Konten
akun2@gmail.com,Flow Eksperimen,Eksperimen
```

**TXT yang didukung:** satu email per baris. Alias default dapat diturunkan dari bagian alamat email, lalu diedit pengguna.

**Validasi:** parser defensif, UTF-8, batas ukuran impor wajar, hindari formula injection ketika mengekspor CSV, abaikan baris kosong, tangani duplikat tanpa menghapus data yang sudah ada. Ekspor hanya metadata non-rahasia. **Jangan dukung file dengan kolom password atau OTP.**

## 11. Keamanan dan Privasi

- Aplikasi tidak meminta atau menyimpan password, OTP, recovery code, cookie autentikasi, passkey, atau token Google Flow.
- Login ke Google dilakukan hanya dalam lingkungan autentikasi resmi yang diizinkan.
- Jangan memakai Accessibility Service untuk memaksa pergantian akun/login, scraping UI kredensial, atau fitur mirip keylogger.
- Jangan menyuntik JavaScript ke halaman login Google atau mengakses state private browser.
- Penghapusan akun pada dashboard hanya menghapus **metadata BYGA**, bukan menghapus Google Account atau menjamin logout dari Flow.
- Lindungi ekspor file dengan Android Storage Access Framework; gunakan PIN/biometrik untuk membuka aplikasi bila diaktifkan.
- Gunakan backup Android secara hati-hati (hindari penyertaan data sensitif; kebijakan backup dibuat eksplisit).
- Telemetri diagnostik harus opt-in jika kelak ditambahkan dan menyamarkan alamat email secara default.
- Ikuti kebijakan Google, persyaratan layanan Flow, serta keamanan aplikasi Android yang berlaku.

## 12. Non-Functional Requirements (NFR)

| ID | Kebutuhan | Target / cara ukur |
|---|---|---|
| NFR-01 | Respons dashboard | Respons interaksi lokal terasa instan pada 50 akun; ukur pada perangkat uji. |
| NFR-02 | Stabilitas | Tidak force close dalam skenario pengujian impor, filter, dan perpindahan launcher. |
| NFR-03 | Ketepatan akun | **0 kasus akun salah** pada pengujian acceptance. |
| NFR-04 | Persistensi metadata | Akun/grup/favorit tetap ada setelah restart aplikasi/perangkat. |
| NFR-05 | Offline | Daftar akun dapat dibuka dan diedit offline; akses Flow memerlukan koneksi. |
| NFR-06 | Keamanan | Tidak ada kredensial/cookie Google di database BYGA atau log. |
| NFR-07 | Aksesibilitas | Kontras memadai, label screen reader, target sentuh ~48dp. |
| NFR-08 | Transparansi | Semua status akun dan keterbatasan launcher dijelaskan tanpa klaim palsu. |

## 13. PoC Wajib — Gate Sebelum Pengembangan Penuh

### 13.1 Sasaran PoC

Membuktikan secara nyata bahwa pengguna dapat memilih akun pada BYGA dan masuk ke **akun Flow yang benar**, dengan sesi A/B/C tetap terpisah. Karena aplikasi Flow resmi telah tersedia di Android, **uji jalur native app terlebih dahulu**, diikuti browser resmi dan profil bila diperlukan.

### 13.2 Perangkat / prasyarat

- Minimal 1 ponsel Android 12+ dengan aplikasi Google Flow resmi terpasang.
- Minimal 3 akun Google milik pengguna yang sah dan dapat mengakses Flow.
- Chrome terbaru atau browser kompatibel yang akan diuji.
- Koneksi internet yang stabil.
- Bukti layar nama/email akun aktif Flow diperiksa oleh tester; tidak melalui pengambilan cookie atau kredensial.

### 13.3 Matriks uji

| ID | Skenario | Lulus bila |
|---|---|---|
| POC-01 | Login akun A kemudian B | Kedua identitas dapat dibuka sesuai yang dipilih. |
| POC-02 | Kembali ke A setelah B | A masih masuk sebagai A tanpa memengaruhi B. |
| POC-03 | Perpindahan A→B→C sebanyak 20 kali | Tidak ada satu pun identitas akun tertukar. |
| POC-04 | Tutup paksa BYGA lalu buka ulang | Sesi persisten tetap benar bila disyaratkan. |
| POC-05 | Restart perangkat | Akun tetap dapat dipilih dengan hasil yang benar, selama sesi masih valid. |
| POC-06 | Sesi salah satu akun kedaluwarsa | Hanya akun terkait meminta autentikasi ulang; akun lain tidak terganggu. |
| POC-07 | Tanpa jaringan | Dashboard tetap bekerja; peluncuran Flow gagal dengan pesan yang jelas. |
| POC-08 | Tampilkan native Flow | Uji apakah Intent resmi benar-benar dapat memilih identitas akun tertentu. |
| POC-09 | Browser/Custom Tabs | Catat apakah ada kebocoran pemilihan sesi lewat shared cookies. |
| POC-10 | Work Profile (jika tersedia) | Buktikan isolasi nyata dan batas profil pada perangkat. |

**Catatan:** Jika mekanisme tidak memberi pengamatan identitas otomatis, tester wajib mengonfirmasi identitas yang benar-benar ditampilkan di Flow. Hasil tes ditulis apa adanya.

### 13.4 Keputusan Go / No-Go

- **GO — Full scope:** suatu metode sah terbukti mendukung perpindahan A/B/C, sesi independen, dan ketepatan identitas; lanjut membangun dashboard lengkap.
- **CONDITIONAL GO:** hanya sejumlah akun/profil tertentu dapat diisolasi; dokumentasikan batas nyata dan minta persetujuan perubahan cakupan produk sebelum melanjutkan.
- **NO-GO pada requirement #2:** tidak ada metode sah yang mendukung isolasi persisten; hentikan pengembangan fitur “satu klik akun independen”. Aplikasi hanya dapat dilanjutkan sebagai organizer/launcher biasa **dengan persetujuan baru**, bukan sebagai pemenuhan requirement awal.

**Definisi selesai PoC:** video/screenshot uji, matriks hasil, metode yang dicoba, versi Android/browser/Flow, catatan kesalahan, dan rekomendasi GO/NO-GO.

## 14. MVP dan Tahap Implementasi

### Tahap 0 — Penelitian dan PoC (wajib pertama)

- Audit metode resmi: Google Flow native, browser, Custom Tabs, Work Profile.
- Implementasi APK eksperimen minimal dengan pemilih 3 akun dan mekanisme uji pembukaan.
- Uji matriks pada §13; keluarkan keputusan GO/NO-GO.

### Tahap 1 — MVP dashboard (hanya jika GO)

- Struktur aplikasi Kotlin/Compose, navigasi, Room/DataStore.
- Tambah akun manual + picker + impor CSV/TXT.
- Dashboard list/grid, pencarian, status, dan buka akun melalui metode yang teruji.
- Edit/hapus akun dan pesan error yang akurat.

### Tahap 2 — Manajemen lanjutan

- Grup, favorit, urutan manual, histori, pemulihan halaman jika memungkinkan.
- PIN/biometrik, ekspor/impor metadata, optimasi performa.

### Tahap 3 — Rilis

- Unit/integration/UI tests, manual device testing, audit keamanan.
- Build release signed, dokumentasi instalasi APK, changelog.
- GitHub Actions untuk CI APK debug dan paket kandidat release. Signing keys disimpan sebagai secret dan tidak dikomit.

## 15. Quality Assurance dan Acceptance Criteria Final

**Requirement minimum untuk menyebut BYGA Flow Manager “berhasil”:**

1. Pengguna dapat menambahkan minimal 3 akun dan membedakan alias/email masing-masing.
2. Menekan akun A membuka Google Flow **sebagai akun A**, dibuktikan melalui pemeriksaan identitas nyata.
3. Akun B/C dapat dibuka secara bergantian tanpa menggantikan sesi akun lain.
4. Uji 20 kali perpindahan A/B/C menunjukkan **nol salah akun**.
5. Setelah restart aplikasi, metadata lokal tetap tersimpan; sesi Flow yang masih valid tetap terasosiasi dengan identitas yang benar.
6. Saat autentikasi ulang diperlukan, aplikasi menggunakan login resmi, tanpa meminta password/OTP pada UI BYGA.
7. Impor TXT/CSV menolak baris tidak valid dan tidak membuat duplikat tidak sengaja.
8. Dashboard list/grid, pencarian, folder, favorit, dan pengurutan bekerja.
9. Tidak ada cookie/password/token Google dalam Room, log, atau ekspor.
10. Jika item #2–#4 gagal secara teknis, proyek **tidak** boleh dilabeli selesai sesuai scope dan harus kembali ke keputusan produk.

## 16. Risiko dan Mitigasi

| Risiko | Tingkat | Mitigasi |
|---|---|---|
| Browser membagikan cookie antar akun | Kritis | PoC sebelum MVP; jangan menyamakan tab dengan profil. |
| Flow native tidak menyediakan pemilih akun via Intent publik | Kritis | Uji jalur native; jika tak tersedia jangan klaim one-tap. |
| Google memblokir embedded WebView OAuth | Kritis | Gunakan browser/autentikasi resmi; hindari WebView untuk login. |
| Android Work Profile tidak cukup banyak | Tinggi | Catat batas profil sebenarnya; bukan solusi default puluhan akun. |
| Sesi habis atau berubah dari luar aplikasi | Tinggi | Verifikasi ulang dengan cara resmi; status `UNVERIFIED` saat tidak tahu. |
| Website Flow kurang optimal di mobile | Sedang | Uji Flow native dan browser; jelaskan fungsi yang tidak kompatibel. |
| Generasi video terhenti/berubah ketika berpindah aplikasi | Sedang | Jangan janjikan pekerjaan background; Flow native sendiri menangani notifikasi sesuai kemampuan resmi. |
| Bocornya metadata email pada backup/log | Sedang | Minimasi log, kontrol backup, enkripsi data sesuai threat model. |
| Pembaruan Flow/Android mengubah perilaku | Tinggi | Pengujian regresi pada versi utama dan capability registry. |

## 17. Rekomendasi Struktur Repository

```text
byga-flow-manager/
├── README.md
├── PRD.md
├── docs/
│   ├── poc-test-plan.md
│   ├── poc-results.md
│   ├── security.md
│   └── acceptance-checklist.md
├── app/
│   └── src/main/
│       ├── java/.../byga/flowmanager/
│       │   ├── ui/
│       │   ├── data/
│       │   ├── domain/
│       │   ├── launcher/
│       │   └── security/
│       └── AndroidManifest.xml
├── gradle/
└── .github/workflows/
    ├── android-ci.yml
    └── apk-release.yml
```

GitHub Actions: lint, unit tests, assembleDebug, upload APK artifact, dan pipeline release bertanda tangan setelah langkah keamanan lulus. **Jangan membuat CI yang otomatis memublikasikan APK produksi sebelum PoC dinyatakan GO.**

## 18. Backlog Prioritas

| Tahap | Deliverable | Syarat |
|---|---|---|
| **P0-A** | APK PoC pembuka Flow untuk A/B/C | Tanpa perlu dashboard lengkap |
| **P0-B** | Laporan uji isolasi + GO/NO-GO | Wajib sebelum fitur lengkap |
| **P0-C** | Registrasi akun + dashboard + launcher | Hanya jika P0-B GO |
| **P0-D** | Import, search, status akurat, edit/hapus | Hanya jika P0-B GO |
| **P1** | Grup/favorit/list-grid/backup/kunci | Setelah P0 stabil |
| **P2** | Pelengkap workspace sesuai hasil pengujian | Tanpa klaim fungsi yang tidak tersedia |

## 19. Pertanyaan Terbuka (Tidak Menghalangi PoC)

Hal-hal berikut belum diputuskan lewat sesi tanya-jawab; gunakan default teknis sementara dan finalisasi setelah PoC:

- Jumlah akun nyata yang ditargetkan (UI awal didesain menangani 50 entri).
- Apakah APK dipakai sendiri atau akan dirilis ke pengguna lain.
- Apakah aplikasi Google Flow native wajib dibuka, atau browser yang telah terbukti cukup.
- Perangkat Android lain yang perlu menjadi target selain perangkat uji utama.
- Apakah backup cloud lintas perangkat diperlukan di fase mendatang.
- Perilaku jika hanya sebagian akun dapat memakai rute terisolasi.

**Prioritas penyelesaian:** PoC isolasi sesi terlebih dahulu. Pertanyaan tambahan tidak boleh menyebabkan coding fitur penuh sebelum feasibility gate.

## 20. Referensi Resmi dan Asumsi

Referensi teknis yang diperiksa pada **24 Oktober 2026**:

1. Google Flow Help — **Mulai menggunakan Google Flow (Android)**: https://support.google.com/flow/answer/16353333?co=GENIE.Platform%3DAndroid&hl=id
2. Google Play — **Google Flow (Google Labs)**: https://play.google.com/store/apps/details?id=com.google.android.apps.labs.whisk
3. Google Identity — **Best Practices for Implementing Sign in with Google** (WebView tidak didukung untuk login Google): https://developers.google.com/identity/siwg/best-practices
4. Google OAuth 2.0 policies — **Use secure browsers**: https://developers.google.com/identity/protocols/oauth2/policies
5. Chrome for Developers — **Overview of Android Custom Tabs** (default berbagi state browser): https://developer.chrome.com/docs/android/custom-tabs
6. Chrome for Developers — **Ephemeral Custom Tabs** (data hanya sepanjang sesi): https://developer.chrome.com/docs/android/custom-tabs/guide-ephemeral-tab
7. Android Developers — **Android Enterprise developer guide** (isolasi dan autentikasi dalam lingkungan managed): https://developer.android.google.cn/work/guide?hl=en

**Asumsi dokumen:** BYGA Flow Manager adalah aplikasi pendamping pihak ketiga, **bukan** produk resmi Google, tidak memperoleh akses internal ke Google Flow, dan hanya memakai integrasi yang tersedia secara sah. Dokumentasi dapat berubah sehingga perilaku launcher perlu diuji ulang saat implementasi.

---

## Pernyataan Scope

Dokumen ini mengunci **keputusan PRD #1–#7** dan menyediakan spesifikasi implementasi awal. **Definisi keberhasilan tetap ditentukan oleh requirement #2: pemisahan sesi yang nyata dan dapat diverifikasi.** UI premium, folder, banyak kartu akun, maupun APK yang sekadar membuka URL belum cukup untuk menyatakan proyek berhasil.
