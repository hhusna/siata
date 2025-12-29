# SIADA

**SIATA** (System Informasi Aset) adalah aplikasi manajemen aset berbasis Java yang terdiri dari modul Server (Spring Boot) dan Client (JavaFX) untuk mengelola siklus hidup aset mulai dari pengadaan, pemeliharaan, hingga penghapusan. 

## Fitur Utama
- **Multi-Role**: Dukungan role (Tim Manajemen Aset, PPBJ, PPK, Direktur) dengan hak akses granular.
- **Manajemen Aset Lengkap**: CRUD, Soft Delete, Riwayat Log, dan Pelacakan Kondisi.
- **Workflow Persetujuan**: Alur pengajuan dan persetujuan aset berjenjang.
- **Laporan Dinamis**: Cetak laporan PDF/Excel.
- **Audit Log**: Pencatatan otomatis setiap aktivitas perubahan data.
- **Database Versioning**: Menggunakan **Flyway** untuk migrasi database yang aman.

## Teknologi
Daftar teknologi inti yang digunakan.
- **Server**: Java 21, Spring Boot 3, Spring Data JPA, Hibernate, MySQL, Flyway.
- **Client**: Java 21, JavaFX, Unirest (HTTP Client).
- **Security**: Spring Security + JWT, BCrypt Hashing.
- **Tools**: Maven, WiX Toolset (untuk installer).

## Video Penggunaan Aplikasi
[Lihat Video Penggunaan Aplikasi](https://drive.google.com/file/d/1UI6nXFA9GUctvg068zMPBg0ROm4L3PJc/view?usp=drive_link)
<p align="center">
  <a href="https://drive.google.com/file/d/1UI6nXFA9GUctvg068zMPBg0ROm4L3PJc/view?usp=drive_link">
    <img src="dokumentasi/thumbnail.png" width="240" alt="Demo Video">
  </a>
</p>

## Struktur Folder
project-root/  
├─ client/  
├─ server/  
├─ dokumentasi/  
├─ laporan_proyek/ (berisi BAST, Alih Guna Sistem, Laporan Milestone 4, PPT Milestone 4, dan Petunjuk Instalasi)  
├─ .gitignore  
└─ README.md  

## Lisensi dan Laporan
- Petunjuk Instalasi  
[Petunjuk_Instalasi_SIADA.pdf](laporan_proyek/Petunjuk_Instalasi_SIADA.pdf)
- Berita Acara Serah Terima (BAST)
- Alih Guna Sistem
- Laporan Milestone 4  
[Laporan Milestone 4.pdf](laporan_proyek/_RPL__Kelompok_6_-_Laporan_Akhir.pdf)  
[PPT Milestone 4.pdf](laporan_proyek/_RPL__Kelompok_6_-_Presentasi_Akhir.pdf)

## Dokumentasi
Silakan baca panduan berikut di folder `laporan_proyek/`:
1.  **[Panduan Instalasi & Deployment](laporan_proyek/Panduan_Instalasi_SIADA.pdf)**
    - Cara install di Localhost.
    - Cara deploy ke VPS Linux (Ubuntu/Debian).
    - Cara build Client dan membuat Installer Windows (.exe/.msi).
    - Cara konfigurasi koneksi Client (`config.properties`).

2.  **[Panduan Pengguna (User Manual)](laporan_proyek/Panduan_Penggunaan_SIADA.pdf)**
    - Panduan penggunaan fitur berdasarkan Role.
    - Daftar akun default untuk login.

## Quick Start (Localhost)
1.  **Server**:
    ```bash
    cd server
    mvn spring-boot:run
    ```
    *Database akan otomatis dibuat dan diisi data dummy (Seeding).*

2.  **Client**:
    ```bash
    cd client
    mvn clean package
    java -jar target/siata-client-1.0-SNAPSHOT.jar
    ```

## Akun Default
Password untuk semua akun di bawah adalah: `password123`
| Role | Username |
|---|---|
| **Admin Aset** | `admin_aset` |
| **PPBJ** | `staff_ppbj` |
| **PPK** | `staff_ppk` |
| **Direktur** | `direktur` |