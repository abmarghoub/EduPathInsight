package ens.edupath.module.service;

import com.opencsv.CSVWriter;
import ens.edupath.module.dto.ImportResponse;
import ens.edupath.module.entity.Module;
import ens.edupath.module.repository.ModuleRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ModuleImportExportService {

    private final ModuleRepository moduleRepository;
    private final RestTemplate restTemplate;

    @Value("${services.data-ingestion-service:http://data-ingestion-service}")
    private String dataIngestionServiceUrl;

    @Autowired
    public ModuleImportExportService(ModuleRepository moduleRepository, RestTemplate restTemplate) {
        this.moduleRepository = moduleRepository;
        this.restTemplate = restTemplate;
    }

    /**
     * Importe des modules via Data Ingestion Service
     */
    public ImportResponse importModulesViaIngestion(MultipartFile file, boolean async) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new org.springframework.core.io.ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });
            body.add("entityType", "Module");
            body.add("async", String.valueOf(async));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            String url = dataIngestionServiceUrl + "/api/ingestion/upload";
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            ImportResponse importResponse = new ImportResponse();
            
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null) {
                importResponse.setLogId(responseBody.get("logId") != null ? 
                    ((Number) responseBody.get("logId")).longValue() : null);
                importResponse.setFileName((String) responseBody.get("fileName"));
                importResponse.setStatus(String.valueOf(responseBody.get("status")));
                if (responseBody.get("totalRecords") != null) {
                    importResponse.setTotalRecords(((Number) responseBody.get("totalRecords")).intValue());
                }
                if (responseBody.get("successfulRecords") != null) {
                    importResponse.setSuccessfulRecords(((Number) responseBody.get("successfulRecords")).intValue());
                }
                if (responseBody.get("failedRecords") != null) {
                    importResponse.setFailedRecords(((Number) responseBody.get("failedRecords")).intValue());
                }
                importResponse.setMessage((String) responseBody.get("message"));
            } else {
                importResponse.setMessage("Import initié via Data Ingestion Service");
            }
            
            return importResponse;
        } catch (Exception e) {
            ImportResponse errorResponse = new ImportResponse();
            errorResponse.setStatus("FAILED");
            errorResponse.setMessage("Erreur lors de l'import: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * Exporte les modules en CSV
     */
    public Resource exportModulesToCSV() throws IOException {
        List<Module> modules = moduleRepository.findAll();
        
        StringWriter writer = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(writer);
        
        // Headers
        String[] headers = {"code", "name", "description", "credits", "active"};
        csvWriter.writeNext(headers);
        
        // Data
        for (Module module : modules) {
            String[] row = {
                module.getCode(),
                module.getName(),
                module.getDescription() != null ? module.getDescription() : "",
                String.valueOf(module.getCredits()),
                module.getActive().toString()
            };
            csvWriter.writeNext(row);
        }
        
        csvWriter.close();
        
        byte[] bytes = writer.toString().getBytes();
        return new ByteArrayResource(bytes != null ? bytes : new byte[0]);
    }

    /**
     * Exporte les modules en Excel
     */
    public Resource exportModulesToExcel() throws IOException {
        List<Module> modules = moduleRepository.findAll();
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Modules");
        
        // Style pour les headers
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        
        // Headers
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Code", "Name", "Description", "Credits", "Active"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data
        int rowNum = 1;
        for (Module module : modules) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(module.getCode());
            row.createCell(1).setCellValue(module.getName());
            row.createCell(2).setCellValue(module.getDescription() != null ? module.getDescription() : "");
            row.createCell(3).setCellValue(module.getCredits());
            row.createCell(4).setCellValue(module.getActive());
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        byte[] bytes = outputStream.toByteArray();
        return new ByteArrayResource(bytes != null ? bytes : new byte[0]);
    }
}


