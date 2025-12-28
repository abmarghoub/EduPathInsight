package ens.edupath.note.controller;

import ens.edupath.note.dto.*;
import ens.edupath.note.entity.Alert;
import ens.edupath.note.entity.KPI;
import ens.edupath.note.service.*;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notes/admin")
@CrossOrigin(origins = "*")
public class NoteController {

    private final NoteService noteService;
    private final KPIService kpiService;
    private final AlertService alertService;
    private final NoteImportExportService importExportService;

    public NoteController(NoteService noteService,
                         KPIService kpiService,
                         AlertService alertService,
                         NoteImportExportService importExportService) {
        this.noteService = noteService;
        this.kpiService = kpiService;
        this.alertService = alertService;
        this.importExportService = importExportService;
    }

    // Gestion des notes (Admin uniquement)
    @PostMapping("/notes")
    public ResponseEntity<NoteResponse> createNote(@Valid @RequestBody NoteRequest request) {
        try {
            NoteResponse response = noteService.createNote(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/notes/{id}")
    public ResponseEntity<NoteResponse> updateNote(
            @PathVariable Long id,
            @Valid @RequestBody NoteRequest request) {
        try {
            NoteResponse response = noteService.updateNote(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/notes")
    public ResponseEntity<List<NoteResponse>> getAllNotes() {
        return ResponseEntity.ok(noteService.getAllNotes());
    }

    @GetMapping("/notes/{id}")
    public ResponseEntity<NoteResponse> getNoteById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(noteService.getNoteById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/notes/student/{studentId}")
    public ResponseEntity<List<NoteResponse>> getNotesByStudent(@PathVariable String studentId) {
        return ResponseEntity.ok(noteService.getNotesByStudent(studentId));
    }

    @GetMapping("/notes/module/{moduleId}")
    public ResponseEntity<List<NoteResponse>> getNotesByModule(@PathVariable Long moduleId) {
        return ResponseEntity.ok(noteService.getNotesByModule(moduleId));
    }

    @GetMapping("/notes/student/{studentId}/module/{moduleId}")
    public ResponseEntity<List<NoteResponse>> getNotesByStudentAndModule(
            @PathVariable String studentId,
            @PathVariable Long moduleId) {
        return ResponseEntity.ok(noteService.getNotesByStudentAndModule(studentId, moduleId));
    }

    @DeleteMapping("/notes/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        try {
            noteService.deleteNote(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Gestion des KPIs
    @GetMapping("/kpis/student/{studentId}")
    public ResponseEntity<List<KPIResponse>> getKPIsByStudent(@PathVariable String studentId) {
        List<KPI> kpis = kpiService.getKPIsByStudent(studentId);
        List<KPIResponse> responses = kpis.stream()
                .map(this::toKPIResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/kpis/module/{moduleId}")
    public ResponseEntity<List<KPIResponse>> getKPIsByModule(@PathVariable Long moduleId) {
        List<KPI> kpis = kpiService.getKPIsByModule(moduleId);
        List<KPIResponse> responses = kpis.stream()
                .map(this::toKPIResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/kpis/student/{studentId}/module/{moduleId}")
    public ResponseEntity<KPIResponse> getKPI(@PathVariable String studentId, @PathVariable Long moduleId) {
        KPI kpi = kpiService.getKPI(studentId, moduleId);
        if (kpi == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toKPIResponse(kpi));
    }

    @PostMapping("/kpis/student/{studentId}/module/{moduleId}/calculate")
    public ResponseEntity<KPIResponse> calculateKPI(@PathVariable String studentId, @PathVariable Long moduleId) {
        KPI kpi = kpiService.calculateAndUpdateKPI(studentId, moduleId);
        if (kpi == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toKPIResponse(kpi));
    }

    // Gestion des alertes
    @GetMapping("/alerts/student/{studentId}")
    public ResponseEntity<List<AlertResponse>> getAlertsByStudent(@PathVariable String studentId) {
        List<Alert> alerts = alertService.getAlertsByStudent(studentId);
        List<AlertResponse> responses = alerts.stream()
                .map(this::toAlertResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/alerts/student/{studentId}/active")
    public ResponseEntity<List<AlertResponse>> getActiveAlertsByStudent(@PathVariable String studentId) {
        List<Alert> alerts = alertService.getActiveAlertsByStudent(studentId);
        List<AlertResponse> responses = alerts.stream()
                .map(this::toAlertResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/alerts/module/{moduleId}")
    public ResponseEntity<List<AlertResponse>> getAlertsByModule(@PathVariable Long moduleId) {
        List<Alert> alerts = alertService.getAlertsByModule(moduleId);
        List<AlertResponse> responses = alerts.stream()
                .map(this::toAlertResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/alerts/{alertId}/acknowledge")
    public ResponseEntity<AlertResponse> acknowledgeAlert(@PathVariable Long alertId) {
        try {
            Alert alert = alertService.acknowledgeAlert(alertId);
            return ResponseEntity.ok(toAlertResponse(alert));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/alerts/{alertId}/resolve")
    public ResponseEntity<AlertResponse> resolveAlert(@PathVariable Long alertId) {
        try {
            Alert alert = alertService.resolveAlert(alertId);
            return ResponseEntity.ok(toAlertResponse(alert));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Import/Export
    @PostMapping("/notes/import")
    public ResponseEntity<ImportResponse> importNotes(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "async", defaultValue = "false") boolean async) {
        
        if (file == null || file.isEmpty()) {
            ImportResponse errorResponse = new ImportResponse();
            errorResponse.setStatus("FAILED");
            errorResponse.setMessage("Aucun fichier fourni");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        ImportResponse response = importExportService.importNotesViaIngestion(file, async);
        
        if ("FAILED".equals(response.getStatus())) {
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/notes/export/csv")
    public ResponseEntity<Resource> exportNotesCSV() {
        try {
            Resource resource = importExportService.exportNotesToCSV();
            String filename = "notes_export_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/notes/export/excel")
    public ResponseEntity<Resource> exportNotesExcel() {
        try {
            Resource resource = importExportService.exportNotesToExcel();
            String filename = "notes_export_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Export de template pour remplir les notes
    @PostMapping("/notes/export/template")
    public ResponseEntity<Resource> exportNotesTemplate(@Valid @RequestBody ExportTemplateRequest request) {
        try {
            Resource resource = importExportService.exportNotesTemplateForModule(
                    request.getModuleId(),
                    request.getModuleCode(),
                    request.getModuleName(),
                    request.getEvaluationType(),
                    request.getEvaluationTitle(),
                    request.getMaxScore()
            );
            String filename = "notes_template_" + request.getModuleCode() + "_" + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/notes/export/template/excel")
    public ResponseEntity<Resource> exportNotesTemplateExcel(@Valid @RequestBody ExportTemplateRequest request) {
        try {
            Resource resource = importExportService.exportNotesTemplateForModuleExcel(
                    request.getModuleId(),
                    request.getModuleCode(),
                    request.getModuleName(),
                    request.getEvaluationType(),
                    request.getEvaluationTitle(),
                    request.getMaxScore()
            );
            String filename = "notes_template_" + request.getModuleCode() + "_" + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private KPIResponse toKPIResponse(KPI kpi) {
        return new KPIResponse(
                kpi.getId(),
                kpi.getStudentId(),
                kpi.getModuleId(),
                kpi.getAverageScore(),
                kpi.getTotalScore(),
                kpi.getTotalMaxScore(),
                kpi.getNumberOfEvaluations(),
                kpi.getPassingCount(),
                kpi.getFailingCount(),
                kpi.getHighestScore(),
                kpi.getLowestScore(),
                kpi.getLastCalculatedAt()
        );
    }

    private AlertResponse toAlertResponse(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getStudentId(),
                alert.getModuleId(),
                alert.getType().name(),
                alert.getTitle(),
                alert.getMessage(),
                alert.getStatus().name(),
                alert.getThresholdValue(),
                alert.getActualValue(),
                alert.getCreatedAt(),
                alert.getAcknowledgedAt()
        );
    }
}

