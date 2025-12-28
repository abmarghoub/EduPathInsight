package ens.edupath.ingestion.service;

import com.opencsv.exceptions.CsvException;
import ens.edupath.ingestion.dto.IngestionResponse;
import ens.edupath.ingestion.dto.ValidationResult;
import ens.edupath.ingestion.model.jpa.IngestionLog;
import ens.edupath.ingestion.model.neo4j.Activity;
import ens.edupath.ingestion.model.neo4j.Evaluation;
import ens.edupath.ingestion.model.neo4j.Module;
import ens.edupath.ingestion.model.neo4j.Student;
import ens.edupath.ingestion.repository.jpa.IngestionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class DataIngestionService {

    private final FileValidationService fileValidationService;
    private final CSVParserService csvParserService;
    private final ExcelParserService excelParserService;
    private final GraphService graphService;
    private final IngestionLogRepository ingestionLogRepository;
    private final NotificationService notificationService;

    @Autowired
    public DataIngestionService(FileValidationService fileValidationService,
                               CSVParserService csvParserService,
                               ExcelParserService excelParserService,
                               GraphService graphService,
                               IngestionLogRepository ingestionLogRepository,
                               NotificationService notificationService) {
        this.fileValidationService = fileValidationService;
        this.csvParserService = csvParserService;
        this.excelParserService = excelParserService;
        this.graphService = graphService;
        this.ingestionLogRepository = ingestionLogRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public IngestionResponse processFile(MultipartFile file, String entityType, boolean async) {
        // Créer le log
        IngestionLog log = createIngestionLog(file, entityType);
        log.setStatus(IngestionLog.Status.PROCESSING);
        log = ingestionLogRepository.save(log);

        if (async) {
            processFileAsync(file, entityType, log);
            return createResponse(log, "Traitement asynchrone démarré");
        } else {
            return processFileSync(file, entityType, log);
        }
    }

    @Async("taskExecutor")
    public CompletableFuture<IngestionResponse> processFileAsync(MultipartFile file, String entityType, IngestionLog log) {
        IngestionResponse response = processFileSync(file, entityType, log);
        return CompletableFuture.completedFuture(response);
    }

    @Transactional
    public IngestionResponse processFileSync(MultipartFile file, String entityType, IngestionLog log) {
        try {
            // Validation
            ValidationResult validation = fileValidationService.validateFile(file, entityType);
            if (!validation.isValid()) {
                log.setStatus(IngestionLog.Status.FAILED);
                log.setErrorMessage(String.join("; ", validation.getErrors()));
                log = ingestionLogRepository.save(log);
                return createResponse(log, "Erreur de validation");
            }

            // Parsing
            List<Map<String, String>> records = parseFile(file);

            if (records.isEmpty()) {
                log.setStatus(IngestionLog.Status.FAILED);
                log.setErrorMessage("Aucune donnée trouvée dans le fichier");
                log = ingestionLogRepository.save(log);
                return createResponse(log, "Fichier vide");
            }

            log.setTotalRecords(records.size());
            int successful = 0;
            int failed = 0;

            // Traitement des données
            for (Map<String, String> record : records) {
                try {
                    processRecord(record, entityType);
                    successful++;
                } catch (Exception e) {
                    failed++;
                    // Logger l'erreur mais continuer
                    System.err.println("Erreur lors du traitement d'un enregistrement: " + e.getMessage());
                }
            }

            // Mettre à jour le log
            log.setSuccessfulRecords(successful);
            log.setFailedRecords(failed);
            
            if (failed == 0) {
                log.setStatus(IngestionLog.Status.COMPLETED);
            } else if (successful > 0) {
                log.setStatus(IngestionLog.Status.PARTIALLY_COMPLETED);
            } else {
                log.setStatus(IngestionLog.Status.FAILED);
            }
            
            log = ingestionLogRepository.save(log);

            // Envoyer une notification
            notificationService.sendIngestionNotification(log);

            return createResponse(log, "Traitement terminé");

        } catch (Exception e) {
            log.setStatus(IngestionLog.Status.FAILED);
            log.setErrorMessage(e.getMessage());
            log = ingestionLogRepository.save(log);
            return createResponse(log, "Erreur: " + e.getMessage());
        }
    }

    private List<Map<String, String>> parseFile(MultipartFile file) throws IOException, CsvException {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IOException("Nom de fichier invalide");
        }

        if (filename.endsWith(".csv")) {
            return csvParserService.parseCSV(file);
        } else if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
            return excelParserService.parseExcel(file);
        } else {
            throw new IOException("Format de fichier non supporté");
        }
    }

    private void processRecord(Map<String, String> record, String entityType) {
        switch (entityType) {
            case "User":
                // Créer dans le graphe Neo4j
                graphService.createOrUpdateStudent(record);
                // Note: Pour créer dans auth-service, il faudrait appeler AuthServiceClient
                // Cela sera géré dans une future version ou via un webhook
                break;
            case "Module":
                graphService.createOrUpdateModule(record);
                break;
            case "Note":
            case "Evaluation":
                processEvaluation(record);
                break;
            case "Presence":
            case "Activity":
                processActivity(record);
                break;
            default:
                throw new IllegalArgumentException("Type d'entité non supporté: " + entityType);
        }
    }

    private void processEvaluation(Map<String, String> record) {
        // Récupérer ou créer student et module dans Neo4j
        String studentId = record.get("student_id");
        String moduleId = record.get("module_id");

        Student student = null;
        Module module = null;

        if (studentId != null && !studentId.isEmpty()) {
            student = graphService.createOrUpdateStudent(record);
        }

        if (moduleId != null && !moduleId.isEmpty()) {
            module = graphService.createOrUpdateModule(record);
        }

        if (student != null && module != null) {
            graphService.createEvaluation(record, student, module);
        }

        // Créer la note dans note-service si les données sont complètes
        // Note: Cette partie nécessitera un NoteServiceClient pour créer les notes
        // Pour l'instant, les notes seront créées via l'import dans note-service
    }

    private void processActivity(Map<String, String> record) {
        // Récupérer ou créer student et module dans Neo4j
        String studentId = record.get("student_id");
        String moduleId = record.get("module_id");

        Student student = null;
        Module module = null;

        if (studentId != null && !studentId.isEmpty()) {
            student = graphService.createOrUpdateStudent(record);
        }

        if (moduleId != null && !moduleId.isEmpty()) {
            module = graphService.createOrUpdateModule(record);
        }

        if (student != null && module != null) {
            graphService.createActivity(record, student, module);
        }
        
        // Note: La création dans activities-service sera gérée via l'import direct
        // car activities-service envoie directement à Data Ingestion qui valide et crée
    }

    private IngestionLog createIngestionLog(MultipartFile file, String entityType) {
        IngestionLog log = new IngestionLog();
        log.setFileName(file.getOriginalFilename());
        log.setFileType(getFileExtension(file.getOriginalFilename()));
        log.setEntityType(entityType);
        log.setStatus(IngestionLog.Status.PENDING);
        return log;
    }

    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : filename.substring(lastDotIndex + 1);
    }

    private IngestionResponse createResponse(IngestionLog log, String message) {
        IngestionResponse response = new IngestionResponse();
        response.setLogId(log.getId());
        response.setFileName(log.getFileName());
        response.setEntityType(log.getEntityType());
        response.setStatus(log.getStatus().name());
        response.setTotalRecords(log.getTotalRecords());
        response.setSuccessfulRecords(log.getSuccessfulRecords());
        response.setFailedRecords(log.getFailedRecords());
        response.setMessage(message);
        response.setProcessedAt(log.getCreatedAt());
        return response;
    }
}

