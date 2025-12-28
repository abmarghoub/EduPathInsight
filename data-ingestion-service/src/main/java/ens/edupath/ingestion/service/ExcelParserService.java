package ens.edupath.ingestion.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExcelParserService {

    public List<Map<String, String>> parseExcel(MultipartFile file) throws IOException {
        List<Map<String, String>> records = new ArrayList<>();
        Workbook workbook = null;

        try {
            String filename = file.getOriginalFilename();
            if (filename != null && filename.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(file.getInputStream());
            } else if (filename != null && filename.endsWith(".xls")) {
                workbook = new HSSFWorkbook(file.getInputStream());
            } else {
                throw new IOException("Format Excel non supporté");
            }

            Sheet sheet = workbook.getSheetAt(0); // Première feuille

            if (sheet.getPhysicalNumberOfRows() == 0) {
                return records;
            }

            // Première ligne = headers
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return records;
            }

            String[] headers = extractHeaders(headerRow);

            // Lignes de données
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                Map<String, String> record = createRecord(headers, row);
                if (!record.isEmpty()) {
                    records.add(record);
                }
            }

        } finally {
            if (workbook != null) {
                workbook.close();
            }
        }

        return records;
    }

    private String[] extractHeaders(Row headerRow) {
        List<String> headers = new ArrayList<>();
        for (Cell cell : headerRow) {
            String headerValue = getCellValueAsString(cell);
            headers.add(normalizeHeader(headerValue));
        }
        return headers.toArray(new String[0]);
    }

    private String normalizeHeader(String header) {
        return header.trim()
                .toLowerCase()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-z0-9_]", "");
    }

    private Map<String, String> createRecord(String[] headers, Row row) {
        Map<String, String> record = new HashMap<>();
        
        for (int i = 0; i < headers.length && i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            String value = getCellValueAsString(cell);
            String key = headers[i];
            
            if (!key.isEmpty() && !value.isEmpty()) {
                record.put(key, value);
            }
        }
        
        return record;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // Éviter les nombres avec .0 à la fin
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}


