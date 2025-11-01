package com.example.nodo_final.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle; // 1. Import
import org.apache.poi.ss.usermodel.DataFormat; // 2. Import
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class ExcelHelper {

    public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    static String[] HEADERS = { "ID", "Tên", "Mã", "Mô tả", "Ngày tạo", "Ngày sửa", "Người tạo", "Người sửa" };
    static String SHEET = "Categories";

    public static ByteArrayInputStream categoriesToExcel(List<Object[]> categories) {

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream();) {

            Sheet sheet = workbook.createSheet(SHEET);

            // --- (BƯỚC SỬA 1) ---
            // Tạo một CellStyle cho ngày tháng
            CellStyle dateCellStyle = workbook.createCellStyle();
            DataFormat dataFormat = workbook.createDataFormat();
            dateCellStyle.setDataFormat(dataFormat.getFormat("dd/MM/yyyy"));
            // --- (HẾT BƯỚC SỬA 1) ---

            // 1. Tạo hàng Header
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < HEADERS.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(HEADERS[col]);
            }

            // 2. Tạo các hàng Data
            int rowIdx = 1;
            for (Object[] rowData : categories) {
                Row row = sheet.createRow(rowIdx++);

                // Loop qua 8 cột (Object[])
                for(int i = 0; i < rowData.length; i++) {
                    Cell cell = row.createCell(i); // Tạo ô
                    Object value = rowData[i];

                    if (value instanceof Long) {
                        cell.setCellValue((Long) value);
                    } else if (value instanceof String) {
                        cell.setCellValue((String) value);

                        // --- (BƯỚC SỬA 2) ---
                    } else if (value instanceof Date) {
                        cell.setCellValue((Date) value); //
                        cell.setCellStyle(dateCellStyle); // Áp dụng style
                        // --- (HẾT BƯỚC SỬA 2) ---

                    } else {
                        cell.setCellValue("");
                    }
                }
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi xuất file Excel: " + e.getMessage());
        }
    }
}