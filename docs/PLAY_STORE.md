# Persiapan publikasi berbayar

Project ini menyiapkan produk awal dan AAB bertanda tangan; publikasi dan persetujuan Google Play merupakan langkah terpisah.

## Listing awal

- Nama: **Game Optimizer PRO**
- Deskripsi singkat: **Siapkan sesi gaming, pantau perangkat, dan kelola pustaka game pribadi.**
- Deskripsi: Game Optimizer PRO membantu Anda menyiapkan sesi bermain melalui dashboard kondisi perangkat, pustaka game, profil rekomendasi per game, dan riwayat sesi manual. Pantau RAM tersedia, daya dan suhu baterai, penyimpanan, serta status jaringan. Akses pengaturan layar, Wi-Fi, baterai, dan Jangan Ganggu langsung dari satu tempat. Tanpa iklan, akun, atau pelacak. Aplikasi tidak mengubah FPS game, menambah RAM, atau melakukan overclock. Performa bergantung pada perangkat, game, dan pengaturan yang Anda pilih.
- Kategori yang disarankan: Tools.
- Model penjualan: aplikasi berbayar di Play Console; tidak ada pembelian dalam aplikasi.

## Teks listing 24 bahasa

Teks deskripsi singkat dan lengkap untuk semua bahasa aplikasi tersedia di [katalog listing](../localization/play_store.json). Batas karakter telah diperiksa: deskripsi singkat maksimal 80 karakter dan deskripsi lengkap maksimal 4.000 karakter pada setiap bahasa. Terjemahan dibuat berdasarkan fitur produk; mintalah penutur asli meninjau teks untuk negara tujuan sebelum listing dipublikasikan.

## Sebelum mengirim ke review

1. Tentukan harga, negara distribusi, identitas pengembang, dan alamat email dukungan di Play Console. Jangan terbitkan sebagai gratis jika rencananya menjual aplikasi yang sama sebagai berbayar; periksa aturan monetisasi saat publikasi.
2. Aktifkan Play App Signing. Gunakan JKS project sebagai upload key dan simpan cadangan terenkripsi di lokasi aman.
3. Unduh AAB release dari Actions. Tingkatkan `versionCode` pada setiap unggahan baru.
4. Host [kebijakan privasi HTML](../privacy-policy.html) pada URL publik yang stabil. Verifikasi nama pengembang dan kontak sudah cocok dengan akun Play Console; dokumen ini perlu dipublikasikan lewat hosting statis sebelum URL dimasukkan ke Play Console.
5. Isi Data Safety sesuai perilaku build final: tidak ada pengumpulan/pembagian data oleh aplikasi saat ini. Pemrosesan hanya lokal. Tinjau kembali bila menambah SDK.
6. Lengkapi deklarasi iklan (tidak ada), rating konten, target audiens, akses aplikasi, dan pertanyaan kebijakan lain.
7. Gunakan [ikon aplikasi 512×512](../docs/store-assets/play-icon-512.png) dan [gambar fitur global 1024×500](../docs/store-assets/feature-graphic-1024x500.png). Versi Indonesia tersedia di [feature graphic Bahasa Indonesia](../docs/store-assets/feature-graphic-id-1024x500.png). Keduanya PNG di bawah batas 15 MB. Aset dapat dibuat ulang melalui `scripts/New-StoreAssets.ps1`.
8. Uji di perangkat fisik berbagai merek, termasuk Android 8 dan Android 16, mode layar besar, font besar, offline, dan mode hemat daya. Jalankan closed testing bila diwajibkan untuk akun Anda.
9. Pastikan persyaratan API, verifikasi akun, pengujian, dan kebijakan yang berlaku saat pengiriman telah dipenuhi. Tidak ada jaminan otomatis lolos review.

Rujukan: https://support.google.com/googleplay/android-developer/answer/11926878
