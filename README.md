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
git clone "https://git.stis.ac.id/kelompok-6-rpl/siata.git" -> download project ini dan tempatkan ke folder di atas, kalau berhasil akan muncul folder baru
cd siata -> pindah ke folder hasil clone
```

#### CARA MULAI KERJA DI SUATU BRANCH
Buka folder yang berhasil diclone di Intellij IDEA. Buka terminalnya Intellij atau Git Bash (opsional)

##### Memastikan working directory kita up-to-date, disarankan untuk melakukan ini setiap mulai kerja.
```
git checkout (mis: dashboard) -> pindah ke suatu branch untuk dipull atau diambil jika ada update di branch tersebut.
git pull -> sangat disarankan agar working directory kita selalu up-to-date dan tidak terjadi error saat commit.
(jika ada conflict, resolve dengan manual)
```

##### Setiap selesai kerja, disarankan melakukan ini.
```
(pastikan branch sesuai)
git pull
(jika ada conflict, resolve dengan manual)
git add .
git commit -m "(apa yang dikerjakan)"
git push
```

Kenapa tidak wajib dan hanya disarankan? Karena itu dilakukan untuk meminimalkan merge conflict (dimana working directorymu tidak sesuai dengan apa yang ada di remote repository, alhasil apa yang kamu kerjakan tidak relevan) dan tidak error saat melakukan push (karena jika local repository tidak sesuai dengan remote repository, tidak akan bisa dipush)

#### Kalau fiturnya udah jadi (kerjaan di branch tersebut udah final dan tidak ada bug), lakukan ini.
Ini untuk menggabungkan branch kita ke branch main (aplikasi yang udah stable)
```
git checkout main
git pull
(jika ada conflict, resolve dengan manual)
git add .
git commit -m "(apa yang dikerjakan)"
git push
```