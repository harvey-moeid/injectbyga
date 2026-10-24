# Keamanan organizer satu browser

Tidak ada backend, analytics, OAuth client, login WebView, akses cookie/token/private browser storage, root, atau Accessibility automation. Login dan pergantian akun dilakukan pengguna pada browser/Flow resmi. Account chooser Android hanya metadata email yang dipilih dengan persetujuan; tidak memakai GET_ACCOUNTS atau membaca daftar akun diam-diam.

## Penyimpanan

Email, alias, catatan proyek, nama grup, target launch dan email aktual dari konfirmasi dienkripsi AES-256-GCM dengan Android Keystore. IV acak tiap enkripsi, AAD field/record; ciphertext yang dimodifikasi/dipertukarkan ditolak. Key tidak diekspor atau dikomit. Hardware backing tergantung perangkat dan tidak dijamin. Key generation memakai lock lintas instance, berbagi key yang sudah ada dengan PoC agar upgrade tidak merusak data lama.

Room bukan full database encryption: UUID, group membership, favorit, warna, timestamp, package/version dan outcome tetap plaintext dalam sandbox Android. Enkripsi tidak melindungi proses/perangkat yang sudah dikuasai penyerang. Backup cloud/device-transfer dikecualikan. Tidak ada fallback plaintext ketika Keystore rusak/hilang; reset data Android menghapus metadata.

Kunci dashboard opsional menggunakan Android BiometricPrompt (BIOMETRIC_STRONG atau DEVICE_CREDENTIAL). Tidak membuat/menyimpan PIN sendiri atau password Google. Dashboard dan Lab sama-sama memeriksa lock; state unlock tidak dipersistenkan dan hilang ketika aplikasi ditinggalkan. Biometrik/PIN, proses mati, dan OEM behavior perlu diuji pada HP nyata. Tanpa kunci aktif, siapa pun dengan akses ke aplikasi dapat membaca metadata di UI.

## Impor/ekspor dan hapus

Impor CSV/TXT lewat Storage Access Framework dibatasi2 MB/5.000 baris dan UTF-8 ketat, tanpa persist URI permission yang tidak diperlukan. Hanya header email/alias/group; header tak dikenal (password/OTP/cookie/token) ditolak. Raw baris invalid tidak ditampilkan/dicatat; preview menampilkan metadata valid dan line/reason penolakan. Impor recheck duplikat secara atomik dan tidak menimpa akun lama.

Ekspor CSV hanya email, alias dan grup. Pengguna mengonfirmasi bahwa file mengandung metadata pribadi dalam plaintext, lalu memilih lokasi. Formula spreadsheet yang diawali=,+,-,@ dinetralkan dengan apostrof, kutipan di-escape. Apostrof literal dapat terbawa saat reimport; ekspor ini daftar akun, bukan full backup seluruh preferensi/history/favorit/catatan.

Hapus akun menghapus metadata dashboard dan riwayat terkait melalui foreign-key cascade; hapus grup hanya melepas membership. Tidak logout, menghapus Google Account, mengubah sesi browser, menghapus data PoC terpisah, atau file ekspor lama. SQLite delete bukan klaim secure forensic erasure.

## Identitas dan diagnostik

Intent browser memuat hanya URL Flow HTTPS dan package provider yang dipilih untuk semua akun. Tidak ada email, authuser atau token pada URI/extras. Browser tidak tersedia tidak dialihkan diam-diam. Konfirmasi manual bukan verifikasi otomatis; status akun aktif sekarang selalu tidak diketahui. Error storage/import/crypto dilaporkan melalui pesan aman; aplikasi tidak log exception mentah/email. Jangan mengunggah screenshot/log yang berisi PII tanpa penyamaran.

CI hanya memakai data sintetik. Gradle wrapper/checksum dipin; lint error tetap fatal dan version warnings terlihat. APK debug untuk uji. Workflow signing manual tetap memerlukan gate review dan Secrets; key decode hanya ke runner temp lalu dihapus. Tidak ada secret, keystore atau password di repository dan tidak ada publikasi produksi/merge otomatis.
