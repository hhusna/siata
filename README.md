# SIATA (Sistem Informasi Manajemen Aset)

**SIATA** (System Informasi Aset) adalah aplikasi manajemen aset berbasis Java yang terdiri dari modul Server (Spring Boot) dan Client (JavaFX) untuk mengelola siklus hidup aset mulai dari pengadaan, pemeliharaan, hingga penghapusan.

## 🚀 Fitur Utama
- **Multi-Role**: Dukungan role (Tim Manajemen Aset, PPBJ, PPK, Direktur) dengan hak akses granular.
- **Manajemen Aset Lengkap**: CRUD, Soft Delete, Riwayat Log, dan Pelacakan Kondisi.
- **Workflow Persetujuan**: Alur pengajuan dan persetujuan aset berjenjang.
- **Laporan Dinamis**: Cetak laporan PDF/Excel.
- **Audit Log**: Pencatatan otomatis setiap aktivitas perubahan data.
- **Database Versioning**: Menggunakan **Flyway** untuk migrasi database yang aman.

## 🛠 Teknologi
- **Server**: Java 21, Spring Boot 3, Spring Data JPA, Hibernate, MySQL, Flyway.
- **Client**: Java 21, JavaFX, Unirest (HTTP Client).
- **Security**: Spring Security + JWT, BCrypt Hashing.
- **Tools**: Maven, WiX Toolset (untuk installer).

## 📚 Dokumentasi Lengkap
Silakan baca panduan berikut di folder `dokumentasi/`:

1.  **[Panduan Instalasi & Deployment](dokumentasi/installation_guide.md)**
    - Cara install di Localhost.
    - Cara deploy ke VPS Linux (Ubuntu/Debian).
    - Cara build Client dan membuat Installer Windows (.exe/.msi).
    - Cara konfigurasi koneksi Client (`config.properties`).

2.  **[Panduan Pengguna (User Manual)](dokumentasi/user_manual.md)**
    - Panduan penggunaan fitur berdasarkan Role.
    - Daftar akun default untuk login.

## 📂 Struktur Project
Berikut adalah gambaran besar struktur folder proyek:

```
siata/
├── client/                     # Modul Client (JavaFX Desktop App)
│   ├── installer/              # Output installer (.msi/.exe)
│   ├── src/main/java/com/siata/client/
│   │   ├── api/                # Komunikasi HTTP ke Server
│   │   ├── component/          # Komponen UI Reusable
│   │   ├── config/             # Konfigurasi Aplikasi (ApiConfig)
│   │   ├── dto/                # Data Transfer Object
│   │   ├── model/              # Model Data Client
│   │   ├── view/               # Tampilan/Page (FXML Controller)
│   │   ├── MainApplication.java
│   │   └── Launcher.java
│   └── src/main/resources/     # Asset (FXML, CSS, Images, Config)
│
├── server/                     # Modul Server (Spring Boot API)
│   ├── src/main/java/siata/siata/
│   │   ├── config/             # Konfigurasi Security, Swagger, dll
│   │   ├── controller/         # Endpoint API
│   │   ├── entity/             # Entity Database (JPA)
│   │   ├── repository/         # Data Access Layer
│   │   ├── security/           # JWT & Auth Logic
│   │   ├── service/            # Business Logic
│   │   └── SiataApplication.java
│   └── src/main/resources/
│       ├── db/migration/       # Skrip Migrasi Flyway (SQL)
│       └── application.properties
│
└── dokumentasi/                # Dokumentasi Lengkap Proyek
    ├── installation_guide.md   # Panduan Instalasi
    └── user_manual.md          # Panduan Penggunaan
```

## ⚡ Quick Start (Localhost)

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

## 🔐 Akun Default
Password untuk semua akun di bawah adalah: `password123`

| Role | Username |
|---|---|
| **Admin Aset** | `admin_aset` |
| **PPBJ** | `staff_ppbj` |
| **PPK** | `staff_ppk` |
| **Direktur** | `direktur` |

---
**Sistem Informasi Aset - Kelompok 6 RPL**