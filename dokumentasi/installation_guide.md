# Panduan Instalasi Sistem Informasi Manajemen Aset

Dokumen ini menjelaskan langkah-langkah instalasi aplikasi untuk lingkungan pengembangan lokal (Localhost) dan server produksi (VPS Linux).

## Prasyarat
- **Java Development Kit (JDK) 21**
- **Maven 3.9+**
- **MySQL 8.0+**
- **Git**
- **WiX Toolset v3.11+** (Khusus untuk membuat installer Windows .msi/.exe)

---

## 1. Instalasi Localhost (Windows/Linux/Mac)

### 1.1 Persiapan Database
1. Pastikan MySQL Server sudah berjalan.
2. Buat database baru bernama `siata`:
   ```sql
   CREATE DATABASE siata;
   ```
3. Pastikan user root bisa mengakses, atau sesuaikan konfigurasi di `application.properties`.

### 1.2 Konfigurasi Aplikasi (Server)
Buka file `server/src/main/resources/application.properties` dan sesuaikan jika perlu:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/siata?useSSL=false&serverTimezone=Asia/Jakarta
spring.datasource.username=root
spring.datasource.password=
```

### 1.3 Build & Run Server
1. Buka terminal di folder project `server`.
2. Jalankan perintah berikut untuk menjalankan migrasi database otomatis (Flyway) dan memulai aplikasi:
   ```bash
   mvn spring-boot:run
   ```
   *Catatan: Saat pertama kali dijalankan, sistem akan otomatis membuat tabel dan mengisi data awal (seeding).*
3. Aplikasi akan berjalan di `http://localhost:8080`.
4. Swagger UI dapat diakses di `http://localhost:8080/swagger-ui.html` (jika diaktifkan).

---

## 2. Deployment ke VPS Linux (Ubuntu/Debian)

### 2.1 Persiapan Server
1. Update paket:
   ```bash
   sudo apt update && sudo apt upgrade -y
   ```
2. Install Java 21:
   ```bash
   sudo apt install openjdk-21-jdk -y
   ```
3. Install MySQL:
   ```bash
   sudo apt install mysql-server -y
   ```

### 2.2 Setup Database di VPS
Masuk ke MySQL dan buat database:
```bash
sudo mysql
```
```sql
CREATE DATABASE siata;
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'password_db_anda';
FLUSH PRIVILEGES;
EXIT;
```

### 2.3 Build Aplikasi Server
Di komputer lokal atau CI/CD, build file JAR server:
```bash
mvn clean package -DskipTests
```
File JAR akan berada di `target/siata-0.0.1-SNAPSHOT.jar`. Upload file ini ke VPS (misal ke `/opt/siata/app.jar`).

### 2.4 Setup Systemd Service
Buat file service agar aplikasi berjalan di background dan auto-start:
```bash
sudo nano /etc/systemd/system/siata.service
```
Isi dengan konfigurasi berikut:
```ini
[Unit]
Description=SIATA Web Application
After=syslog.target network.target mysql.service

[Service]
User=root
ExecStart=/usr/bin/java -jar /opt/siata/app.jar --spring.datasource.password=password_db_anda
SuccessExitStatus=143
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```
Simpan dan keluar, lalu jalankan:
```bash
sudo systemctl daemon-reload
sudo systemctl enable siata
sudo systemctl start siata
```

### 2.5 Setup Nginx Reverse Proxy (Opsional)
Untuk mengakses via domain (port 80/443) tanpa membuka port 8080:
1. Install Nginx: `sudo apt install nginx`
2. Konfigurasi Nginx sebagai reverse proxy ke `localhost:8080`.
3. Gunakan Certbot untuk SSL gratis.

---

## 3. Instalasi Aplikasi Client (Desktop)

Aplikasi client berbasis Java Swing/JavaFX dan perlu diinstal di komputer masing-masing pengguna (Windows/Mac/Linux).

### 3.1 Konfigurasi Koneksi API
Aplikasi client membaca konfigurasi URL dari file `config.properties` yang berada di folder yang sama dengan aplikasi.

**Langkah Konfigurasi:**
1. Di komputer pengguna, buat file teks baru bernama `config.properties` di folder yang sama dengan file `.jar` aplikasi.
2. Isi file tersebut dengan baris berikut:
   ```properties
   api.base.url=http://ALAMAT_IP_SERVER:8080
   ```
   **Contoh:**
   - Localhost: `api.base.url=http://localhost:8080` (Ini adalah default jika file tidak ada)
   - VPS: `api.base.url=http://103.123.45.67:8080`
   - Domain: `api.base.url=https://siata.domain-anda.com`

*Catatan: Jika file `config.properties` tidak ditemukan, aplikasi akan mencoba terhubung ke `http://localhost:8080` secara default.*

### 3.2 Build Aplikasi Client
Anda hanya perlu melakukan build sekali, dan file `.jar` yang sama bisa digunakan untuk berbagai environment (Dev/Prod) cukup dengan mengganti `config.properties`.

1. Buka terminal di folder project `client`.
2. Jalankan perintah build:
   ```bash
   mvn clean package
   ```
3. File installer/executable akan muncul di folder `target/` dengan nama seperti `siata-client-0.0.1-SNAPSHOT.jar`.

### 3.3 Menjalankan Client
**Untuk Pengguna (End-User):**
- Pastikan komputer pengguna sudah terinstall **Java Runtime Environment (JRE) 21+**.
- Copy file `.jar` dan (opsional) file `config.properties` ke satu folder di komputer pengguna.
- Jalankan dengan double-click atau via command line:
  ```bash
  java -jar siata-client.jar
  ```

---

## 4. Membuat Installer Windows (.exe / .msi)
Untuk mendistribusikan aplikasi lebih mudah (seperti aplikasi Windows native yang ada di Start Menu), Anda bisa menggunakan tool `jpackage` yang sudah ada di JDK 21.

### 4.1 Prasyarat
1. **JDK 21** sudah terinstall.
2. **WiX Toolset (v3.11 atau terbaru)** harus terinstall di Windows Anda.

### 4.2 Langkah Pembuatan
1. Pastikan Anda sudah menjalankan `mvn clean package` dan file `.jar` ada di folder `target/`.
2. Buka terminal di folder `client`.
3. Jalankan perintah `jpackage` berikut (sesuaikan nama file jar):

   **Untuk membuat Installer .msi:**
   ```powershell
   jpackage --input target \
     --name "SIATA Client" \
     --main-jar siata-client-1.0-SNAPSHOT.jar \
     --main-class com.siata.client.Launcher \
     --type msi \
     --win-dir-chooser \
     --win-menu \
     --win-shortcut \
     --app-version 1.0.0 \
     --vendor "Tim SIATA" \
     --dest installer
   ```

   **Untuk membuat Installer .exe:**
   Ganti `--type msi` menjadi `--type exe`.

4. File installer yang dihasilkan akan ada di folder `installer/`. File ini sudah mem-bundle Java Runtime, sehingga user tidak perlu install Java lagi.

---

## 5. Strategi Distribusi Paling Mudah (Newbie-Friendly)

Jika target pengguna Anda adalah orang awam yang tidak mengerti cara edit file konfigurasi, ikuti langkah berikut agar mereka tinggal "Next-Next-Finish".

### Langkah untuk Administrator (AAnda):
1. **Set URL Server sebelum Build**:
   Edit file `client/src/main/resources/config.properties` (buat jika belum ada) atau edit kode default di `ApiConfig.java`. Masukkan URL server VPS yang sudah fix.
   ```properties
   api.base.url=https://siata.domain-anda.com
   ```
   *Pastikan URL ini sudah benar dan bisa diakses dari internet.*

2. **Build JAR & Installer**:
   Jalankan `mvn clean package` lalu `jpackage ...` (seperti langkah 4.2).

3. **Distribusi File Installer**:
   Anda akan mendapatkan satu file `.msi` (misal `SIATA-Client-1.0.msi`). Kirimkan **HANYA** file ini ke klien.

### Pengalaman Pengguna (Client):
1. Terima file `.msi`.
2. Double-click file tersebut.
3. Ikuti wizard instalasi (Next -> Finish).
4. Selesai! Aplikasi langsung terhubung ke server tanpa perlu konfigurasi apapun.
