# SIATA

(*TEMPORARY SELAMA DEVELOPMENT*)
Branching Workflow

## 1. Struktur Branch
```
main -> versi produksi / stabil
develop -> versi pengembangan (tempat integrasi semua fitur)
```

## 2. Penamaan Branch
```
(namafitur) -> branch untuk develop Backend
(namafitur)_ui -> branch untuk develop Frontend
```

## 3. Alur Kerja

#### CLONE REPO KE LOCAL (HANYA DILAKUKAN PERTAMA KALI)
Pakai git bash terus cd ke folder yang diinginkan.
Clone repository ini.
Untuk bekerja di suatu branch, tidak perlu membuat branch baru, langsung checkout ke branch yang tersedia di remote repository.

```
git clone (url)
cd (folder-hasil-clone)
git checkout (namabranch)
```


#### MERGE FRONTEND DAN BACKEND
Karena suatu fitur memiliki branch yang berbeda antara frontend dan backend, maka perlu dilakukan merge agar kode yang ditulis up-to-date.

Nama branch yang ingin dimerge: kalau sedang mengerjakan frontend, maka merge backend, dan sebaliknya
```
git pull
git checkout (nama-branch-yang-sedang-dikerjakan)
git merge (nama-branch-yang-ingin-dimerge)
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