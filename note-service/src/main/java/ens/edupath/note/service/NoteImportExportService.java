package ens.edupath.note.service;

import ens.edupath.note.client.ModuleServiceClient;
import ens.edupath.note.dto.ImportResponse;
import ens.edupath.note.entity.Note;
import ens.edupath.note.repository.NoteRepository;
import com.opencsv.CSVWriter;
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
import java.util.List;
import java.util.Map;

@Service
public class NoteImportExportService {

    private final NoteRepository noteRepository;
    private final RestTemplate restTemplate;
    private final ModuleServiceClient moduleServiceClient;

    @Value("${services.data-ingestion-service:http://data-ingestion-service}")
    private String dataIngestionServiceUrl;

    @Autowired
    public NoteImportExportService(NoteRepository noteRepository, 
                                  RestTemplate restTemplate,
                                  ModuleServiceClient moduleServiceClient) {
        this.noteRepository = noteRepository;
        this.restTemplate = restTemplate;
        this.moduleServiceClient = moduleServiceClient;
    }

    /**
     * Importe des notes via Data Ingestion Service
     */
    public ImportResponse importNotesViaIngestion(MultipartFile file, boolean async) {
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
            body.add("entityType", "Evaluation");
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
     * Exporte un template de notes pour un module (avec étudiants inscrits et champ note vide)
     */
    public Resource exportNotesTemplateForModule(Long moduleId, String moduleCode, String moduleName, 
                                                 String evaluationType, String evaluationTitle, 
                                                 Double maxScore) throws IOException {
        // Récupérer les étudiants inscrits au module
        List<Map<String, Object>> enrollments = moduleServiceClient.getModuleEnrollments(moduleId);
        
        StringWriter writer = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(writer);
        
        // Headers avec note vide pour remplir
        String[] headers = {"student_id", "student_username", "student_email", "module_id", "module_code", 
                           "module_name", "evaluation_type", "evaluation_title", "score", "max_score", 
                           "comments", "evaluation_date"};
        csvWriter.writeNext(headers);
        
        // Data avec les étudiants inscrits, score vide à remplir
        for (Map<String, Object> enrollment : enrollments) {
            // Filtrer seulement les inscriptions approuvées
            Object statusObj = enrollment.get("status");
            String status = statusObj != null ? statusObj.toString() : "";
            if ("APPROVED".equals(status)) {
                Object studentIdObj = enrollment.get("studentId");
                Object studentUsernameObj = enrollment.get("studentUsername");
                Object studentEmailObj = enrollment.get("studentEmail");
                
                String[] row = {
                    studentIdObj != null ? studentIdObj.toString() : "",
                    studentUsernameObj != null ? studentUsernameObj.toString() : "",
                    studentEmailObj != null ? studentEmailObj.toString() : "",
                    String.valueOf(moduleId),
                    moduleCode,
                    moduleName,
                    evaluationType,
                    evaluationTitle,
                    "", // Score vide à remplir
                    maxScore != null ? String.valueOf(maxScore) : "20", // Max score par défaut 20
                    "", // Comments vide
                    LocalDateTime.now().toString() // Date par défaut (à modifier si nécessaire)
                };
                csvWriter.writeNext(row);
            }
        }
        
        csvWriter.close();
        
        byte[] bytes = writer.toString().getBytes();
        return new ByteArrayResource(bytes != null ? bytes : new byte[0]);
    }

    /**
     * Exporte les notes existantes en CSV
     */
    public Resource exportNotesToCSV() throws IOException {
        List<Note> notes = noteRepository.findAll();
        
        StringWriter writer = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(writer);
        
        // Headers
        String[] headers = {"student_id", "student_username", "module_id", "module_code", "module_name", 
                           "evaluation_type", "evaluation_title", "score", "max_score", "percentage", 
                           "passing", "comments", "evaluation_date"};
        csvWriter.writeNext(headers);
        
        // Data
        for (Note note : notes) {
            String[] row = {
                note.getStudentId(),
                note.getStudentUsername(),
                String.valueOf(note.getModuleId()),
                note.getModuleCode(),
                note.getModuleName(),
                note.getEvaluationType(),
                note.getEvaluationTitle(),
                String.valueOf(note.getScore()),
                String.valueOf(note.getMaxScore()),
                String.format("%.2f", note.getPercentage()),
                note.isPassing().toString(),
                note.getComments() != null ? note.getComments() : "",
                note.getEvaluationDate() != null ? note.getEvaluationDate().toString() : ""
            };
            csvWriter.writeNext(row);
        }
        
        csvWriter.close();
        
        byte[] bytes = writer.toString().getBytes();
        return new ByteArrayResource(bytes != null ? bytes : new byte[0]);
    }

    /**
     * Exporte un template de notes pour un module en Excel (avec étudiants inscrits et champ note vide)
     */
    public Resource exportNotesTemplateForModuleExcel(Long moduleId, String moduleCode, String moduleName,
                                                      String evaluationType, String evaluationTitle,
                                                      Double maxScore) throws IOException {
        // Récupérer les étudiants inscrits au module
        List<Map<String, Object>> enrollments = moduleServiceClient.getModuleEnrollments(moduleId);
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Notes Template");
        
        // Style pour les headers
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        
        // Headers
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Student ID", "Student Username", "Student Email", "Module ID", "Module Code",
                           "Module Name", "Evaluation Type", "Evaluation Title", "Score", "Max Score",
                           "Comments", "Evaluation Date"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data avec les étudiants inscrits, score vide à remplir
        int rowNum = 1;
        for (Map<String, Object> enrollment : enrollments) {
            // Filtrer seulement les inscriptions approuvées
            Object statusObj = enrollment.get("status");
            String status = statusObj != null ? statusObj.toString() : "";
            if ("APPROVED".equals(status)) {
                Row row = sheet.createRow(rowNum++);
                
                Object studentIdObj = enrollment.get("studentId");
                Object studentUsernameObj = enrollment.get("studentUsername");
                Object studentEmailObj = enrollment.get("studentEmail");
                
                row.createCell(0).setCellValue(studentIdObj != null ? studentIdObj.toString() : "");
                row.createCell(1).setCellValue(studentUsernameObj != null ? studentUsernameObj.toString() : "");
                row.createCell(2).setCellValue(studentEmailObj != null ? studentEmailObj.toString() : "");
                row.createCell(3).setCellValue(moduleId);
                row.createCell(4).setCellValue(moduleCode);
                row.createCell(5).setCellValue(moduleName);
                row.createCell(6).setCellValue(evaluationType);
                row.createCell(7).setCellValue(evaluationTitle);
                row.createCell(8).setCellValue(""); // Score vide à remplir
                row.createCell(9).setCellValue(maxScore != null ? maxScore : 20.0);
                row.createCell(10).setCellValue(""); // Comments vide
                row.createCell(11).setCellValue(LocalDateTime.now().toString());
            }
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

    /**
     * Exporte les notes en Excel
     */
    public Resource exportNotesToExcel() throws IOException {
        List<Note> notes = noteRepository.findAll();
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Notes");
        
        // Style pour les headers
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        
        // Headers
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Student ID", "Student Username", "Module ID", "Module Code", "Module Name",
                           "Evaluation Type", "Evaluation Title", "Score", "Max Score", "Percentage",
                           "Passing", "Comments", "Evaluation Date"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data
        int rowNum = 1;
        for (Note note : notes) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(note.getStudentId());
            row.createCell(1).setCellValue(note.getStudentUsername());
            row.createCell(2).setCellValue(note.getModuleId());
            row.createCell(3).setCellValue(note.getModuleCode());
            row.createCell(4).setCellValue(note.getModuleName());
            row.createCell(5).setCellValue(note.getEvaluationType());
            row.createCell(6).setCellValue(note.getEvaluationTitle());
            row.createCell(7).setCellValue(note.getScore());
            row.createCell(8).setCellValue(note.getMaxScore());
            row.createCell(9).setCellValue(note.getPercentage());
            row.createCell(10).setCellValue(note.isPassing());
            row.createCell(11).setCellValue(note.getComments() != null ? note.getComments() : "");
            if (note.getEvaluationDate() != null) {
                row.createCell(12).setCellValue(note.getEvaluationDate().toString());
            }
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

