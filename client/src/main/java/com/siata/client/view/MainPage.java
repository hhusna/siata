package com.siata.client.view;

public enum MainPage {
    DASHBOARD("🏠", "Dashboard", "Selasa, 7 Oktober 2025", "Ringkasan sistem distribusi aset pegawai", null),
    RECAPITULATION("📊", "Rekapitulasi & Matriks", "Rabu, 8 Oktober 2025", "Ringkasan dan matriks distribusi aset", null),
    ASSET_MANAGEMENT("📦", "Manajemen Aset", "Sabtu, 11 Oktober 2025", "Kelola data aset pegawai", null),
    EMPLOYEE_MANAGEMENT("👥", "Manajemen Pegawai", "Rabu, 8 Oktober 2025", "Kelola data pegawai dan aset yang dimiliki", null),
    ASSET_REQUEST("📝", "Pengajuan Aset", "Rabu, 8 Oktober 2025", "Catat dan kelola permohonan & pengajuan aset pegawai", null),
    ASSET_APPROVAL("✅", "Persetujuan Aset", "Rabu, 8 Oktober 2025", "Kelola permohonan dan pengajuan aset dari pegawai", null),
    ASSET_REMOVAL("🗑", "Penghapusan Aset", "Rabu, 8 Oktober 2025", "Kelola aset yang akan dihapus dari sistem", null),
    LOGBOOK("📘", "Logbook", "Rabu, 8 Oktober 2025", "Riwayat semua aktivitas dalam sistem", null);

    private final String icon;
    private final String title;
    private final String dateLabel;
    private final String description;
    private final String badgeText;

    MainPage(String icon, String title, String dateLabel, String description, String badgeText) {
        this.icon = icon;
        this.title = title;
        this.dateLabel = dateLabel;
        this.description = description;
        this.badgeText = badgeText;
    }

    public String icon() {
        return icon;
    }

    public String title() {
        return title;
    }

    public String dateLabel() {
        return dateLabel;
    }

    public String description() {
        return description;
    }

    public String badgeText() {
        return badgeText;
    }
}

