# SIATA

(*TEMPORARY SELAMA DEVELOPMENT*)  
Branching Workflow

## 1. Struktur Branch
```
main -> versi produksi / stabil
(fitur) -> tempat kerja per fitur untuk meminimalisir merge conflict
```

Satu branch bisa digunakan oleh dua orang (frontend dan backend). Foldernya dipisah antara frontend dan backend untuk meminimalisir merge conflict.

## 2. Alur Kerja

#### Set user.email dan user.name
Pakai Git Bash
```
git config user.email "you@example.com"
git config user.name "Your Name"
```

Ini adalah identitas yang tertulis saat melakukan commit.


#### CLONE REPO KE LOCAL (HANYA DILAKUKAN PERTAMA KALI)
Pakai Git Bash
```
cd (mis: C:/xampp/htdocs) -> folder untuk menyimpan project ini
git clone () -> download project ini ke folder di atas, kalau berhasil akan muncul folder baru
cd (folder-hasil-clone) -> pindah ke folder hasil clone
```

#### CARA MULAI KERJA DI SUATU BRANCH
Buka folder yang berhasil diclone di Intellij IDEA. Buka terminalnya Intellij atau Git Bash (opsional)
```
git checkout (mis: dashboard) -> pindah ke suatu branch untuk dipull atau diambil jika ada update di branch tersebut.
git pull -> sangat disarankan agar working directory kita selalu up-to-date dan tidak terjadi error saat commit.
```

Setelah di merge, testing di aplikasinya, jika ada bug, perbaiki dahulu sebelum di push ke main

#### PUSH KE MAIN
Siapkan folder yang ingin di push.
Melakukan commit dan push ke remote repo.
```
git add .
git commit -m (message)
git push
```