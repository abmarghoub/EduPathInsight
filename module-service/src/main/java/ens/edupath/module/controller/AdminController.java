package ens.edupath.module.controller;

import ens.edupath.module.dto.*;
import ens.edupath.module.service.*;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/modules/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final ModuleService moduleService;
    private final EnrollmentService enrollmentService;
    private final EnrollmentPeriodService enrollmentPeriodService;
    private final ModuleImportExportService importExportService;

    public AdminController(ModuleService moduleService,
                          EnrollmentService enrollmentService,
                          EnrollmentPeriodService enrollmentPeriodService,
                          ModuleImportExportService importExportService) {
        this.moduleService = moduleService;
        this.enrollmentService = enrollmentService;
        this.enrollmentPeriodService = enrollmentPeriodService;
        this.importExportService = importExportService;
    }

    // Gestion des modules
    @PostMapping("/modules")
    public ResponseEntity<ModuleResponse> createModule(@Valid @RequestBody ModuleRequest request) {
        try {
            return ResponseEntity.ok(moduleService.createModule(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/modules/{id}")
    public ResponseEntity<ModuleResponse> updateModule(
            @PathVariable Long id,
            @Valid @RequestBody ModuleRequest request) {
        try {
            return ResponseEntity.ok(moduleService.updateModule(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/modules")
    public ResponseEntity<List<ModuleResponse>> getAllModulesAdmin() {
        return ResponseEntity.ok(moduleService.getAllModules());
    }

    @GetMapping("/modules/{id}")
    public ResponseEntity<ModuleResponse> getModuleByIdAdmin(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(moduleService.getModuleById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/modules/{id}")
    public ResponseEntity<Void> deleteModule(@PathVariable Long id) {
        try {
            moduleService.deleteModule(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Gestion des périodes d'inscription
    @PostMapping("/modules/{moduleId}/enrollment-period")
    public ResponseEntity<EnrollmentPeriodResponse> setEnrollmentPeriod(
            @PathVariable Long moduleId,
            @Valid @RequestBody EnrollmentPeriodRequest request) {
        try {
            return ResponseEntity.ok(enrollmentPeriodService.setEnrollmentPeriod(moduleId, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/modules/{moduleId}/enrollment-period")
    public ResponseEntity<EnrollmentPeriodResponse> getEnrollmentPeriod(@PathVariable Long moduleId) {
        try {
            return ResponseEntity.ok(enrollmentPeriodService.getEnrollmentPeriod(moduleId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/modules/{moduleId}/enrollment-period")
    public ResponseEntity<Void> deleteEnrollmentPeriod(@PathVariable Long moduleId) {
        try {
            enrollmentPeriodService.deleteEnrollmentPeriod(moduleId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Gestion des inscriptions
    @PostMapping("/enrollments")
    public ResponseEntity<EnrollmentResponse> enrollStudent(
            @RequestParam Long moduleId,
            @RequestParam String studentId,
            @RequestParam String studentUsername,
            @RequestParam String studentEmail) {
        try {
            EnrollmentResponse response = enrollmentService.enrollStudent(
                    moduleId, studentId, studentUsername, studentEmail, true
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/modules/{moduleId}/enrollments")
    public ResponseEntity<List<EnrollmentResponse>> getModuleEnrollments(@PathVariable Long moduleId) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByModule(moduleId));
    }

    @GetMapping("/modules/{moduleId}/enrollments/pending")
    public ResponseEntity<List<EnrollmentResponse>> getPendingEnrollments(@PathVariable Long moduleId) {
        return ResponseEntity.ok(enrollmentService.getPendingEnrollments(moduleId));
    }

    @PostMapping("/enrollments/{enrollmentId}/approve")
    public ResponseEntity<EnrollmentResponse> approveEnrollment(@PathVariable Long enrollmentId) {
        try {
            return ResponseEntity.ok(enrollmentService.approveEnrollment(enrollmentId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/enrollments/{enrollmentId}/reject")
    public ResponseEntity<EnrollmentResponse> rejectEnrollment(@PathVariable Long enrollmentId) {
        try {
            return ResponseEntity.ok(enrollmentService.rejectEnrollment(enrollmentId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Import/Export
    @PostMapping("/modules/import")
    public ResponseEntity<ImportResponse> importModules(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "async", defaultValue = "false") boolean async) {
        
        if (file == null || file.isEmpty()) {
            ImportResponse errorResponse = new ImportResponse();
            errorResponse.setStatus("FAILED");
            errorResponse.setMessage("Aucun fichier fourni");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        ImportResponse response = importExportService.importModulesViaIngestion(file, async);
        
        if ("FAILED".equals(response.getStatus())) {
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/modules/export/csv")
    public ResponseEntity<Resource> exportModulesCSV() {
        try {
            Resource resource = importExportService.exportModulesToCSV();
            String filename = "modules_export_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/modules/export/excel")
    public ResponseEntity<Resource> exportModulesExcel() {
        try {
            Resource resource = importExportService.exportModulesToExcel();
            String filename = "modules_export_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}


