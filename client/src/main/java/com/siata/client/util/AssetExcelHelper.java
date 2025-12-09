package com.siata.client.util;

import com.siata.client.model.Asset;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Utility class for reading and writing Excel files for Asset data.
 */
public class AssetExcelHelper {

    private static final String[] HEADERS = {
        "Kode Aset", "No Aset", "Jenis Aset", "Merk Barang", "NIP Pemegang", 
        "Subdirektorat", "Tanggal Perolehan", "Nilai Rupiah", "Kondisi", "Status"
    };

    /**
     * Parse an Excel file and return a list of Assets.
     */
    public static List<Asset> parseExcel(File file) throws IOException {
        List<Asset> assets = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            boolean isFirstRow = true;

            for (Row row : sheet) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }

                if (row == null) continue;

                try {
                    String kodeAset = getCellValueAsString(row.getCell(0));
                    Integer noAset = getCellValueAsInteger(row.getCell(1));
                    String jenisAset = getCellValueAsString(row.getCell(2));
                    String merkBarang = getCellValueAsString(row.getCell(3));
                    String nipPemegang = getCellValueAsString(row.getCell(4));
                    String subdir = getCellValueAsString(row.getCell(5));
                    LocalDate tanggal = getCellValueAsDate(row.getCell(6));
                    BigDecimal nilai = getCellValueAsBigDecimal(row.getCell(7));
                    String kondisi = getCellValueAsString(row.getCell(8));
                    String status = getCellValueAsString(row.getCell(9));
                    
                    // Auto-calculate "Dipakai" status based on NIP or Subdir
                    // Logic: IF (NIP is present OR Subdir is present) -> TRUE, ELSE -> FALSE
                    String dipakai = "FALSE";
                    // Validation logic for auto-dipakai happens after we clean NIP and Subdir below

                    // Cleaning and validations...
                    if (kodeAset == null || kodeAset.trim().isEmpty()) continue;
                    kodeAset = kodeAset.replaceAll("\\.0$", "").trim();
                    if (jenisAset == null) jenisAset = "";
                    if (merkBarang == null) merkBarang = "";
                    if (nipPemegang == null) nipPemegang = "";
                    else nipPemegang = nipPemegang.replaceAll("\\s+", ""); // Remove spaces
                    
                    if (subdir == null) subdir = "";
                    if (kondisi == null || kondisi.isEmpty()) kondisi = "Baik";
                    // Normalize status to AKTIF or NONAKTIF
                    if (status == null || status.isEmpty()) {
                        status = "AKTIF";
                    } else {
                        String upper = status.trim().toUpperCase().replace(" ", "");
                        status = upper.contains("NON") || upper.equals("NONAKTIF") || upper.equals("INACTIVE") ? "NONAKTIF" : "AKTIF";
                    }
                    if (tanggal == null) tanggal = LocalDate.now();
                    if (nilai == null) nilai = BigDecimal.ZERO;

                    Asset asset = new Asset();
                    asset.setKodeAset(kodeAset);
                    asset.setNoAset(noAset);
                    asset.setJenisAset(jenisAset);
                    asset.setMerkBarang(merkBarang);
                    asset.setKeterangan(nipPemegang); // NIP
                    asset.setSubdir(subdir);
                    asset.setTanggalPerolehan(tanggal);
                    asset.setNilaiRupiah(nilai);
                    asset.setKondisi(kondisi);
                    asset.setStatus(status);
                    asset.setStatus(status);
                    
                    // Final auto-calc logic for Dipakai
                    boolean isUsed = (nipPemegang != null && !nipPemegang.isEmpty()) || (subdir != null && !subdir.isEmpty());
                    asset.setDipakai(isUsed ? "TRUE" : "FALSE");
                    
                    assets.add(asset);

                } catch (Exception e) {
                    System.err.println("Error parsing row " + (row.getRowNum() + 1) + ": " + e.getMessage());
                }
            }
        }
        return assets;
    }

    /**
     * Export a list of Assets to an Excel file.
     */
    public static void exportToExcel(List<Asset> assets, File file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Data Aset");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Create header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data style
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // Date style
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.cloneStyleFrom(dataStyle);
            dateStyle.setDataFormat(workbook.createDataFormat().getFormat("dd/MM/yyyy"));

            // Create data rows
            int rowNum = 1;
            for (Asset asset : assets) {
                Row row = sheet.createRow(rowNum++);

                createCell(row, 0, asset.getKodeAset(), dataStyle);
                createCell(row, 1, asset.getNoAset() != null ? String.valueOf(asset.getNoAset()) : "", dataStyle);
                createCell(row, 2, asset.getJenisAset(), dataStyle);
                createCell(row, 3, asset.getMerkBarang(), dataStyle);
                createCell(row, 4, asset.getKeterangan(), dataStyle);
                createCell(row, 5, asset.getSubdir(), dataStyle);
                
                Cell dateCell = row.createCell(6);
                if (asset.getTanggalPerolehan() != null) {
                    dateCell.setCellValue(Date.from(asset.getTanggalPerolehan()
                        .atStartOfDay(ZoneId.systemDefault()).toInstant()));
                }
                dateCell.setCellStyle(dateStyle);

                createCell(row, 7, asset.getNilaiRupiah() != null ? asset.getNilaiRupiah().toString() : "0", dataStyle);
                createCell(row, 8, asset.getKondisi(), dataStyle);
                createCell(row, 9, asset.getStatus(), dataStyle);
            }

            // Write to file
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
        }
    }

    /**
     * Create an empty Excel template with headers only.
     */
    public static void createTemplate(File file) throws IOException {
        exportToExcel(new ArrayList<>(), file);
    }

    private static void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }
                double numValue = cell.getNumericCellValue();
                if (numValue == (long) numValue) {
                    return String.valueOf((long) numValue);
                }
                return String.valueOf(numValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return null;
        }
    }

    private static LocalDate getCellValueAsDate(Cell cell) {
        if (cell == null) return null;

        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            } else if (cell.getCellType() == CellType.STRING) {
                String dateStr = cell.getStringCellValue();
                if (dateStr != null && !dateStr.isEmpty()) {
                    return LocalDate.parse(dateStr);
                }
            }
        } catch (Exception e) {
            // Ignore parse errors
        }
        return null;
    }

    private static BigDecimal getCellValueAsBigDecimal(Cell cell) {
        if (cell == null) return BigDecimal.ZERO;

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return BigDecimal.valueOf(cell.getNumericCellValue());
            } else if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue().replaceAll("[^\\d.]", "");
                if (!value.isEmpty()) {
                    return new BigDecimal(value);
                }
            }
        } catch (Exception e) {
            // Ignore parse errors
        }
        return BigDecimal.ZERO;
    }

    private static Integer getCellValueAsInteger(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                String val = cell.getStringCellValue().replaceAll("[^\\d]", "");
                if (!val.isEmpty()) {
                    return Integer.parseInt(val);
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }
}
