CREATE TABLE pegawai (
    nip BIGINT NOT NULL,
    nama VARCHAR(100) NOT NULL,
    nama_subdir VARCHAR(100),
    is_ppnpn BIT,
    status VARCHAR(20),
    PRIMARY KEY (nip)
);

CREATE TABLE user (
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    simulation_role VARCHAR(50),
    nip_pegawai BIGINT,
    PRIMARY KEY (username),
    UNIQUE (nip_pegawai),
    CONSTRAINT fk_user_pegawai FOREIGN KEY (nip_pegawai) REFERENCES pegawai (nip)
);

CREATE TABLE aset (
    id_aset BIGINT AUTO_INCREMENT NOT NULL,
    kode_aset VARCHAR(100) NOT NULL,
    jenis_aset VARCHAR(100) NOT NULL,
    merk_aset VARCHAR(100),
    tanggal_perolehan DATE,
    harga_aset DECIMAL(20, 2) NOT NULL,
    kondisi VARCHAR(50),
    status_pemakaian VARCHAR(50),
    subdirektorat VARCHAR(100),
    apakah_dihapus INT NOT NULL,
    no_aset INT,
    dipakai VARCHAR(255),
    nip_pegawai BIGINT,
    PRIMARY KEY (id_aset),
    CONSTRAINT fk_aset_pegawai FOREIGN KEY (nip_pegawai) REFERENCES pegawai (nip)
);

CREATE TABLE permohonan_aset (
    id_permohonan BIGINT AUTO_INCREMENT NOT NULL,
    kode_permohonan VARCHAR(100),
    nip BIGINT,
    nip_pengguna VARCHAR(100),
    subdirektorat_pengguna VARCHAR(100),
    jenis_aset VARCHAR(100),
    jumlah INT,
    deskripsi TEXT,
    tujuan_penggunaan TEXT,
    status_persetujuan VARCHAR(50),
    timestamp DATE NOT NULL,
    PRIMARY KEY (id_permohonan),
    CONSTRAINT fk_permohonan_pegawai FOREIGN KEY (nip) REFERENCES pegawai (nip)
);

CREATE TABLE pengajuan_aset (
    id_pengajuan BIGINT AUTO_INCREMENT NOT NULL,
    kode_pengajuan VARCHAR(100),
    nip BIGINT,
    nama_pengaju VARCHAR(100),
    unit VARCHAR(100),
    jenis_aset VARCHAR(100),
    jumlah INT,
    deskripsi TEXT,
    tujuan_penggunaan TEXT,
    prioritas VARCHAR(50),
    status_persetujuan VARCHAR(50),
    timestamp DATE NOT NULL,
    PRIMARY KEY (id_pengajuan),
    CONSTRAINT fk_pengajuan_pegawai FOREIGN KEY (nip) REFERENCES pegawai (nip)
);

CREATE TABLE penghapusan_aset (
    id_penghapusan BIGINT AUTO_INCREMENT NOT NULL,
    id_aset BIGINT,
    nama_aset VARCHAR(100) NOT NULL,
    jenis_aset VARCHAR(100),
    tanggal_perolehan DATE,
    harga_aset DECIMAL(15, 2),
    kondisi VARCHAR(50),
    PRIMARY KEY (id_penghapusan),
    UNIQUE (id_aset),
    CONSTRAINT fk_penghapusan_aset FOREIGN KEY (id_aset) REFERENCES aset (id_aset)
);

CREATE TABLE log_riwayat (
    id_log BIGINT AUTO_INCREMENT NOT NULL,
    id_permohonan BIGINT,
    id_pengajuan BIGINT,
    id_aset BIGINT,
    nip_pegawai BIGINT,
    jenis_log VARCHAR(100),
    isi_log TEXT,
    timestamp DATETIME(6) NOT NULL,
    catatan TEXT,
    lampiran VARCHAR(500),
    PRIMARY KEY (id_log),
    CONSTRAINT fk_log_permohonan FOREIGN KEY (id_permohonan) REFERENCES permohonan_aset (id_permohonan),
    CONSTRAINT fk_log_pengajuan FOREIGN KEY (id_pengajuan) REFERENCES pengajuan_aset (id_pengajuan),
    CONSTRAINT fk_log_aset FOREIGN KEY (id_aset) REFERENCES aset (id_aset),
    CONSTRAINT fk_log_pegawai FOREIGN KEY (nip_pegawai) REFERENCES pegawai (nip)
);
