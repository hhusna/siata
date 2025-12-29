# Panduan Penggunaan Sistem Informasi Manajemen Aset

Dokumen ini berisi panduan penggunaan aplikasi berdasarkan Role pengguna.

## Login
Akses halaman login dan masukkan kredensial berikut:

| Role | Username | Password Default |
|---|---|---|
| **Tim Manajemen Aset** | `admin_aset` | `password123` |
| **PPBJ** | `staff_ppbj` | `password123` |
| **PPK** | `staff_ppk` | `password123` |
| **Direktur** | `direktur` | `password123` |

---

## 1. Role: Tim Manajemen Aset (User: admin_aset)
Role ini memiliki akses penuh untuk mengelola data aset, pegawai, dan permohonan.

### Fitur Utama:
- **Dashboard**: Melihat ringkasan aset, total aktif, rusak, dan perlu peremajaan.
- **Manajemen Aset**:
  - **Tambah Aset**: Input data aset baru (kode, nama, harga, kondisi, pemegang).
  - **Edit/Hapus Aset**: Memperbarui informasi atau menghapus aset (termasuk Soft Delete).
  - **Riwayat Log**: Melihat sejarah perubahan pada setiap aset.
- **Manajemen Pegawai**: Menambah dan mengelola data pegawai serta menghubungkannya dengan User.
- **Permohonan & Pengajuan Aset**: Memproses permintaan aset dari pegawai lain.
- **Penghapusan Aset**: Mengelola daftar aset yang akan dihapus/dilelang.
- **Laporan**: Mencetak laporan aset per kategori atau unit.

---

## 2. Role: PPBJ (User: staff_ppbj)
Pejabat Pengadaan Barang/Jasa bertugas memverifikasi pengadaan aset baru.

### Fitur Utama:
- **Verifikasi Pengajuan**: Menerima notifikasi pengajuan aset baru dan melakukan verifikasi spesifikasi serta harga.
- **Input Data Pengadaan**: Memasukkan data vendor dan detail kontrak pengadaan.
- **Status Pengadaan**: Memantau proses pengadaan dari awal hingga barang diterima.

---

## 3. Role: PPK (User: staff_ppk)
Pejabat Pembuat Komitmen bertugas menyetujui anggaran dan pembelian.

### Fitur Utama:
- **Persetujuan Anggaran**: Menyetujui atau menolak pengajuan berdasarkan ketersediaan anggaran.
- **Validasi Pembayaran**: Memvalidasi dokumen pembayaran untuk aset yang telah diadakan.
- **Monitoring Kontrak**: Memantau pelaksanaan kontrak pengadaan.

---

## 4. Role: Direktur (User: direktur)
Pimpinan tertinggi yang memantau keseluruhan aset dan memberikan persetujuan akhir untuk hal-hal strategis.

### Fitur Utama:
- **Dashboard Eksekutif**: Ringkasan aset bernilai tinggi dan status anggaran.
- **Persetujuan Akhir**: Memberikan persetujuan untuk penghapusan aset bernilai besar atau pengadaan strategis.
- **Laporan Statistik**: Melihat tren pengadaan dan performa manajemen aset.
