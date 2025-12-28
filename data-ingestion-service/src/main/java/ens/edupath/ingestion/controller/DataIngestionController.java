package ens.edupath.ingestion.controller;

import ens.edupath.ingestion.dto.IngestionResponse;
import ens.edupath.ingestion.service.DataIngestionService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ingestion")
@CrossOrigin(origins = "*")
public class DataIngestionController {

    private final DataIngestionService dataIngestionService;

    public DataIngestionController(DataIngestionService dataIngestionService) {
        this.dataIngestionService = dataIngestionService;
    }

    @PostMapping("/upload")
    public ResponseEntity<IngestionResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("entityType") @NotBlank String entityType,
            @RequestParam(value = "async", defaultValue = "false") boolean async) {
        
        try {
            IngestionResponse response = dataIngestionService.processFile(file, entityType, async);
            
            if (response.getStatus().equals("FAILED")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            IngestionResponse errorResponse = new IngestionResponse();
            errorResponse.setStatus("FAILED");
            errorResponse.setMessage("Erreur lors du traitement: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Data Ingestion Service is running");
    }
}


