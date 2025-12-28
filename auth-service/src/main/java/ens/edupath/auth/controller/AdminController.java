package ens.edupath.auth.controller;

import ens.edupath.auth.dto.CreateUserRequest;
import ens.edupath.auth.dto.UserImportResponse;
import ens.edupath.auth.dto.UserResponse;
import ens.edupath.auth.entity.User;
import ens.edupath.auth.service.UserService;
import ens.edupath.auth.service.UserImportExportService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth/admin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final UserImportExportService userImportExportService;

    public AdminController(UserService userService, UserImportExportService userImportExportService) {
        this.userService = userService;
        this.userImportExportService = userImportExportService;
    }

    @PostMapping("/create-user")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request) {
        try {
            // Logger la requête reçue
            System.out.println("=== CREATE USER REQUEST ===");
            System.out.println("Username: " + request.getUsername());
            System.out.println("Email: " + request.getEmail());
            System.out.println("Password: " + (request.getPassword() != null ? "***" : "null"));
            System.out.println("Roles: " + request.getRoles());
            
            Set<String> roles = request.getRoles();
            if (roles == null || roles.isEmpty()) {
                roles = Set.of("ROLE_STUDENT");
            }
            
            System.out.println("Roles après traitement: " + roles);
            
            User user = userService.createUserByAdmin(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword(),
                    roles
            );
            
            System.out.println("Utilisateur créé avec succès: " + user.getUsername());
            return ResponseEntity.ok(toUserResponse(user));
        } catch (RuntimeException e) {
            System.err.println("Erreur lors de la création de l'utilisateur: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers().stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users/import")
    public ResponseEntity<UserImportResponse> importUsers(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "viaIngestion", defaultValue = "true") boolean viaIngestion,
            @RequestParam(value = "async", defaultValue = "false") boolean async) {
        
        if (file == null || file.isEmpty()) {
            UserImportResponse errorResponse = new UserImportResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Aucun fichier fourni");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        UserImportResponse response;
        if (viaIngestion) {
            // Import via Data Ingestion Service
            response = userImportExportService.importUsersViaIngestion(file, async);
        } else {
            // Import manuel (direct)
            // Note: Pour l'import manuel, il faudrait d'abord parser le fichier
            // Pour l'instant, on utilise uniquement viaIngestion
            response = userImportExportService.importUsersViaIngestion(file, async);
        }

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/users/export/csv")
    public ResponseEntity<Resource> exportUsersCSV() {
        try {
            Resource resource = userImportExportService.exportUsersToCSV();
            String filename = "users_export_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/users/export/excel")
    public ResponseEntity<Resource> exportUsersExcel() {
        try {
            Resource resource = userImportExportService.exportUsersToExcel();
            String filename = "users_export_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getEmailVerified(),
                user.getEnabled(),
                user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet()),
                user.getCreatedAt()
        );
    }

    private static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
