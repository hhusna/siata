package siata.siata.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import siata.siata.entity.Aset;
import siata.siata.entity.Pegawai;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

@Service
public class LaporanService {

    @Autowired
    private AsetService asetService;
    
    @Autowired
    private PegawaiService pegawaiService;

    public ByteArrayInputStream createAsetPdfReport() {
        List<Aset> asetList = asetService.getAllAset();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate()); // Landscape

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Judul
            Font fontJudul = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph judul = new Paragraph("Laporan Rekapitulasi Aset", fontJudul);
            judul.setAlignment(Element.ALIGN_CENTER);
            judul.setSpacingAfter(20);
            document.add(judul);

            // Tabel
            PdfPTable table = new PdfPTable(8); // 8 Kolom
            table.setWidthPercentage(100);
            float[] columnWidths = {2f, 3f, 3f, 3f, 3f, 2f, 2f, 3f}; // Lebar kolom
            table.setWidths(columnWidths);

            // Header Tabel
            addTableHeader(table);

            // Isi Tabel
            for (Aset aset : asetList) {
                addTableRow(table, aset);
            }

            document.add(table);

        } catch (DocumentException e) {
            e.printStackTrace();
        } finally {
            document.close();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
    
    /**
     * Create formal letter PDF for laptop needs notification
     */
    public ByteArrayInputStream createLaptopNeedsLetter() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        document.setMargins(72, 72, 72, 72); // 1 inch margins

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // === KOP SURAT ===
            addLetterhead(document);
            
            // === TANGGAL DAN NOMOR SURAT ===
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("id", "ID"));
            String formattedDate = today.format(formatter);
            
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            
            Paragraph datePlace = new Paragraph("Jakarta, " + formattedDate, normalFont);
            datePlace.setAlignment(Element.ALIGN_RIGHT);
            datePlace.setSpacingAfter(10);
            document.add(datePlace);
            
            // Nomor Surat
            Paragraph nomorSurat = new Paragraph("Nomor: S-____/DT.02.01/" + today.getYear(), normalFont);
            nomorSurat.setSpacingAfter(3);
            document.add(nomorSurat);
            
            Paragraph lampiran = new Paragraph("Lampiran: 1 (satu) berkas", normalFont);
            lampiran.setSpacingAfter(3);
            document.add(lampiran);
            
            Paragraph perihal = new Paragraph("Perihal: Pemberitahuan Kebutuhan Laptop", boldFont);
            perihal.setSpacingAfter(20);
            document.add(perihal);
            
            // === TUJUAN ===
            Paragraph tujuan = new Paragraph("Yth. Kepala Bagian Pengadaan\nDirektorat Jenderal Kekayaan Negara\ndi Tempat", normalFont);
            tujuan.setSpacingAfter(20);
            document.add(tujuan);
            
            // === PEMBUKA ===
            Paragraph pembuka = new Paragraph(
                "Dengan hormat,\n\n" +
                "Sehubungan dengan kebutuhan operasional pegawai di lingkungan kerja kami, " +
                "bersama ini kami sampaikan pemberitahuan mengenai kebutuhan perangkat laptop " +
                "untuk mendukung pelaksanaan tugas dan fungsi secara optimal.",
                normalFont
            );
            pembuka.setSpacingAfter(15);
            pembuka.setAlignment(Element.ALIGN_JUSTIFIED);
            document.add(pembuka);
            
            // === ISI - CALCULATE LAPTOP NEEDS ===
            List<Pegawai> allPegawai = pegawaiService.getAllPegawai();
            List<Aset> allAset = asetService.getAllAset();
            int totalASN = allPegawai.size();
            
            LocalDate now = LocalDate.now();
            long goodLaptops = allAset.stream()
                .filter(a -> a.getJenisAset() != null && a.getJenisAset().equalsIgnoreCase("Laptop"))
                .filter(a -> "AKTIF".equalsIgnoreCase(a.getStatusPemakaian()))
                .filter(a -> a.getKondisi() != null && !a.getKondisi().equalsIgnoreCase("Rusak Berat"))
                .filter(a -> {
                    if (a.getTanggalPerolehan() == null) return false;
                    long days = ChronoUnit.DAYS.between(a.getTanggalPerolehan(), now);
                    double years = days / 365.25;
                    return years < 4;
                })
                .count();
            
            int laptopBaik = (int) goodLaptops;
            int kebutuhan = totalASN - laptopBaik;
            
            Paragraph isi = new Paragraph(
                "Berdasarkan hasil pendataan dan analisis kebutuhan, berikut adalah rincian data:\n\n" +
                "1. Jumlah total pegawai (ASN): " + totalASN + " orang\n" +
                "2. Jumlah laptop dalam kondisi baik: " + laptopBaik + " unit\n" +
                "   (Kriteria: Status Aktif, Kondisi bukan Rusak Berat, Usia < 4 tahun)\n" +
                "3. Kebutuhan laptop: " + (kebutuhan > 0 ? kebutuhan : 0) + " unit\n\n" +
                (kebutuhan > 0 ? 
                    "Dengan demikian, terdapat kekurangan sebanyak " + kebutuhan + " unit laptop " +
                    "yang perlu diadakan untuk memenuhi kebutuhan seluruh pegawai." :
                    "Berdasarkan data tersebut, jumlah laptop yang tersedia sudah mencukupi kebutuhan pegawai."),
                normalFont
            );
            isi.setSpacingAfter(15);
            isi.setAlignment(Element.ALIGN_JUSTIFIED);
            document.add(isi);
            
            // === PENUTUP ===
            Paragraph penutup = new Paragraph(
                "Demikian pemberitahuan ini kami sampaikan untuk dapat ditindaklanjuti sebagaimana mestinya. " +
                "Atas perhatian dan kerjasamanya, kami ucapkan terima kasih.",
                normalFont
            );
            penutup.setSpacingAfter(40);
            penutup.setAlignment(Element.ALIGN_JUSTIFIED);
            document.add(penutup);
            
            // === TANDA TANGAN ===
            Paragraph ttdHeader = new Paragraph("Hormat kami,\nTim Manajemen Aset", normalFont);
            ttdHeader.setAlignment(Element.ALIGN_LEFT);
            ttdHeader.setSpacingAfter(50);
            document.add(ttdHeader);
            
            Paragraph ttdName = new Paragraph("_______________________\nNama: _______________\nNIP: _______________", normalFont);
            ttdName.setAlignment(Element.ALIGN_LEFT);
            document.add(ttdName);

        } catch (DocumentException e) {
            e.printStackTrace();
        } finally {
            document.close();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
    
    /**
     * Add letterhead to the document
     */
    private void addLetterhead(Document document) throws DocumentException {
        // Kop Surat - Institution Header
        Font kopBesar = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font kopKecil = FontFactory.getFont(FontFactory.HELVETICA, 10);
        
        Paragraph kop1 = new Paragraph("KEMENTERIAN PERHUBUNGAN", kopBesar);
        kop1.setAlignment(Element.ALIGN_CENTER);
        document.add(kop1);
        
        Paragraph kop2 = new Paragraph("DIREKTORAT JENDERAL PERHUBUNGAN UDARA", kopBesar);
        kop2.setAlignment(Element.ALIGN_CENTER);
        document.add(kop2);
        
        Paragraph kop3 = new Paragraph("DIREKTORAT ANGKUTAN UDARA", kopBesar);
        kop3.setAlignment(Element.ALIGN_CENTER);
        kop3.setSpacingAfter(5);
        document.add(kop3);
        
        Paragraph alamat = new Paragraph(
            "Gedung Karya Lt. 22\n" +
            "Jl. Medan Merdeka Barat No. 8 Jakarta 10110\n" +
            "Telepon (021) 3505006; Faksimile (021) 3505139\n" +
            "www.hubud.dephub.go.id",
            kopKecil
        );
        alamat.setAlignment(Element.ALIGN_CENTER);
        alamat.setSpacingAfter(5);
        document.add(alamat);
        
        // Garis pembatas
        Paragraph garis = new Paragraph("════════════════════════════════════════════════════════════════════════════");
        garis.setAlignment(Element.ALIGN_CENTER);
        garis.setSpacingAfter(20);
        document.add(garis);
    }

    private void addTableHeader(PdfPTable table) {
        String[] headers = {"ID Aset", "Kode Aset", "Jenis Aset", "Merk", "Tgl Perolehan", "Kondisi", "Status", "Pemegang"};
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }

    private void addTableRow(PdfPTable table, Aset aset) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 9);

        table.addCell(new Phrase(aset.getIdAset().toString(), font));
        table.addCell(new Phrase(aset.getKodeAset(), font));
        table.addCell(new Phrase(aset.getJenisAset(), font));
        table.addCell(new Phrase(aset.getMerkAset() != null ? aset.getMerkAset() : "-", font));
        table.addCell(new Phrase(aset.getTanggalPerolehan() != null ? aset.getTanggalPerolehan().toString() : "-", font));
        table.addCell(new Phrase(aset.getKondisi() != null ? aset.getKondisi() : "-", font));
        table.addCell(new Phrase(aset.getStatusPemakaian() != null ? aset.getStatusPemakaian() : "-", font));
        table.addCell(new Phrase(aset.getPegawai() != null ? aset.getPegawai().getNama() : "Tidak ada", font));
    }
}
