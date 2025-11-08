package siata.siata.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import siata.siata.entity.Aset;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class LaporanService {

    @Autowired
    private AsetService asetService;

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