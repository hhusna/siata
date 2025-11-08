# SIATA

(*TEMPORARY SELAMA DEVELOPMENT*)  
## Branching Workflow

---

## 1. Struktur Branch

```
main     -> versi produksi / stabil  
dev  -> tempat testing fitur
```

Silahkan buat branch sendiri jika memang diperlukan lalu push ke remote repository untuk dibuat upstreamnya.

Gunakan perintah berikut untuk melihat daftar branch yang ada di remote repository:
```bash
git branch -r
```


> Satu branch dapat digunakan oleh dua orang (frontend dan backend).  
> Folder frontend dan backend **dipisah** agar meminimalkan merge conflict.  
> Frontend = Mayoritas isinya adalah JavaFX, tidak menggunakan Spring Boot.  
> Backend = Mayoritas isinya adalah Web Service, menggunakan Spring Boot. 

---

## 2. Alur Kerja

### Set Identitas Git
Lakukan satu kali untuk mengatur nama dan email commit:
```bash
git config user.email "you@example.com"
git config user.name "Your Name"
```

---

### Clone Repository (Hanya Pertama Kali)
```bash
cd C:/xampp/htdocs           # contoh folder tempat menyimpan project
git clone https://git.stis.ac.id/kelompok-6-rpl/siata.git
cd siata
```

---

### Mulai Bekerja di Suatu Branch
Buka folder hasil clone di IntelliJ IDEA.  
Gunakan terminal di IntelliJ atau Git Bash.

#### Pastikan Working Directory Selalu Up-to-Date
```bash
git checkout (nama-branch)   # contoh: git checkout dashboard
git pull                     # ambil update terbaru dari branch tersebut jika tidak up-to-date
# jika ada conflict, resolve manual
```

---

### Setelah Selesai Bekerja
```bash
# pastikan berada di branch yang benar
git pull                     # ambil update terbaru sebelum commit jika tidak up to date
# jika ada conflict, resolve manual
git add .
git commit -m "deskripsi pekerjaan"
git push
```

> Langkah `git pull` tidak wajib, tapi **sangat disarankan** untuk menghindari merge conflict dan error saat push.

---

### Jika Fitur Sudah Selesai dan Stabil
Lakukan merge ke branch `main`:
```bash
git checkout main
git pull
git merge (nama-branch)
# jika ada conflict, resolve manual
git add .
git commit -m "merge fitur X ke main"
git push
```

---

## Catatan
- Selalu **pastikan branch lokal up-to-date** sebelum bekerja.  
- **Jangan commit langsung ke main** kecuali untuk rilis versi stabil.  
- Untuk tiap fitur baru, buat branch baru agar pengembangan terpisah dan lebih aman.

---