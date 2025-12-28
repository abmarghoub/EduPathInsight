package ens.edupath.ingestion.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CSVParserService {

    public List<Map<String, String>> parseCSV(MultipartFile file) throws IOException, CsvException {
        List<Map<String, String>> records = new ArrayList<>();

        try (CSVReader reader = new CSVReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            List<String[]> allRows = reader.readAll();
            
            if (allRows.isEmpty()) {
                return records;
            }

            // Première ligne = headers
            String[] headers = allRows.get(0);
            
            // Normaliser les headers (trim, lowercase, replace spaces)
            String[] normalizedHeaders = normalizeHeaders(headers);

            // Lignes de données
            for (int i = 1; i < allRows.size(); i++) {
                String[] row = allRows.get(i);
                Map<String, String> record = createRecord(normalizedHeaders, row);
                if (!record.isEmpty()) {
                    records.add(record);
                }
            }
        }

        return records;
    }

    private String[] normalizeHeaders(String[] headers) {
        String[] normalized = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            normalized[i] = headers[i].trim()
                    .toLowerCase()
                    .replaceAll("\\s+", "_")
                    .replaceAll("[^a-z0-9_]", "");
        }
        return normalized;
    }

    private Map<String, String> createRecord(String[] headers, String[] values) {
        Map<String, String> record = new java.util.HashMap<>();
        int maxLength = Math.min(headers.length, values.length);
        
        for (int i = 0; i < maxLength; i++) {
            String key = headers[i];
            String value = values[i] != null ? values[i].trim() : "";
            if (!key.isEmpty() && !value.isEmpty()) {
                record.put(key, value);
            }
        }
        
        return record;
    }
}


