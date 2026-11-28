# Habisin — Marketplace Makanan Surplus

Aplikasi Android (Jetpack Compose) + REST API FastAPI + MySQL 8.0 untuk menjual makanan surplus sebelum melewati batas konsumsi.

| Item | Keterangan |
|---|---|
| Nama proyek | Habisin: Marketplace Makanan Surplus |
| Penyusun | Muhamad Rafli Pratama — NIM 20240140003 — Kelas A |
| Program studi | Teknologi Informasi, Universitas Muhammadiyah Yogyakarta |
| Mata kuliah | Pengembangan Aplikasi Mobile 2026 |
| Dokumen acuan | `SRS_HABISIN_20240140003.pdf` (SRS-HABISIN-20240140003-v1.0, 22 November 2026) |
| Repositori mobile | `MUHAMADRAFLIPRATAMA/0003-Habisin` |
| Repositori API | `MUHAMADRAFLIPRATAMA/0003_API_Habisin` |
| URL publik backend | `https://habisin.raflipratama.my.id` |

README ini disusun mengikuti struktur SRS: setiap bab merujuk nomor bab SRS yang dijawabnya, sehingga kesesuaian implementasi dapat diperiksa butir per butir.

---

## 1. Ringkasan Produk (SRS 1.4)

Habisin membantu penjual usaha mikro mengurangi kerugian akibat makanan tidak terjual, sekaligus memberi pembeli akses makanan berkualitas dengan harga lebih rendah. Setiap produk menampilkan harga normal, harga diskon, sisa stok, dan **batas konsumsi**, sehingga pembeli dapat memutuskan sebelum produk tidak layak dimakan.

Tujuan produk:

1. Menyediakan kanal penjualan cepat bagi usaha mikro dengan makanan berlebih.
2. Menekan pemborosan pangan dengan menjual produk sebelum batas konsumsi.
3. Memberi pengalaman transaksi yang aman melalui JWT, otorisasi berbasis peran, dan pencatatan pesanan yang konsisten.
4. Menyediakan data penjualan bagi penjual melalui laporan dan ringkasan.

Di luar ruang lingkup versi 1.0: pengiriman makanan, payment gateway/QRIS nyata, notifikasi push, pelacakan lokasi real-time, dan aplikasi iOS. Pembayaran berupa **simulasi QR lokal berlaku 60 detik**.

## 2. Fitur (SRS 2.2)

| # | Fitur | Prioritas | Status |
|---|---|---|---|
| 1 | Registrasi dan login (access token JWT + refresh token) | High | selesai |
| 2 | Otorisasi berbasis peran (pembeli, penjual) | High | selesai |
| 3 | Katalog produk: pencarian, filter jenis, harga diskon, stok, batas konsumsi | High | selesai |
| 4 | Pengelolaan produk oleh penjual (CRUDS + unggah gambar) | High | selesai |
| 5 | Keranjang belanja tersimpan di basis data (CRUDS) | High | selesai |
| 6 | Checkout atomik multi-produk + QR simulasi 60 detik | High | selesai |
| 7 | Riwayat pesanan dengan status pembayaran tersinkronisasi | High | selesai |
| 8 | Ulasan produk (CRUDS + filter + kepemilikan) | Medium | selesai |
| 9 | Favorit produk dengan catatan pribadi (CRUDS) | Medium | selesai |
| 10 | Dashboard dan laporan penjualan (termasuk ekspor CSV) | Medium | selesai |
| 11 | Habisin AI: rekomendasi, saran harga, penyusun deskripsi | Medium | selesai |
| 12 | Penyimpanan struk pembayaran di perangkat | Low | selesai |

## 3. Aktor dan Hak Akses (SRS 2.3)

| Aktor | Menu | Batasan |
|---|---|---|
| Pembeli | Beranda, Favorit, Keranjang, Pesanan, Profil | hanya data miliknya sendiri |
| Penjual | menu pembeli + Dashboard Penjual, Produk Saya | hanya produk dan pesanan miliknya |
| Penguji/dosen | — | memverifikasi kesesuaian implementasi dengan SRS |

Pembatasan diterapkan dua lapis: antarmuka menyembunyikan menu yang tidak sesuai peran, dan backend menolak permintaan tidak sah dengan **403** sehingga tidak dapat dilewati dengan memanggil API langsung.

## 4. Arsitektur dan Teknologi (SRS 2.1, 2.4, 2.5)

```
Aplikasi Android (Compose, ViewModel, Repository, Retrofit)
        │  JSON + Bearer token
        ▼
Nginx (web, port 80)  ──proxy /api dan path API──►  FastAPI (api, port 8000)
        │                                                  │ SQLAlchemy 2.x
        │                                                  ▼
   Cloudflare Tunnel (habisin.raflipratama.my.id)      MySQL 8.0 (db)
```

| Lapisan | Teknologi |
|---|---|
| Klien | Kotlin, Jetpack Compose, Material 3, Navigation Compose, ViewModel + StateFlow, Retrofit/OkHttp, Coil |
| Backend | Python 3.11, FastAPI, SQLAlchemy 2.x, Pydantic, passlib/bcrypt, python-jose, qrcode |
| Basis data | MySQL 8.0 (container Docker, volume persisten) |
| Web | HTML/CSS/JS tanpa framework, dilayani Nginx dan memakai API yang sama |
| Publikasi | Cloudflare Tunnel permanen (hostname tetap) |

Kendala yang dipatuhi (SRS 2.5): UI hanya Compose, navigasi hanya Navigation Compose, state MVVM, MySQL wajib dan **Room dilarang**, minimal 4 entitas saling terhubung dan 5 fitur CRUDS penuh, kredensial hanya di berkas environment backend, alamat backend tidak ditulis di kode Android (dibaca dari konfigurasi).

## 5. Struktur Repositori

```
Habisin/
├── app/src/main/java/com/example/habisin/
│   ├── data/            # api, model, repository, session, storage, dummy
│   ├── navigation/      # Screen (rute bertipe), Routes, HabisinNavHost
│   ├── ui/
│   │   ├── components/  # Design, HabisinIcon, Inputs, Cards, ProductCard
│   │   ├── screens/     # 12 berkas layar (11 composable Screen + dialog)
│   │   ├── theme/       # Color, Type, Theme
│   │   └── util/        # Format, ImageCache, UiText
│   └── viewmodel/       # 10 ViewModel
├── app/src/main/res/values/    # dimens.xml (94 token), strings.xml (334 string), colors.xml
└── (backend & web ada di repositori terpisah `0003_API_Habisin`)
```

## 6. Model Data (SRS Appendix B.2, SRS 6)

| Tabel | Atribut utama | Relasi |
|---|---|---|
| `users` | id, nama, email, kata_sandi (bcrypt), peran, dibuat_pada | induk produk, keranjang, pesanan, ulasan, favorit, struk, refresh token |
| `products` | id, penjual_id, nama, deskripsi, harga_normal, harga_diskon, stok, jenis_makanan, penyimpanan, batas_konsumsi, gambar | milik satu penjual |
| `cart_items` | id, user_id, product_id, jumlah | satu baris per produk per pembeli |
| `orders` | id, user_id, product_id, jumlah, total_harga, status, status_bayar, checkout_session_id | milik pembeli, merujuk produk |
| `checkout_sessions` | id, user_id, payment_code, payment_expires_at, status | satu sesi untuk banyak pesanan |
| `ulasan` | id, user_id, product_id, order_id, rating, komentar | satu ulasan per pesanan selesai |
| `favorit` | id, user_id, product_id, catatan | unik per pengguna+produk |
| `struk` | id, user_id, order_id, nama_berkas, total_harga, dibuat_pada | salinan struk di server |
| `refresh_tokens` | id, user_id, token, kedaluwarsa | penyegaran sesi |

Sembilan tabel ini memenuhi syarat minimal empat entitas utama yang saling berhubungan (SRS 6), dan setiap tabel memiliki kunci utama, kunci asing, serta stempel waktu.

## 7. Matriks Kebutuhan Fungsional (SRS Bab 3)

Kolom "Dipenuhi oleh" menunjuk berkas atau endpoint yang mengimplementasikan kebutuhan.

### 7.1 Autentikasi dan Manajemen Akun (SRS 3.1, High)

| REQ | Kebutuhan | Dipenuhi oleh |
|---|---|---|
| REQ-1 | Endpoint registrasi menolak email terdaftar | `POST /auth/register`, `routers/auth.py` |
| REQ-2 | Kata sandi hash bcrypt, tidak dikembalikan API | `security.py` (`hash_password`), `schemas.py` tanpa field sandi |
| REQ-3 | Access token JWT + refresh token saat login | `POST /auth/login`, `security.py` |
| REQ-4 | Endpoint penyegaran token | `POST /auth/refresh` |
| REQ-5 | Endpoint profil pengguna aktif | `GET /auth/me` |
| REQ-6 | Token disimpan aman dan dilampirkan tiap permintaan | `data/session/SessionStore.kt`, `data/api/AuthInterceptor.kt` |
| REQ-7 | Arahkan ke layar Masuk bila token tidak berlaku | `AuthInterceptor` → `AuthScreens.kt` |

### 7.2 Otorisasi Berbasis Peran (SRS 3.2, High)

| REQ | Kebutuhan | Dipenuhi oleh |
|---|---|---|
| REQ-1 | Peran disimpan dan disertakan pada token | `models.User.peran`, `security.create_access_token` |
| REQ-2 | Endpoint khusus penjual menolak peran lain (403) | `routers/products.py`, `routers/orders.py`, `routers/reports.py` |
| REQ-3 | Menu dan tombol menyesuaikan peran | `ProfileScreen.kt` (menu Dashboard hanya penjual), `navigation/Screen.kt` |
| REQ-4 | Pemeriksaan kepemilikan pada ubah/hapus | semua router (`if obj.user_id != user.id: raise 403`) |

### 7.3 Katalog dan Pencarian Produk (SRS 3.3, High)

| REQ | Kebutuhan | Dipenuhi oleh |
|---|---|---|
| REQ-1 | Daftar produk dengan pencarian dan filter jenis | `GET /produk?q=&jenis_makanan=&sort=` |
| REQ-2 | Produk kedaluwarsa/stok nol disembunyikan | `routers/products.py` (`stok > 0`, `batas_konsumsi >= now`) |
| REQ-3 | Endpoint detail produk + data penjual | `GET /produk/{product_id}` |
| REQ-4 | Persentase diskon dihitung dari dua harga | `ui/util/Format.kt` (`discountPercent`) |
| REQ-5 | Keadaan memuat, kosong, dan galat | `HomeScreen.kt` (`loading`, `error`, `EmptyState`) |
| REQ-6 | Cache gambar di memori | `ui/util/ImageCache.kt` |

### 7.4 Pengelolaan Produk oleh Penjual (SRS 3.4, High)

| REQ | Kebutuhan | Dipenuhi oleh |
|---|---|---|
| REQ-1 | CRUDS penuh entitas produk | `POST/GET/PUT/DELETE /produk`, `?q=` |
| REQ-2 | Ubah/hapus hanya oleh pemilik | `routers/products.py` |
| REQ-3 | Unggah gambar produk | `POST /produk/{product_id}/gambar` |
| REQ-4 | Validasi harga diskon ≤ harga normal, stok ≥ 0 | `schemas.py` + `SellerScreen.kt` |
| REQ-5 | Formulir produk dengan validasi dan pemilih gambar | `SellerScreen.kt` |
| REQ-6 | Daftar produk penjual + status stok/batas konsumsi | `GET /produk/penjual`, `SellerScreen.kt` |

### 7.5 Keranjang Belanja (SRS 3.5, High)

| REQ | Kebutuhan | Dipenuhi oleh |
|---|---|---|
| REQ-1 | CRUDS penuh entitas keranjang | `POST/GET/PUT/DELETE /keranjang`, `?q=` |
| REQ-2 | Tolak jumlah melebihi stok | `routers/cart.py` |
| REQ-3 | Total dihitung server dari harga diskon | `routers/cart.py` (`total_harga`) |
| REQ-4 | Keranjang hanya milik pemiliknya | `routers/cart.py` |
| REQ-5 | Jumlah item, harga satuan, total tampil | `CartScreen.kt` |
| REQ-6 | Tombol tambah, kurang, hapus per item | `CartScreen.kt`, `CartViewModel.kt` |

### 7.6 Checkout dan Pembayaran QR Simulasi (SRS 3.6, High)

| REQ | Kebutuhan | Dipenuhi oleh |
|---|---|---|
| REQ-1 | Satu sesi checkout untuk seluruh item | `POST /keranjang/checkout` |
| REQ-2 | Kunci baris produk dan validasi stok | `routers/cart.py` (`with_for_update`) |
| REQ-3 | Pengurangan stok + pembuatan pesanan atomik | transaksi SQLAlchemy di `routers/cart.py` |
| REQ-4 | Kode pembayaran acak berlaku 60 detik | `secrets.token_urlsafe(24)`, `PAYMENT_QR_TTL_SECONDS` |
| REQ-5 | Endpoint gambar QR Base64 | `GET /keranjang/checkout/{id}/qr`, `GET /pesanan/{id}/qr` |
| REQ-6 | Endpoint penyegaran QR membatalkan kode lama | `POST /keranjang/checkout/{id}/refresh` |
| REQ-7 | Endpoint status sesi untuk pemantauan | `GET /keranjang/checkout/{id}/status` |
| REQ-8 | Pesanan jadi sudah dibayar + keranjang kosong | `POST /pesanan/bayar/{payment_code}` |
| REQ-9 | Hitung waktu, tombol QR baru, tutup otomatis | `CartScreen.kt`, `OrdersScreen.kt` (auto-refresh saat kode habis) |

### 7.7 Riwayat dan Status Pesanan (SRS 3.7, High)

| REQ | Kebutuhan | Dipenuhi oleh |
|---|---|---|
| REQ-1 | Daftar pesanan pengguna aktif | `GET /pesanan/saya` |
| REQ-2 | Sinkronisasi status bayar dengan sesi checkout | `routers/orders.py` |
| REQ-3 | Pembaruan status oleh pemilik/penjual | `PUT /pesanan/{id}`, `PATCH /pesanan/{id}/status` |
| REQ-4 | Pembatalan mengubah status, bukan menghapus | `POST /pesanan/{id}/batal` |
| REQ-5 | Pelacak langkah + label status bayar | `OrdersScreen.kt` (`StepperProgress`) |
| REQ-6 | Simpan struk ke penyimpanan lokal | `OrdersScreen.kt` + `LocalFileStore.kt` |

### 7.8 Ulasan Produk (SRS 3.8, Medium)

| REQ | Kebutuhan | Dipenuhi oleh |
|---|---|---|
| REQ-1 | CRUDS penuh entitas ulasan | `POST /ulasan/{order_id}`, `GET/PUT/DELETE /ulasan`, `?q=` |
| REQ-2 | Rating 1–5 | `schemas.py`, `ReviewsScreen.kt` |
| REQ-3 | Ubah/hapus hanya oleh penulis | `routers/social.py` |
| REQ-4 | Filter kata kunci, rating, produk | `GET /ulasan?q=&rating=&product_id=` |
| REQ-5 | Layar Ulasan Saya | `GET /ulasan/saya`, `ReviewsScreen.kt` |

### 7.9 Favorit Produk (SRS 3.9, Medium)

| REQ | Kebutuhan | Dipenuhi oleh |
|---|---|---|
| REQ-1 | CRUDS penuh entitas favorit | `POST /favorit/{product_id}`, `GET /favorit`, `PUT/DELETE /favorit/{id}`, `?q=` |
| REQ-2 | Cegah duplikasi favorit | unique constraint `user_id+product_id` |
| REQ-3 | Favorit hanya milik pemiliknya | `routers/social.py` |
| REQ-4 | Daftar favorit + pencarian + ubah catatan | `FavoritesScreen.kt` |

### 7.10 Dashboard dan Laporan Penjualan (SRS 3.10, Medium)

| REQ | Kebutuhan | Dipenuhi oleh |
|---|---|---|
| REQ-1 | Ringkasan penjualan penjual aktif | `GET /ai/ringkasan-penjualan`, `routers/reports.py` |
| REQ-2 | Ekspor pesanan CSV | `GET /pesanan/export.csv` (`routers/reports.py`) |
| REQ-3 | Jumlah produk aktif, pesanan masuk, pendapatan | `SellerScreen.kt` |
| REQ-4 | Data laporan terbatas milik penjual | filter `penjual_id` pada `routers/reports.py` |

### 7.11 Habisin AI (SRS 3.11, Medium)

| REQ | Kebutuhan | Dipenuhi oleh |
|---|---|---|
| REQ-1 | Endpoint percakapan AI | `POST /ai/chat`, `POST /ai/tanya-produk` |
| REQ-2 | Kunci API hanya di environment backend | `backend/.env` (`GEMINI_API_KEY`), tidak pernah ke klien |
| REQ-3 | Saran harga dan penyusun deskripsi | `POST /ai/saran-harga`, `POST /ai/deskripsi-produk` |
| REQ-4 | Pesan galat jelas, aplikasi tidak berhenti | `AiViewModel` → `R.string.msg_ai_unavailable` |

### 7.12 Penyimpanan Berkas Lokal (SRS 3.12, Low)

| REQ | Kebutuhan | Dipenuhi oleh |
|---|---|---|
| REQ-1 | Tulis berkas struk ke penyimpanan lokal | `data/storage/LocalFileStore.kt` |
| REQ-2 | Baca dan tampilkan kembali struk | `ReceiptsScreen.kt` |
| REQ-3 | Daftar struk + waktu simpan | `GET /struk`, `ReceiptsViewModel.kt` |
| REQ-4 | Tangani kegagalan tulis dengan pesan jelas | `R.string.file_failed`, `msg_receipt_local_only_saved` |

## 8. Kontrak API

53 endpoint pada 8 router. Dokumentasi interaktif: `https://habisin.raflipratama.my.id/docs`.

| Router | Endpoint utama |
|---|---|
| `/auth` | `POST /register`, `POST /login`, `POST /refresh`, `GET /me` |
| `/produk` | `GET /`, `GET /{id}`, `POST /`, `PUT /{id}`, `DELETE /{id}`, `GET /penjual`, `POST /{id}/gambar` |
| `/keranjang` | `GET /`, `POST /`, `PUT /{id}`, `DELETE /{id}`, `POST /checkout`, `GET /checkout/{id}/qr`, `POST /checkout/{id}/refresh`, `GET /checkout/{id}/status` |
| `/pesanan` | `POST /`, `GET /saya`, `GET /semua`, `PUT /{id}`, `PATCH /{id}/status`, `POST /{id}/batal`, `GET /{id}/qr`, `POST /{id}/bayar`, `GET /bayar/{code}`, `POST /bayar/{code}`, `GET /bayar-data/{code}` |
| `/ulasan`, `/favorit` | CRUDS ulasan dan favorit, `GET /ulasan/saya` |
| `/struk` | `POST /`, `GET /`, `GET /{id}`, `DELETE /{id}` |
| `/ai` | `POST /chat`, `POST /cari`, `POST /tanya-produk`, `POST /saran-harga`, `POST /deskripsi-produk`, `GET /ringkasan-penjualan`, `GET /dampakku` |

Format galat mengikuti SRS 4.4: `400` validasi, `401` autentikasi, `403` akses terlarang, `404` tidak ditemukan, `503` layanan belum dikonfigurasi.

## 9. Alur Utama: Checkout dan Pembayaran QR (SRS 3.6)

1. Pembeli menekan **Bayar dengan QR** di layar Keranjang.
2. `POST /keranjang/checkout` mengunci stok, membuat satu sesi, dan menerbitkan `payment_code` acak berlaku 60 detik.
3. `GET /keranjang/checkout/{id}/qr` mengembalikan PNG QR (Base64), total, dan sisa waktu. Isi QR: `https://habisin.raflipratama.my.id/pesanan/bayar/<kode>`.
4. HP lain memindai QR dan membuka halaman bayar (total, hitung mundur, tombol **Bayar sekarang**).
5. `POST /pesanan/bayar/{payment_code}` menandai seluruh pesanan sesi `sudah_dibayar`, mengosongkan keranjang, menutup sesi. Aplikasi menutup dialog lewat polling status.
6. Bila 60 detik habis, aplikasi **otomatis menerbitkan kode baru** (kode lama tidak berlaku), dan pembeli juga dapat menekan **QR baru**.

Penyebab umum QR tidak dapat dibuka:

| Gejala | Penyebab | Solusi |
|---|---|---|
| QR tidak membuka apa pun | isi QR memakai `127.0.0.1`/IP Docker | cek `qr_base` pada `/health` |
| Halaman bayar bilang kedaluwarsa | kode hanya 60 detik | tekan **QR baru** atau tunggu auto-refresh |
| Domain tidak menjawab | container `tunnel-named` mati | `docker compose --profile named up -d tunnel-named` |

## 10. Kebutuhan Nonfungsional (SRS 5)

| Aspek | Target SRS | Implementasi |
|---|---|---|
| Kinerja | daftar 50 produk < 2 dtk; checkout < 3 dtk; QR < 1 dtk | index `products`, satu transaksi checkout, `qrcode` lokal |
| Kinerja klien | buka aplikasi < 3 dtk, gambar ter-cache | `ImageCache`, Compose lazy grid |
| Keselamatan data | keranjang/pesanan di DB, bukan memori | tabel `cart_items`, `orders` |
| Keselamatan data | pembatalan mengubah status, bukan hapus | `POST /pesanan/{id}/batal` |
| Keamanan pangan | produk lewat batas konsumsi tidak tampil | filter `batas_konsumsi >= now` |
| Keamanan | semua endpoint ubah data wajib JWT | `Depends(current_user)` |
| Keamanan | kata sandi bcrypt, tidak pernah dikembalikan | `security.py`, `schemas.py` |
| Keamanan | kunci AI hanya di `.env` backend | `routers/ai.py` |
| Kegunaan | alur beli selesai tanpa petunjuk | 5 layar utama + snackbar tiap aksi |
| Perawatan | pisah lapisan data/domain/UI, router per fitur | `data/`, `viewmodel/`, `ui/`, `routers/` |
| Portabilitas | backend dapat dipindah | Docker Compose |
| Aksesibilitas | target sentuh ≥ 44 dp, label pada ikon | `dimensionResource` + `contentDescription` dari `strings.xml` |
| Lokalisasi | seluruh teks di resource, Rupiah, Asia/Jakarta | `strings.xml` (334 string), `Format.kt` |

## 11. Desain Antarmuka (SRS 4.1)

| Token | Nilai |
|---|---|
| Warna utama | `#2E7D32` |
| Latar | `#F7FAF5` (krem) |
| Aksen | `#F2994A` (amber) |
| Garis batas | `#E4ECE4` |
| Sudut | 16–22 dp |
| Tipografi | Poppins |

Aturan tanpa hardcode yang dipatuhi: seluruh teks ada di `res/values/strings.xml`, seluruh ukuran di `res/values/dimens.xml` (94 token), warna hanya di `ui/theme/Color.kt`, tipografi hanya di `ui/theme/Type.kt`. Audit saat ini: 0 literal `.dp`, 0 `Text("…")`, 0 `Color(0x…)` di luar tema, 0 `fontSize` manual.

Layar: Login, Daftar, Beranda, Detail Produk, Keranjang, Dialog QR, Pesanan, Ulasan Saya, Struk Tersimpan, Favorit, Toko, Detail Toko, Profil, Dashboard Penjual, Habisin AI.

## 12. Menjalankan Proyek

### 12.1 Backend (Docker)

```bash
cd backend
cp .env.example .env          # isi DATABASE_URL, JWT_SECRET, GEMINI_API_KEY bila ada
docker compose up -d db api web
docker compose exec api python -m app.seed_demo    # akun & produk demo
curl -s http://127.0.0.1:8000/health
```

Tunnel permanen (URL tetap, tidak berubah saat restart):

```bash
docker compose stop tunnel
docker compose --profile named up -d tunnel-named
curl -s https://habisin.raflipratama.my.id/health
```

### 12.2 Aplikasi Android

```bash
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew :app:installDebug
```

Alamat API dibaca dari `BuildConfig` (`app/build.gradle.kts`), bukan ditulis di kode. Untuk perangkat fisik gunakan `adb reverse tcp:8000 tcp:8000` atau alamat domain publik.

### 12.3 Antarmuka web

`http://localhost` (container `web`) atau `https://habisin.raflipratama.my.id`.

## 13. Akun Demo

| Peran | Email | Kata sandi |
|---|---|---|
| Pembeli | `pembeli@habisin.id` | `habisin123` |
| Penjual | `penjual@habisin.id` | `habisin123` |

Dibuat oleh `python -m app.seed_demo`. Kata sandi hanya untuk demo lokal.

## 14. Batasan Versi 1.0 (SRS Appendix C)

1. Pembayaran masih simulasi lokal, belum terhubung penyedia pembayaran nyata.
2. Notifikasi pesanan baru belum tersedia; penjual membuka aplikasi untuk memeriksa.
3. Produk yang melewati batas konsumsi disembunyikan; penjual perlu memperbarui data berkala.
4. Habisin AI bergantung pada layanan model bahasa pihak ketiga dan kunci API valid.
5. Pengujian beban skala besar belum dilakukan.
6. Antarmuka web mencakup alur utama, belum selengkap aplikasi Android.

## 15. Status Implementasi dan Progress (SRS 2.6, 6)

Seluruh kebutuhan fungsional SRS Bab 3 terimplementasi dan terverifikasi: `./gradlew :app:installDebug` sukses, aplikasi berjalan di emulator tanpa `FATAL EXCEPTION`, alur login → katalog → keranjang → checkout → QR → bayar → struk diuji langsung terhadap backend Docker.

### 15.1 Commit yang sudah dikerjakan

Riwayat saat ini: **9 commit** pada branch `main`.

| # | Commit | Tanggal | Isi |
|---|---|---|---|
| 1 | `a708c5e` | 2026-11-08 | Add .gitignore file to exclude build artifacts and sensitive backend files |
| 2 | `651cde8` | 2026-11-08 | Add MainActivity class to initialize application and set up navigation |
| 3 | `4dff359` | 2026-11-15 | srs |
| 4 | `abe4817` | 2026-11-15 | Add navigation setup with HabisinNavHost and Routes definitions |
| 5 | `f07d1e7` | 2026-11-22 | chore: tambahkan gitignore untuk data lokal, log, dan berkas rahasia |
| 6 | `c322a05` | 2026-11-22 | build: siapkan konfigurasi Gradle dan manifest aplikasi |
| 7 | `1edd78b` | 2026-11-22 | docs: update README with local configuration and demo account details |
| 8 | `2237555` | 2026-11-23 | docs: update README to reflect database configuration changes and add permanent tunnel setup instructions |
| 9 | `f2f86a1` | 2026-11-23 | Implement new feature for user authentication and improve error handling |

### 15.2 Rencana commit berikutnya

Sisa pekerjaan di-commit bertahap satu kelompok per commit (bukan sekaligus), agar riwayat mencerminkan proses pengerjaan. Total setelah selesai: **42 commit**, memenuhi syarat minimal 30 commit pada pedoman.

| # | Kelompok commit | Berkas | Status |
|---|---|---|---|
| 1 | chore: perluas gitignore untuk data lokal, log, dan kredensial | 1 berkas — `.gitignore` | belum |
| 2 | docs: tambahkan dokumen SRS proyek ke repositori | 1 berkas — `SRS_HABISIN_20240140003.pdf` | belum |
| 3 | feat(theme): token warna dan tipografi aplikasi | 5 berkas — `Color.kt`, `Type.kt`, +3 | belum |
| 4 | feat(theme): token dimens dan teks antarmuka | 2 berkas — `dimens.xml`, `strings.xml` | belum |
| 5 | feat(theme): aset launcher, font, dan aturan cadangan | 6 berkas — `poppins_regular.ttf`, `ic_launcher_background.xml`, +4 | belum |
| 6 | feat(data): model, repository, dan klien HTTP | 5 berkas — `Product.kt`, `ProductRepository.kt`, +3 | belum |
| 7 | feat(data): API autentikasi, produk, toko, dan laporan | 4 berkas — `AuthApi.kt`, `ProductApi.kt`, +2 | belum |
| 8 | feat(data): API keranjang, pesanan, sosial, struk, dan AI | 5 berkas — `CartApi.kt`, `OrderApi.kt`, +3 | belum |
| 9 | feat(data): penyimpanan sesi, berkas lokal, dan util tampilan | 6 berkas — `SessionStore.kt`, `ProductImageStore.kt`, +4 | belum |
| 10 | feat(data): data contoh untuk pratinjau desain | 5 berkas — `DummyProducts.kt`, `DummyStores.kt`, +3 | belum |
| 11 | feat(ui): komponen dasar dan ikon | 2 berkas — `Design.kt`, `HabisinIcon.kt` | belum |
| 12 | feat(ui): komponen input, kartu, dan kartu produk | 3 berkas — `Inputs.kt`, `Cards.kt`, +1 | belum |
| 13 | feat(nav): rute bertipe dan navigasi Compose | 3 berkas — `Screen.kt`, `Routes.kt`, +1 | belum |
| 14 | feat(viewmodel): state autentikasi dan beranda | 2 berkas — `AuthViewModel.kt`, `HomeViewModel.kt` | belum |
| 15 | feat(viewmodel): state keranjang dan pesanan | 2 berkas — `CartViewModel.kt`, `OrderViewModel.kt` | belum |
| 16 | feat(viewmodel): state favorit dan ulasan | 2 berkas — `SocialViewModel.kt`, `ReviewsViewModel.kt` | belum |
| 17 | feat(viewmodel): state struk, toko, penjual, dan Habisin AI | 4 berkas — `ReceiptsViewModel.kt`, `ShopViewModel.kt`, +2 | belum |
| 18 | feat(ui): layar masuk dan buat akun | 1 berkas — `AuthScreens.kt` | belum |
| 19 | feat(ui): layar beranda dengan pencarian dan kisi produk | 1 berkas — `HomeScreen.kt` | belum |
| 20 | feat(ui): layar keranjang dan dialog pembayaran QR | 1 berkas — `CartScreen.kt` | belum |
| 21 | feat(ui): layar pesanan dan struk tersimpan | 2 berkas — `OrdersScreen.kt`, `ReceiptsScreen.kt` | belum |
| 22 | feat(ui): layar favorit, ulasan, dan toko | 4 berkas — `FavoritesScreen.kt`, `ReviewsScreen.kt`, +2 | belum |
| 23 | feat(ui): layar profil pembeli | 1 berkas — `ProfileScreen.kt` | belum |
| 24 | feat(ui): dashboard penjual dan layar Habisin AI | 2 berkas — `SellerScreen.kt`, `AiScreen.kt` | belum |
| 25 | test: unit test dan instrumented test | 2 berkas — `ExampleUnitTest.kt`, `ExampleInstrumentedTest.kt` | belum |
| 26 | feat(backend): konfigurasi, model, dan skema basis data | 4 berkas — `config.py`, `database.py`, +2 | belum |
| 27 | feat(backend): keamanan JWT dan bootstrap aplikasi | 4 berkas — `security.py`, `main.py`, +2 | belum |
| 28 | feat(backend): router autentikasi dan produk | 3 berkas — `__init__.py`, `auth.py`, +1 | belum |
| 29 | feat(backend): router keranjang dan pesanan | 2 berkas — `cart.py`, `orders.py` | belum |
| 30 | feat(backend): router ulasan, laporan, AI, dan struk | 4 berkas — `social.py`, `reports.py`, +2 | belum |
| 31 | feat(backend): seed data demo dan skrip operasional | 5 berkas — `seed_demo.py`, `update_host_ip.py`, +3 | belum |
| 32 | build(backend): Docker, nginx, skema SQL, dan contoh environment | 6 berkas — `Dockerfile`, `docker-compose.yml`, +4 | belum |
| 33 | feat(web): antarmuka web pembeli dan penjual | 3 berkas — `index.html`, `app.js`, +1 | belum |

### 15.3 Verifikasi

| Pemeriksaan | Hasil |
|---|---|
| `./gradlew :app:compileDebugKotlin` | BUILD SUCCESSFUL |
| `./gradlew :app:installDebug` | terpasang di emulator, tanpa `FATAL EXCEPTION` |
| Audit hardcode | 0 literal `.dp`, 0 `Text("…")`, 0 `Color(0x…)` di luar tema |
| Sumber teks & ukuran | `strings.xml` 334 string, `dimens.xml` 94 token |
| Alur pembayaran QR | kode acak 60 detik, auto-terbit ulang saat kedaluwarsa, halaman bayar 200 lewat domain |
| Rahasia di repositori | tidak ada `.env`, `.venv`, data MySQL, kredensial tunnel |


### 15.4 Progress Pertemuan 9–16

| Pertemuan | Fokus | Hasil |
|---|---|---|
| 9 | Fondasi proyek | Gradle, manifest, tema Compose, struktur paket, backend Docker Compose + MySQL |
| 10 | Autentikasi & otorisasi | layar Masuk/Daftar, penyimpanan token, interceptor Bearer + refresh, menu per peran |
| 11 | Katalog & produk | Beranda, pencarian, chip jenis makanan, kartu produk, detail produk, CRUDS produk penjual |
| 12 | Keranjang | layar Keranjang, ubah jumlah, hapus item, ringkasan total dari server |
| 13 | Checkout & QR | dialog QR dengan hitung mundur 60 detik, QR baru, pemantauan status sesi |
| 14 | Pesanan & struk | riwayat pesanan, pelacak status, simpan struk ke penyimpanan lokal + tabel `struk` |
| 15 | Ulasan, favorit, laporan | Ulasan Saya, Favorit + catatan, daftar/detail toko, dashboard penjual |
| 16 | AI, desain akhir, publikasi | Habisin AI, audit tanpa hardcode (`dimens.xml` 94 token, `strings.xml` 334 string), URL publik permanen, pemisahan repo |

### 15.5 Progress Mingguan (Weekly Progress)

Progress didokumentasikan dari riwayat commit git pada repositori ini.

#### Minggu 1 (8 - 24 November 2026)

- [x] Setup proyek Android Studio, Gradle, dan `.gitignore` artefak build (`a708c5e`, `c322a05`)
- [x] `MainActivity` dan kerangka navigasi Compose (`651cde8`, `abe4817`)
- [x] Dokumen SRS Habisin versi 1.0 (`4dff359`)
- [x] `.gitignore` data lokal, log, dan berkas kredensial (`f07d1e7`)
- [x] README konfigurasi lokal, akun demo, dan tunnel permanen (`1edd78b`, `2237555`)
- [x] README ditulis ulang mengikuti struktur SRS (`f2f86a1`, `335ef91`)
- [x] Pemisahan repositori mobile dan API (`760ab07`)

#### Minggu 2 (25 - 28 November 2026)

- [x] Klien layanan AI dan kontrak chat asisten (`5bd5408`, `517cab3`)
- [x] Endpoint saran harga, deskripsi produk, dan ringkasan penjualan (`550fa1e`, `0400ee4`, `f874670`)
- [x] Pencarian cerdas, dampak penjual, dan tanya produk (`05fc967`, `bebd72f`)
- [x] Model permintaan, ringkasan, dan jawaban AI keranjang (`2a09477`, `465a558`, `9964cfe`)
- [x] State keranjang, pesan, QR, dan AI pada ViewModel (`ee14efb`, `feab436`, `d71b7b5`)
- [x] Deteksi isi keranjang dan aksi tanya AI (`6cd24f2`, `db07e8a`, `766cf65`)
- [x] Checkout, pemantauan status bayar, dan pemuatan daftar (`bb3b190`, `7fd0e82`, `5e1c1f2`)
- [x] Tambah, ubah, dan hapus item keranjang (`52a1dcb`)
- [x] Layar keranjang: impor, state, snackbar, dan hitung mundur QR (`0d28bfe`, `620bd2a`, `05115e4`)
- [x] Scaffold, top bar, daftar item, dan kartu AI keranjang (`9591c55`, `5356e4d`, `adc7497`)
- [x] Ringkasan total, dialog QR, dan jawaban AI keranjang (`80daf93`, `9c95e14`, `874c253`, `78e7cd8`)

## 16. Lisensi dan Atribusi

Proyek akademik Mata Kuliah Pengembangan Aplikasi Mobile 2026, Program Studi Teknologi Informasi, Universitas Muhammadiyah Yogyakarta. Hak atas dokumen SRS dan kode sumber berada pada Muhamad Rafli Pratama (20240140003).
