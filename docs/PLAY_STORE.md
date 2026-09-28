# Persiapan publikasi berbayar

Project ini menyiapkan produk awal dan AAB bertanda tangan; publikasi dan persetujuan Google Play merupakan langkah terpisah.

## Listing awal

- Nama: **Game Optimizer PRO**
- Deskripsi singkat: **Siapkan sesi gaming, pantau perangkat, dan kelola pustaka game pribadi.**
- Deskripsi: Game Optimizer PRO membantu Anda menyiapkan sesi bermain melalui dashboard kondisi perangkat, pustaka game, profil rekomendasi per game, dan riwayat sesi manual. Pantau RAM tersedia, daya dan suhu baterai, penyimpanan, serta status jaringan. Akses pengaturan layar, Wi-Fi, baterai, dan Jangan Ganggu langsung dari satu tempat. Tanpa iklan, akun, atau pelacak. Aplikasi tidak mengubah FPS game, menambah RAM, atau melakukan overclock. Performa bergantung pada perangkat, game, dan pengaturan yang Anda pilih.
- Kategori yang disarankan: Tools.
- Model penjualan: aplikasi berbayar di Play Console; tidak ada pembelian dalam aplikasi.

## Sebelum mengirim ke review

1. Tentukan harga, negara distribusi, identitas pengembang, dan alamat email dukungan di Play Console. Jangan terbitkan sebagai gratis jika rencananya menjual aplikasi yang sama sebagai berbayar; periksa aturan monetisasi saat publikasi.
2. Aktifkan Play App Signing. Gunakan JKS project sebagai upload key dan simpan cadangan terenkripsi di lokasi aman.
3. Unduh AAB release dari Actions. Tingkatkan `versionCode` pada setiap unggahan baru.
4. Host kebijakan privasi pada URL publik yang stabil. Verifikasi nama pengembang dan kontak sudah cocok dengan akun Play Console.
5. Isi Data Safety sesuai perilaku build final: tidak ada pengumpulan/pembagian data oleh aplikasi saat ini. Pemrosesan hanya lokal. Tinjau kembali bila menambah SDK.
6. Lengkapi deklarasi iklan (tidak ada), rating konten, target audiens, akses aplikasi, dan pertanyaan kebijakan lain.
7. Ikon listing PNG 512×512 dan feature graphic 1024×500 tersedia di `docs/store-assets`. Screenshot nyata emulator dihasilkan oleh job UI dalam artifact `device-test-reports`; tinjau sebelum dipakai untuk listing. Aset dapat dibuat ulang melalui `scripts/New-StoreAssets.ps1`.
8. Uji di perangkat fisik berbagai merek, termasuk Android 8 dan Android 16, mode layar besar, font besar, offline, dan mode hemat daya. Jalankan closed testing bila diwajibkan untuk akun Anda.
9. Pastikan persyaratan API, verifikasi akun, pengujian, dan kebijakan yang berlaku saat pengiriman telah dipenuhi. Tidak ada jaminan otomatis lolos review.

Rujukan: https://support.google.com/googleplay/android-developer/answer/11926878
