# Keamanan Phase 0

## Data dan batas kepercayaan

BYGA adalah pendamping pihak ketiga, bukan produk Google. Tidak ada OAuth client, backend, analytics, WebView, akses account database Android, cookie jar, session manager Google, Accessibility Service, atau pembacaan browser private data. Tidak ada permission INTERNET karena jaringan dilakukan oleh aplikasi Flow/browser eksternal. Tidak ada GET_ACCOUNTS, QUERY_ALL_PACKAGES, cross-profile privilege, atau permission storage luas.

Yang disimpan: tiga target email, pilihan provider, histori percobaan, email yang dilihat tester, waktu, metode, versi provider, tipe profil, skenario/run, outcome, dan error code terbatas. Email dienkripsi AES-256-GCM memakai kunci Android Keystore non-exportable. AAD mengikat ciphertext ke field dan ID record; pertukaran record atau modifikasi ditolak. IV baru untuk setiap enkripsi. Kunci tidak ada di repository. Hardware backing/StrongBox bergantung perangkat dan tidak diklaim selalu tersedia.

Room bukan database terenkripsi seluruhnya: slot A/B/C, metode, package/version, timestamp, dan outcome tetap plaintext pada sandbox aplikasi. Enkripsi field mengurangi paparan email pada salinan DB; tidak melindungi perangkat unlocked yang telah dikompromikan atau proses BYGA yang dikuasai penyerang. UI menampilkan email untuk tujuan pemeriksaan; screenshot bisa memuat PII, sehingga bukti publik harus disamarkan. Tidak ada PIN/biometrik di fase eksperimen.

## Penyimpanan, backup, dan ekspor

Backup cloud dan device transfer aplikasi dikecualikan secara eksplisit agar ciphertext tidak dipindahkan tanpa kunci. Tidak ada fallback plaintext bila Keystore gagal. Key loss/ciphertext rusak menghasilkan error; pemulihan melalui hapus data aplikasi Android menghapus bukti lokal. Jangan uninstall sebelum mengekspor bukti yang diperlukan.

Ekspor JSON hanya melalui Android CreateDocument (Storage Access Framework), tanpa email, target login, cookie, atau catatan bebas. Laporan berisi model perangkat/provider/version, timestamp dan hasil pseudonim A/B/C; tetap bisa bersifat sensitif secara operasional, pilih lokasi penyimpanan dengan sadar. Penghapusan lokal menghapus metadata BYGA saja; tidak logout atau menghapus akun Google, dan tidak menghapus file ekspor terdahulu. Penghapusan SQLite bukan janji secure erasure forensik.

Tidak ada logging PII/exception mentah dari aplikasi. Kode error seperti `PROVIDER_UNAVAILABLE`, `ACTIVITY_NOT_FOUND`, `POLICY_DENIED` dibatasi. Jangan mengunggah `logcat` atau screenshot tanpa menyamarkan email/token/URL pribadi. Diagnostik build tidak mengandung kredensial login pengguna.

## Autentikasi

Pengguna login sendiri pada Flow/browser resmi. Email target hanya disimpan lokal dan tidak disisipkan ke URL, `authuser`, private Intent extra, atau deep link yang ditebak. Package native mengikuti referensi Play PRD; install dari publisher Google Labs resmi. Deteksi package bukan verifikasi tanda tangan publisher dan bukan bukti login. Default Custom Tabs berbagi sesi browser, sehingga tab tidak diperlakukan sebagai profil terisolasi. BYGA tidak menjanjikan aktivitas video background.

## CI dan signing

Gradle wrapper dan checksum distribusi dikomit; validasi wrapper via gradle/actions/setup-gradle. CI hanya membangun APK debug eksperimen. Secrets `BYGA_KEYSTORE_BASE64`, `BYGA_KEYSTORE_PASSWORD`, `BYGA_KEY_ALIAS`, `BYGA_KEY_PASSWORD` hanya dipakai workflow manual setelah gate GO. Keystore decode ke runner temp, permission ketat, dihapus dengan `if: always()`. Jangan commit local.properties, keystore, password, APK, atau account test credentials.

Workflow release tidak menerbitkan GitHub Release/Play Store; hanya signed candidate artifact setelah gate. Konfigurasikan protected branch dan required reviewer pada environment `production` untuk review bukti yang nyata. Script gate hanya guard konfigurasi; tidak bisa menggantikan penilaian manusia atas bukti. Tetapkan CODEOWNERS/branch protection sesuai pemilik repository saat tim tersedia.
