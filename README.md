# Software Requirements Specification (SRS)
# Habisin — Marketplace Makanan Surplus

**Versi:** 1.0  
**Tanggal:** 15 November 2026  
**Status:** Draft berdasarkan implementasi project saat ini  
**Platform:** Android, FastAPI, MySQL/MariaDB XAMPP

---

## 1. Ringkasan

Habisin adalah aplikasi marketplace makanan surplus. Penjual dapat menawarkan makanan yang masih layak konsumsi dengan harga diskon. Pembeli dapat mencari produk, menyimpan favorit, memasukkan produk ke keranjang, melakukan checkout multi-produk, membayar melalui QR lokal simulasi, memantau pesanan, dan memberi ulasan.

Project terdiri dari:

- Android native dengan Jetpack Compose dan MVVM.
- Backend REST API dengan FastAPI.
- Database MySQL/MariaDB melalui XAMPP.
- Web pembayaran lokal untuk QR simulasi.
- Fitur AI melalui backend.

GitHub dan pengelolaan commit tidak termasuk scope dokumen ini.

---

## 2. Tujuan Sistem

1. Mengurangi pemborosan makanan melalui penjualan makanan surplus.
2. Membantu pembeli memperoleh makanan dengan harga lebih rendah.
3. Memberi penjual sarana mengelola produk dan pesanan.
4. Menyediakan checkout multi-produk dengan stok yang konsisten.
5. Menyediakan pembayaran lokal simulasi yang aman untuk pengembangan.
6. Menyediakan bantuan AI berbasis data produk dan penjualan.

---

## 3. Ruang Lingkup

### 3.1 Termasuk

- Registrasi, login, refresh token, dan informasi akun.
- Peran pembeli dan penjual.
- Katalog produk.
- CRUD produk penjual.
- Upload gambar produk.
- Pencarian dan pengurutan produk.
- Daftar toko dan detail toko.
- Favorit dan ulasan.
- Keranjang dengan CRUD.
- Checkout multi-produk.
- QR pembayaran lokal simulasi.
- Sinkronisasi status pembayaran dan riwayat pesanan.
- Pembatalan dan pembaruan status pesanan.
- Ekspor pesanan CSV.
- Fitur AI pembeli dan penjual.
- Profil dan penyimpanan sesi Android.

### 3.2 Tidak termasuk

- Pembayaran bank, QRIS, e-wallet, atau payment gateway nyata.
- Pengiriman atau kurir eksternal.
- Notifikasi push produksi.
- Panel admin terpisah.
- Deployment cloud produksi.
- Pengelolaan GitHub atau commit.

---

## 4. Aktor Sistem

| Aktor | Deskripsi |
|---|---|
| Pengunjung | Pengguna yang belum login dan dapat melihat halaman autentikasi/katalog sesuai aturan API. |
| Pembeli | Pengguna yang mencari produk, memakai keranjang, checkout, membayar, melihat pesanan, favorit, dan ulasan. |
| Penjual | Pengguna yang mengelola produk miliknya, melihat pesanan toko, mengubah status pesanan, dan memakai AI penjual. |
| Backend | Layanan FastAPI yang memvalidasi autentikasi, otorisasi, stok, transaksi, dan data bisnis. |
| Database | MySQL/MariaDB yang menyimpan akun, produk, pesanan, pembayaran, keranjang, favorit, ulasan, dan data terkait. |

---

## 5. Kebutuhan Fungsional

### FR-01 Autentikasi dan akun

- Sistem harus menyediakan registrasi akun.
- Sistem harus menolak email yang sudah terdaftar.
- Sistem harus menyediakan login dengan email dan password.
- Sistem harus menerbitkan access token dan refresh token setelah autentikasi berhasil.
- Sistem harus menyediakan refresh token.
- Sistem harus menyediakan endpoint akun aktif.
- Sistem harus menyimpan token secara lokal di Android setelah login berhasil.
- Sistem harus menghapus sesi lokal saat logout.
- Sistem harus membedakan peran `pembeli` dan `penjual`.
- Backend harus menjadi sumber kebenaran untuk autentikasi dan peran.

Endpoint utama:

```text
POST /auth/register
POST /auth/login
GET  /auth/me
POST /auth/refresh
```

### FR-02 Katalog produk

- Pembeli dapat melihat produk aktif.
- Sistem harus dapat mencari produk.
- Sistem harus dapat memfilter jenis makanan.
- Sistem harus dapat mengurutkan produk.
- Produk dengan stok habis tidak ditampilkan di katalog pembeli.
- Data produk harus berasal dari backend, bukan data demo hardcode.
- Informasi produk minimal mencakup nama, deskripsi, harga normal, harga diskon, stok, jenis makanan, batas konsumsi, cara penyimpanan, gambar, dan nama penjual jika tersedia.

Endpoint utama:

```text
GET /produk
GET /produk/{product_id}
GET /produk/penjual
```

### FR-03 Manajemen produk penjual

- Penjual dapat membuat produk.
- Penjual dapat melihat produk miliknya.
- Penjual dapat mengubah produk miliknya.
- Penjual dapat menghapus atau menonaktifkan produk sesuai aturan riwayat pesanan.
- Penjual dapat mengunggah gambar produk.
- Backend harus memvalidasi kepemilikan produk.
- Backend harus memvalidasi harga, stok, tanggal konsumsi, dan data wajib.

Endpoint utama:

```text
POST   /produk
PUT    /produk/{product_id}
DELETE /produk/{product_id}
POST   /produk/{product_id}/gambar
```

### FR-04 Toko

- Pembeli dapat melihat daftar toko.
- Pembeli dapat membuka detail toko berdasarkan ID stabil.
- Detail toko menampilkan profil toko dan produk yang tersedia.
- Toko tanpa produk aktif tidak ditampilkan pada menu pembeli.

Endpoint utama:

```text
GET /toko
GET /toko/{seller_id}
```

### FR-05 Favorit

- Pembeli dapat melihat daftar favorit.
- Pembeli dapat mencari favorit.
- Pembeli dapat menambah favorit.
- Pembeli dapat mengubah catatan favorit miliknya.
- Pembeli dapat menghapus favorit miliknya.
- Data favorit harus menampilkan data produk terkait jika tersedia.

Endpoint utama:

```text
GET    /favorit
POST   /favorit/{product_id}
PUT    /favorit/{favorite_id}
DELETE /favorit/{favorite_id}
```

### FR-06 Ulasan

- Pembeli dapat membuat ulasan untuk pesanan yang memenuhi aturan bisnis.
- Pembeli dapat melihat ulasan.
- Pembeli dapat memfilter ulasan berdasarkan produk, rating, atau pencarian.
- Pembeli dapat mengubah ulasan miliknya.
- Pembeli dapat menghapus ulasan miliknya.
- Backend harus memvalidasi kepemilikan dan rating.

Endpoint utama:

```text
GET    /ulasan
GET    /ulasan/saya
POST   /ulasan/{order_id}
PUT    /ulasan/{review_id}
DELETE /ulasan/{review_id}
```

### FR-07 Keranjang

- Pembeli dapat melihat isi keranjang.
- Pembeli dapat mencari isi keranjang.
- Pembeli dapat menambah produk ke keranjang.
- Pembeli dapat mengubah jumlah produk.
- Pembeli dapat menghapus item keranjang.
- Jumlah harus lebih besar dari nol.
- Backend harus memvalidasi stok saat penambahan dan pembaruan.
- Item keranjang harus terikat pada user yang sedang login.

Endpoint utama:

```text
GET    /keranjang
POST   /keranjang
PUT    /keranjang/{item_id}
DELETE /keranjang/{item_id}
```

### FR-08 Checkout dan pembayaran QR lokal

- Checkout harus mendukung beberapa produk dalam satu transaksi.
- Backend harus mengunci baris produk saat memeriksa dan mengurangi stok.
- Checkout harus membuat satu `CheckoutSession` untuk seluruh item.
- Checkout yang masih aktif tidak boleh membuat order dan pengurangan stok ganda.
- Keranjang tidak boleh dihapus saat QR baru dibuat.
- QR harus memiliki token acak dan masa berlaku terbatas.
- QR simulasi harus memakai URL pembayaran lokal yang dikonfigurasi melalui environment backend.
- QR kedaluwarsa harus dapat diperbarui.
- QR lama harus ditolak setelah refresh atau pembayaran selesai.
- Keranjang hanya boleh dihapus setelah pembayaran sukses.
- Pembayaran sukses harus mengubah seluruh order dalam session menjadi `sudah_dibayar`.
- Android harus memperbarui status checkout saat dialog QR aktif.

Endpoint utama:

```text
POST /keranjang/checkout
GET  /keranjang/checkout/{session_id}/qr
GET  /keranjang/checkout/{session_id}/status
POST /keranjang/checkout/{session_id}/refresh
```

Endpoint halaman pembayaran lokal:

```text
GET  /pesanan/bayar/{payment_code}
GET  /pesanan/bayar-data/{payment_code}
POST /pesanan/bayar/{payment_code}
```

### FR-09 Pesanan

- Pembeli dapat melihat riwayat pesanan sendiri.
- Penjual dapat melihat pesanan yang berkaitan dengan produknya.
- Pembeli dapat membayar pesanan yang belum dibayar sesuai aturan sistem.
- Pembeli dapat membatalkan pesanan sesuai aturan status.
- Penjual dapat mengubah status proses pesanan.
- Riwayat harus menampilkan nama produk dari backend, bukan hanya ID.
- Status pembayaran harus tersinkron dengan `CheckoutSession`.
- Sistem harus mempertahankan data historis pesanan.

Status proses yang digunakan:

```text
belum_dibayar
sudah_dibayar
diproses
siap_diambil
selesai
```

Endpoint utama:

```text
POST  /pesanan
GET   /pesanan/saya
PUT   /pesanan/{order_id}
POST  /pesanan/{order_id}/bayar
POST  /pesanan/{order_id}/batal
GET   /pesanan/semua
PATCH /pesanan/{order_id}/status
```

### FR-10 Toko dan laporan

- Sistem harus menyediakan ekspor pesanan ke CSV.
- Penjual dapat melihat ringkasan toko sesuai hak aksesnya.
- Pembeli tidak boleh melihat data penjual yang tidak diizinkan.

Endpoint utama:

```text
GET /pesanan/export.csv
GET /toko
GET /toko/{seller_id}
```

### FR-11 AI

- Pembeli dapat mengirim pertanyaan umum melalui AI.
- Pembeli dapat melakukan pencarian makanan berbasis bahasa alami.
- Pembeli dapat menanyakan bahan atau kandungan produk.
- Pembeli dapat melihat dampak aktivitasnya.
- Penjual dapat meminta saran harga.
- Penjual dapat meminta bantuan membuat deskripsi produk.
- Penjual dapat melihat ringkasan penjualan.
- Provider AI hanya boleh dipanggil dari backend.
- API key AI tidak boleh masuk APK, website, atau response client.
- Prompt user harus divalidasi dan dibatasi panjangnya.
- Jawaban AI tidak boleh mengarang bahan atau menjamin keamanan makanan.
- Pertanyaan kondisi makanan harus menyertakan batasan/safety disclaimer sesuai aturan backend.

Endpoint utama:

```text
POST /ai/chat
POST /ai/saran-harga
POST /ai/deskripsi-produk
GET  /ai/ringkasan-penjualan
POST /ai/cari
GET  /ai/dampakku
POST /ai/tanya-produk
```

### FR-12 Profil dan navigasi Android

Android harus menyediakan layar:

- Login.
- Registrasi.
- Beranda.
- Detail produk melalui kartu produk.
- AI.
- Favorit.
- Keranjang.
- Riwayat pesanan.
- Profil.
- Daftar toko.
- Detail toko.
- Dashboard penjual.

Teks UI harus berada di `res/values/strings.xml` jika merupakan teks tampilan pengguna. Route, status internal, nama field JSON, dan nilai default teknis bukan teks UI.

---

## 6. Kebutuhan Nonfungsional

### NFR-01 Keamanan

- Password harus disimpan dalam bentuk hash.
- Endpoint terlindungi harus memerlukan bearer token.
- Backend harus memvalidasi peran dan kepemilikan data.
- Token, password, API key, dan connection string tidak boleh ditulis ke log atau source Android.
- API key provider hanya berada di environment backend.
- Endpoint pembayaran harus memvalidasi token, session, status, dan expiry.
- Semua input dari client harus divalidasi pada boundary backend.

### NFR-02 Konsistensi data

- Checkout multi-item harus atomik.
- Stok tidak boleh berkurang dua kali akibat retry session aktif.
- Pembayaran sukses harus memperbarui seluruh order dalam session.
- Keranjang tidak boleh hilang sebelum pembayaran sukses.
- Perubahan stok harus memakai transaksi dan row locking.

### NFR-03 Performa dan reliabilitas

- UI harus menampilkan loading, empty state, dan error state.
- Request jaringan tidak boleh dijalankan langsung di composable tanpa state management.
- Login tidak boleh mengirim request ganda saat loading.
- Backend harus dapat berjalan pada `0.0.0.0` untuk pengujian perangkat lokal.
- Android emulator memakai route lokal yang sesuai dengan konfigurasi debugging.

### NFR-04 Maintainability

- Struktur Android mengikuti pemisahan API, repository, ViewModel, screen, navigation, dan resource.
- Backend menggunakan router terpisah.
- Perubahan schema harus idempotent dan tidak menghapus database aplikasi.
- Backend tidak boleh menyimpan data produk demo hardcode untuk menggantikan database.

### NFR-05 Accessibility dan usability

- Tombol harus memiliki teks atau content description yang jelas.
- Form harus memberi feedback error.
- Tombol pembayaran harus menunjukkan status proses.
- Countdown QR harus mudah dibaca.
- Empty state dan error state harus dapat dipahami pengguna.

---

## 7. Model Data Utama

Entitas utama:

- `User`
- `Product`
- `Order`
- `CartItem`
- `CheckoutSession`
- `Favorite`
- `Review`
- Data toko/penjual
- Refresh token atau data sesi autentikasi

Relasi penting:

- Satu user dapat memiliki banyak produk sebagai penjual.
- Satu user dapat memiliki banyak item keranjang.
- Satu checkout session memiliki banyak order.
- Satu order dapat merujuk pada satu product dan satu checkout session.
- Satu user dapat memiliki banyak favorit, ulasan, dan pesanan.
- Produk milik penjual harus dilindungi dengan ownership check.

---

## 8. Arsitektur

### Android

```text
Compose UI
   ↓
ViewModel
   ↓
Repository/API client
   ↓
FastAPI REST API
```

Komponen Android:

- Jetpack Compose.
- MVVM.
- Retrofit.
- OkHttp interceptor untuk bearer token dan refresh.
- Navigation Compose.
- Resource string pada `res/values/strings.xml`.
- Session storage lokal.

### Backend

```text
FastAPI
  ├── auth router
  ├── products router
  ├── cart router
  ├── orders router
  ├── social router
  ├── reports router
  └── ai router
        ↓
SQLAlchemy
        ↓
MySQL/MariaDB XAMPP
```

---

## 9. Konfigurasi Lokal

Backend, database, dan web dijalankan dengan Docker Compose dari folder `backend`:

```text
docker compose up -d --build
docker compose exec api python -m app.seed_demo
```

`seed_demo` menyiapkan akun demo dan produk surplus yang batas konsumsinya masih berlaku, sehingga katalog, keranjang, dan pembayaran QR langsung bisa diuji.

Akun demo:

```text
pembeli@habisin.id / habisin123   (peran pembeli)
penjual@habisin.id / habisin123   (peran penjual)
```

Android emulator memakai `adb reverse tcp:8000 tcp:8000` sehingga aplikasi mengakses `127.0.0.1:8000`. Perangkat fisik memakai IP LAN laptop yang berada pada jaringan Wi-Fi sama, dan IP tersebut harus didaftarkan ke backend agar isi QR pembayaran dapat dibuka HP:

```text
python update_host_ip.py            # QR berisi http://<IP-LAN>:8000/... (Wi-Fi rumah/kampus)
python update_host_ip.py --tunnel   # QR berisi https://<acak>.trycloudflare.com/... (tanpa IP antar perangkat)
```

Skrip menulis `backend/hostnet/ip.txt` atau `backend/hostnet/base.txt` yang di-mount ke container API. Backend membacanya pada setiap permintaan QR, jadi tidak perlu restart. Status alamat yang sedang dipakai dapat dicek di `GET /health` pada field `qr_base`.

Untuk demo tanpa mengatur IP sama sekali, jalankan `backend\start_demo.bat`: container dinyalakan, data demo disiapkan, URL tunnel ditulis, lalu `tunnel_watch.py` memantau URL tunnel dan memperbaruinya otomatis ketika container tunnel restart. Biarkan jendela tersebut terbuka selama demo. Syaratnya laptop dan HP terhubung internet (HP boleh memakai data seluler, tidak harus Wi-Fi yang sama).

URL pembayaran lokal harus berasal dari environment atau berkas di atas. Jangan memakai IP contoh, credential, token, atau API key di dokumen ini.

---

## 10. Kriteria Penerimaan

### AC-01 Autentikasi

- Registrasi user baru berhasil.
- Email duplikat ditolak.
- Login benar berhasil dan menyimpan sesi.
- Password salah ditolak.
- Endpoint protected menolak request tanpa token.

### AC-02 Produk

- Penjual dapat create, read, update, dan delete produk miliknya.
- Penjual tidak dapat mengubah produk penjual lain.
- Pembeli dapat mencari dan membuka detail produk.
- Produk stok nol tidak muncul di feed pembeli.

### AC-03 Keranjang dan checkout

- Produk dapat ditambah, jumlah diubah, dan item dihapus.
- Checkout beberapa produk membuat satu session.
- Stok berkurang sesuai jumlah.
- Checkout ulang pada session aktif tidak mengurangi stok dua kali.
- Keranjang masih ada sebelum pembayaran.

### AC-04 Pembayaran

- QR dapat ditampilkan.
- QR kedaluwarsa setelah batas waktu.
- QR baru membatalkan QR lama.
- Pembayaran sukses mengubah semua order session menjadi `sudah_dibayar`.
- Cart terhapus hanya setelah pembayaran sukses.

### AC-05 Riwayat

- Riwayat menampilkan nama produk dan status pembayaran terbaru.
- Status riwayat berubah setelah pembayaran.
- Pembatalan tidak menghapus histori secara tidak aman.

### AC-06 AI

- Request AI melewati backend.
- Provider key tidak terlihat di APK atau response.
- Provider gagal menghasilkan status disconnected, bukan jawaban katalog palsu.
- Pertanyaan bahan memakai data produk yang tersedia.

---

## 11. Status Implementasi Saat Ini

| Area | Status |
|---|---|
| Android build dan unit test | Terverifikasi berhasil pada build terakhir |
| Auth Android/API | Diimplementasikan; runtime login perlu diuji pada device aktif |
| Produk | Diimplementasikan |
| Favorit | Diimplementasikan |
| Ulasan | Diimplementasikan |
| Pesanan | Diimplementasikan |
| Keranjang CRUD | Diimplementasikan |
| Checkout multi-produk | Diimplementasikan pada kode; runtime end-to-end belum seluruhnya diverifikasi |
| QR pembayaran lokal | Diimplementasikan pada kode; scan dari device lain masih perlu uji runtime |
| Sinkronisasi pembayaran-riwayat | Diimplementasikan pada kode; perlu uji transaksi nyata pada database aktif |
| AI | Endpoint dan UI tersedia; status provider/runtime perlu diverifikasi |
| MariaDB XAMPP | Berhasil start dan port 3306 terverifikasi |
| Apache XAMPP | Port HTTP/HTTPS terdeteksi aktif, panel XAMPP perlu satu instance saja |
| Deployment produksi | Belum termasuk scope verifikasi |

---

## 12. Risiko dan Batasan

- IP LAN laptop dapat berubah.
- Firewall Windows dapat memblokir akses perangkat fisik.
- QR lokal bukan pembayaran nyata.
- Provider AI dapat gagal, timeout, atau menolak request.
- Schema database lama memerlukan migrasi idempotent.
- Runtime authenticated flow belum dapat dianggap selesai hanya dari build.
- Pengujian scan QR memerlukan dua perangkat pada jaringan yang sama.

---

## 13. Rencana Verifikasi

1. Jalankan MariaDB dan backend.
2. Uji `/health`.
3. Instal APK terbaru.
4. Uji registrasi dan login.
5. Uji CRUD produk sebagai penjual.
6. Uji katalog sebagai pembeli.
7. Uji favorit, ulasan, dan toko.
8. Uji cart CRUD.
9. Uji checkout multi-produk.
10. Scan QR dari perangkat kedua.
11. Bayar melalui halaman lokal.
12. Verifikasi database, cart, status session, dan riwayat order.
13. Uji status pesanan dari sisi penjual.
14. Uji AI dengan provider tersedia dan provider gagal.
15. Catat hasil runtime terpisah dari hasil compile.

---

## 14. Artefak Project

```text
Project root:
C:\Users\raflu\AndroidStudioProjects\Habisin

Android source:
app\src\main\java

Android resources:
app\src\main\res

Backend:
backend\app

Debug APK:
app\build\outputs\apk\debug\app-debug.apk
```

---

## 15. Catatan Perubahan

Dokumen ini menggambarkan kebutuhan dan implementasi project saat ini. Status `Diimplementasikan` berarti route atau UI tersedia di source. Status runtime hanya dianggap terverifikasi setelah flow dijalankan pada backend, database, dan perangkat Android yang aktif.

---

## 16. Pembayaran QR Simulasi

Alur pembayaran yang berlaku saat ini:

1. Pembeli menekan **Bayar dengan QR** pada layar Keranjang.
2. `POST /keranjang/checkout` mengunci stok, membuat satu sesi checkout, dan menerbitkan `payment_code` acak (`secrets.token_urlsafe`) berlaku 60 detik.
3. `GET /keranjang/checkout/{id}/qr` mengembalikan gambar QR dalam Base64 beserta `payment_code`, `total_harga`, dan `berlaku_sampai`. Isi QR adalah `<base>/pesanan/bayar/<payment_code>`.
4. HP lain memindai QR dan membuka halaman bayar. Halaman menampilkan total, sisa waktu, dan tombol **Bayar sekarang**.
5. `POST /pesanan/bayar/{payment_code}` menandai seluruh pesanan pada sesi `sudah_dibayar`, mengosongkan keranjang, dan menutup sesi. Aplikasi menutup dialog lewat polling status sesi.
6. `POST /keranjang/checkout/{id}/refresh` menerbitkan kode baru dan membatalkan kode lama, sehingga QR lama tidak dapat dipakai lagi.

Kode QR pada menu **Pesanan** (`GET /pesanan/{id}/qr`) memakai kode sesi checkout untuk pesanan hasil checkout, dan menerbitkan kode baru bila kode sebelumnya sudah kedaluwarsa.

Penyebab umum QR tidak dapat dibuka di HP:

| Gejala | Penyebab | Solusi |
|---|---|---|
| Scan tidak membuka apa pun | Isi QR memakai `127.0.0.1` atau IP internal Docker (`172.x`) | Jalankan `python update_host_ip.py` lalu cek `qr_base` di `/health` |
| Scan membuka tetapi halaman kosong/error | IP LAN berubah karena DHCP | Jalankan ulang `python update_host_ip.py` |
| Scan tetap gagal di Wi-Fi publik/kafe | Wi-Fi memblokir akses antar-perangkat | Jalankan `python update_host_ip.py --tunnel`, atau `start_demo.bat` |
| Aplikasi tidak menampilkan dialog QR | Keranjang kosong atau stok tidak mencukupi | Pesan kesalahan sekarang tampil sebagai snackbar pada layar Keranjang/Pesanan |

---

## 17. Status Fitur terhadap Pedoman PAM 2026

| Ketentuan Pedoman | Implementasi | Status |
|---|---|---|
| Login dan register | `POST /auth/register`, `POST /auth/login`, layar Masuk/Daftar Compose | Selesai |
| Access token JWT dan refresh token | `security.py`, `POST /auth/refresh`, `AuthInterceptor` | Selesai |
| Otorisasi berbasis peran | Peran `pembeli`/`penjual`, menu berbeda, endpoint menolak dengan 403 | Selesai |
| Navigasi multi-halaman | Navigation Compose pada `HabisinNavHost` | Selesai |
| State management MVVM | ViewModel + Repository + Retrofit untuk setiap fitur | Selesai |
| Minimal 4 entitas saling terhubung | `users`, `products`, `cart_items`, `orders`, `checkout_sessions`, `ulasan`, `favorit` | Selesai |
| Minimal 5 fitur CRUDS penuh | Produk, keranjang, pesanan, ulasan, favorit (create/read/update/delete/search) | Selesai |
| Integrasi REST API dan parsing JSON | Retrofit + Gson pada seluruh layar | Selesai |
| Database MySQL | MySQL 8.0 dalam container Docker | Selesai |
| Penyimpanan berkas lokal | `LocalFileStore` menyimpan dan membaca struk pesanan, sekaligus disalin ke tabel `struk` di MySQL | Selesai |
| UI hanya Jetpack Compose | Seluruh layar Compose Material 3 dengan tema Habisin | Selesai |
| README berisi status fitur dan cara menjalankan | Dokumen ini, bagian 9, 11, 16, dan 17 | Selesai |

---

## 18. Struk Pembayaran

Struk disimpan pada dua tempat agar daftar selalu konsisten dan tetap bisa dibaca tanpa jaringan:

1. **MySQL** — tabel `struk` (`user_id`, `order_id`, `nama_berkas`, `isi`, `total_harga`, `dibuat_pada`). Isi struk dibuat di backend dari data pesanan, sehingga formatnya seragam.
2. **Penyimpanan perangkat** — salinan berkas `struk-<id>.txt` pada `filesDir/struk` melalui `LocalFileStore`.

Endpoint:

```text
POST   /struk              { "order_id": 66 }  -> simpan/perbarui struk (upsert per pesanan)
GET    /struk?q=struk      -> daftar struk milik pengguna
GET    /struk/{id}         -> detail beserta isi struk
DELETE /struk/{id}         -> hapus struk (server dan berkas lokal di aplikasi)
```

Layar **Struk Tersimpan** membaca daftar dari `GET /struk` dan menggabungkannya dengan berkas lokal yang belum tersinkron. Berkas yang hanya ada di perangkat diberi penanda `hanya di perangkat`, sehingga daftar tidak pernah tampak kosong padahal struk sudah tersimpan.

---

## 19. Matriks CRUDS Fitur

Bukti pemenuhan syarat "minimal 5 fitur dengan CRUDS penuh" pada pedoman.

| Fitur | Create | Read | Update | Delete | Search |
|---|---|---|---|---|---|
| Produk | `POST /produk` | `GET /produk`, `GET /produk/{id}` | `PUT /produk/{id}` | `DELETE /produk/{id}` | `?q=`, `?jenis_makanan=`, `?sort=` |
| Keranjang | `POST /keranjang` | `GET /keranjang` | `PUT /keranjang/{id}` | `DELETE /keranjang/{id}` | `?q=` |
| Ulasan | `POST /ulasan/{order_id}` | `GET /ulasan`, `GET /ulasan/saya` | `PUT /ulasan/{id}` | `DELETE /ulasan/{id}` | `?q=`, `?rating=`, `?product_id=` |
| Favorit | `POST /favorit/{product_id}` | `GET /favorit` | `PUT /favorit/{id}` (catatan) | `DELETE /favorit/{id}` | `?q=` |
| Struk | `POST /struk` | `GET /struk`, `GET /struk/{id}` | `POST /struk` (upsert per pesanan) | `DELETE /struk/{id}` | `?q=` |
| Pesanan | `POST /pesanan` | `GET /pesanan/saya`, `GET /pesanan/semua` | `PUT /pesanan/{id}`, `PATCH /pesanan/{id}/status` | `POST /pesanan/{id}/batal` (status, bukan hapus — SRS REQ-4) | `?q=`, `?status=` |

Lima fitur pertama memenuhi CRUDS penuh; pesanan sengaja tidak menghapus baris karena SRS 3.7 mewajibkan riwayat tetap utuh saat dibatalkan.

## 20. Entitas Basis Data

| Tabel | Relasi |
|---|---|
| `users` | induk untuk `products`, `cart_items`, `orders`, `ulasan`, `favorit`, `struk`, `refresh_tokens` |
| `products` | milik satu `users` (penjual); dirujuk `cart_items`, `orders`, `ulasan`, `favorit` |
| `cart_items` | satu baris per produk per pembeli |
| `orders` | milik pembeli, merujuk produk, opsional ke `checkout_sessions` |
| `checkout_sessions` | satu sesi pembayaran untuk banyak `orders` (kode QR acak 60 detik) |
| `ulasan` | satu ulasan per pesanan selesai |
| `favorit` | produk yang ditandai pembeli + catatan pribadi |
| `struk` | salinan struk pembayaran yang juga disimpan sebagai berkas di perangkat |
| `refresh_tokens` | token penyegaran sesi |

## 21. Progress Mingguan

Kewajiban pedoman: minimal satu progress per minggu yang dicantumkan di README.

Status repo saat ini: **2 commit baru** (tahap setup). Fitur aplikasi sudah berjalan di mesin lokal dan akan di-commit bertahap, satu kelompok per minggu, agar riwayat commit mencerminkan proses pengerjaan.

### 21.1 Progress yang sudah di-commit

| Minggu | Tanggal | Progress | Commit |
|---|---|---|---|
| 1 | 22 Nov 2026 | `.gitignore` untuk data lokal (MySQL, `.env`, log, tunnel) | `f07d1e7` |
| 1 | 22 Nov 2026 | Konfigurasi Gradle, wrapper, manifest, `keepRules`, `proguard` | `c322a05` |

### 21.2 Rencana commit berikutnya

| # | Kelompok commit | Status |
|---|---|---|
| 3 | Tema: token warna, tipografi, `dimens.xml`, `strings.xml`, aset launcher | belum |
| 4 | Klien REST API, model, sesi, util format | belum |
| 5 | Komponen reusable: tombol, input, kartu, ikon | belum |
| 6 | Data contoh produk, toko, pesanan, ulasan, struk | belum |
| 7 | Navigasi Compose bertipe | belum |
| 8 | Endpoint klien produk, toko, laporan | belum |
| 9 | ViewModel autentikasi dan beranda | belum |
| 10 | ViewModel keranjang dan pesanan | belum |
| 11 | ViewModel favorit, ulasan, struk | belum |
| 12 | ViewModel penjual, toko, Habisin AI | belum |
| 13 | Layar masuk dan buat akun | belum |
| 14 | Layar beranda | belum |
| 15 | Layar keranjang dan pembayaran QR | belum |
| 16 | Layar pesanan dan struk tersimpan | belum |
| 17 | Layar favorit, ulasan, daftar toko, detail toko | belum |
| 18 | Layar profil | belum |
| 19 | Dashboard penjual dan Habisin AI | belum |
| 20 | Unit test dan instrumented test | belum |
| 21 | Backend: konfigurasi, Docker, skema basis data | belum |
| 22 | Backend: autentikasi JWT dan produk | belum |
| 23 | Backend: keranjang dan checkout QR | belum |
| 24 | Backend: pesanan dan pembayaran | belum |
| 25 | Backend: ulasan, favorit, laporan, AI, struk | belum |
| 26 | Backend: seed demo dan skrip operasional | belum |
| 27 | Klien web pembayaran | belum |
| 28 | README: SRS, matriks CRUDS, progress | belum |

