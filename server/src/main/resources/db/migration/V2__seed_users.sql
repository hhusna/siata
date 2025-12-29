-- Seed Pegawai
INSERT INTO pegawai (nip, nama, nama_subdir, is_ppnpn, status) VALUES 
(1001, 'Admin Aset', 'Manajemen Aset', 0, 'AKTIF'),
(1002, 'Staff PPBJ', 'Pengadaan', 0, 'AKTIF'),
(1003, 'Staff PPK', 'Keuangan', 0, 'AKTIF'),
(1004, 'Pak Direktur', 'Direktorat', 0, 'AKTIF');

-- Seed Users (Password: password123)
-- Hash: $2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6
INSERT INTO user (username, password, role, nip_pegawai) VALUES 
('admin_aset', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'TIM_MANAJEMEN_ASET', 1001),
('staff_ppbj', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'PPBJ', 1002),
('staff_ppk', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'PPK', 1003),
('direktur', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'DIREKTUR', 1004);
