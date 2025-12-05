package com.siata.client.util;

import com.siata.client.model.Employee;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for reading and writing Excel files for Employee data.
 */
public class ExcelHelper {

    private static final String[] HEADERS = {"Nama", "NIP", "Subdirektorat", "Status"};

    /**
     * Parse an Excel file and return a list of Employees.
     * Expected format: Column A = NIP (optional for PPNPN), Column B = Nama, Column C = Subdirektorat
     * NIP spaces will be removed automatically.
     */
    public static List<Employee> parseExcel(File file) throws IOException {
        List<Employee> employees = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            boolean isFirstRow = true;

            for (Row row : sheet) {
                // Skip header row
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }

                // Skip completely empty rows
                if (row == null) {
                    continue;
                }

                try {
                    // Column order: Nama (A), NIP (B), Subdirektorat (C), Status (D)
                    String nama = getCellValueAsString(row.getCell(0));
                    String nip = getCellValueAsString(row.getCell(1));
                    String subdir = getCellValueAsString(row.getCell(2));
                    String status = getCellValueAsString(row.getCell(3));

                    // Skip rows with empty Nama (Nama is required)
                    if (nama == null || nama.trim().isEmpty()) {
                        continue;
                    }

                    // Clean NIP - remove spaces, decimal points, and any non-digit characters
                    if (nip != null) {
                        nip = nip.replaceAll("\\s+", "")  // Remove all spaces
                                 .replaceAll("\\.0$", "") // Remove decimal .0
                                 .trim();
                        // If NIP becomes empty after cleaning, set to empty string (PPNPN)
                        if (nip.isEmpty()) {
                            nip = "";
                        }
                    } else {
                        nip = ""; // PPNPN - no NIP
                    }

                    // Default status to AKTIF if empty
                    if (status == null || status.trim().isEmpty()) {
                        status = "AKTIF";
                    } else {
                        status = status.trim().toUpperCase();
                        if (!"AKTIF".equals(status) && !"NONAKTIF".equals(status)) {
                            status = "AKTIF";
                        }
                    }

                    Employee employee = new Employee(nip, nama.trim(), subdir != null ? subdir.trim() : "", status);
                    employees.add(employee);
                } catch (Exception e) {
                    // Skip invalid rows
                    System.err.println("Skipping invalid row: " + e.getMessage());
                }
            }
        }

        return employees;
    }

    /**
     * Export a list of Employees to an Excel file.
     */
    public static void exportToExcel(List<Employee> employees, File file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Data Pegawai");

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Create header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // Create data style
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // Create data rows - Column order: Nama (A), NIP (B), Subdirektorat (C), Status (D)
            int rowNum = 1;
            for (Employee emp : employees) {
                Row row = sheet.createRow(rowNum++);

                Cell namaCell = row.createCell(0);
                namaCell.setCellValue(emp.getNamaLengkap());
                namaCell.setCellStyle(dataStyle);

                Cell nipCell = row.createCell(1);
                nipCell.setCellValue(emp.getNip());
                nipCell.setCellStyle(dataStyle);

                Cell subdirCell = row.createCell(2);
                subdirCell.setCellValue(emp.getUnit());
                subdirCell.setCellStyle(dataStyle);

                Cell statusCell = row.createCell(3);
                statusCell.setCellValue(emp.getStatus() != null ? emp.getStatus() : "AKTIF");
                statusCell.setCellStyle(dataStyle);
            }

            // Auto-size columns
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
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

    /**
     * Get cell value as String, handling different cell types.
     */
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                // Handle numeric NIP - format without scientific notation
                double numValue = cell.getNumericCellValue();
                if (numValue == Math.floor(numValue)) {
                    return String.format("%.0f", numValue);
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
}
