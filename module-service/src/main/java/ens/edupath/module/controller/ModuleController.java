package ens.edupath.module.controller;

import ens.edupath.module.dto.EnrollmentRequest;
import ens.edupath.module.dto.EnrollmentResponse;
import ens.edupath.module.dto.ModuleResponse;
import ens.edupath.module.service.EnrollmentService;
import ens.edupath.module.service.ModuleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
@CrossOrigin(origins = "*")
public class ModuleController {

    private final ModuleService moduleService;
    private final EnrollmentService enrollmentService;

    public ModuleController(ModuleService moduleService, EnrollmentService enrollmentService) {
        this.moduleService = moduleService;
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    public ResponseEntity<List<ModuleResponse>> getAllModules() {
        return ResponseEntity.ok(moduleService.getActiveModules());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModuleResponse> getModuleById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(moduleService.getModuleById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ModuleResponse> getModuleByCode(@PathVariable String code) {
        try {
            return ResponseEntity.ok(moduleService.getModuleByCode(code));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/enroll")
    public ResponseEntity<EnrollmentResponse> enrollInModule(
            @PathVariable Long id,
            @Valid @RequestBody EnrollmentRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Email", required = false) String email,
            @RequestHeader(value = "X-User-Username", required = false) String username) {
        
        try {
            // Utiliser les informations de l'utilisateur depuis les headers
            if (userId == null || username == null || email == null) {
                return ResponseEntity.badRequest().build();
            }

            EnrollmentResponse response = enrollmentService.enrollStudent(
                    id, userId, username, email, false
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/my-enrollments")
    public ResponseEntity<List<EnrollmentResponse>> getMyEnrollments(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(enrollmentService.getEnrollmentsByStudent(userId));
    }

    @DeleteMapping("/enrollments/{enrollmentId}/cancel")
    public ResponseEntity<EnrollmentResponse> cancelEnrollment(
            @PathVariable Long enrollmentId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        try {
            if (userId == null) {
                return ResponseEntity.badRequest().build();
            }

            EnrollmentResponse response = enrollmentService.cancelEnrollment(enrollmentId, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}


