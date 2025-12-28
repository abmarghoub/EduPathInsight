package ens.edupath.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import ens.edupath.auth.dto.UserImportResponse;
import ens.edupath.auth.entity.Role;
import ens.edupath.auth.entity.User;
import ens.edupath.auth.repository.RoleRepository;
import ens.edupath.auth.repository.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserImportExportService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RestTemplate restTemplate;

    @Value("${services.data-ingestion-service:http://data-ingestion-service}")
    private String dataIngestionServiceUrl;

    @Autowired
    public UserImportExportService(UserRepository userRepository,
                                  RoleRepository roleRepository,
                                  PasswordEncoder passwordEncoder,
                                  EmailService emailService,
                                  RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.restTemplate = restTemplate;
    }

    /**
     * Importe des utilisateurs via Data Ingestion Service
     */
    public UserImportResponse importUsersViaIngestion(MultipartFile file, boolean async) {
        try {
            // Préparer la requête multipart
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            // Convertir MultipartFile en Resource pour RestTemplate
            body.add("file", new org.springframework.core.io.ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });
            body.add("entityType", "User");
            body.add("async", String.valueOf(async));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Appeler le service d'ingestion
            String url = dataIngestionServiceUrl + "/api/ingestion/upload";
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            UserImportResponse importResponse = new UserImportResponse();
            
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null) {
                importResponse.setSuccess(true);
                
                if (responseBody.containsKey("status")) {
                    importResponse.setStatus(String.valueOf(responseBody.get("status")));
                }
                if (responseBody.containsKey("message")) {
                    importResponse.setMessage(String.valueOf(responseBody.get("message")));
                }
                if (responseBody.containsKey("successfulRecords")) {
                    Object successfulRecords = responseBody.get("successfulRecords");
                    if (successfulRecords != null) {
                        importResponse.setImportedCount(((Number) successfulRecords).intValue());
                    }
                }
                if (responseBody.containsKey("failedRecords")) {
                    Object failedRecords = responseBody.get("failedRecords");
                    if (failedRecords != null) {
                        importResponse.setFailedCount(((Number) failedRecords).intValue());
                    }
                }
                
                // Déterminer le succès basé sur le status
                String status = importResponse.getStatus();
                if (status != null && (status.equals("FAILED") || status.equals("PARTIALLY_COMPLETED"))) {
                    importResponse.setSuccess(false);
                }
            } else {
                importResponse.setSuccess(true);
                importResponse.setMessage("Import initié via Data Ingestion Service");
            }
            
            return importResponse;
        } catch (Exception e) {
            UserImportResponse errorResponse = new UserImportResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Erreur lors de l'import: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * Importe des utilisateurs manuellement (création directe)
     */
    public UserImportResponse importUsersManual(List<Map<String, String>> usersData) {
        UserImportResponse response = new UserImportResponse();
        int imported = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        Role defaultRole = roleRepository.findByName(Role.RoleName.ROLE_STUDENT)
                .orElseThrow(() -> new RuntimeException("Rôle STUDENT non trouvé"));

        for (Map<String, String> userData : usersData) {
            try {
                String username = userData.getOrDefault("username", "").trim();
                String email = userData.getOrDefault("email", "").trim();
                String password = userData.getOrDefault("password", generateRandomPassword());

                if (username.isEmpty() || email.isEmpty()) {
                    failed++;
                    errors.add("Ligne avec username ou email vide ignorée");
                    continue;
                }

                // Vérifier si l'utilisateur existe déjà
                User existingUser = userRepository.findByUsername(username)
                        .orElse(userRepository.findByEmail(email).orElse(null));

                if (existingUser != null) {
                    // Mettre à jour l'utilisateur existant
                    existingUser.setEmail(email);
                    if (!userData.getOrDefault("first_name", "").isEmpty()) {
                        // Note: Ajouter ces champs si nécessaire dans l'entité User
                    }
                    userRepository.save(existingUser);
                } else {
                    // Créer un nouvel utilisateur
                    User user = new User();
                    user.setUsername(username);
                    user.setEmail(email);
                    user.setPassword(passwordEncoder.encode(password));
                    user.setEmailVerified(false);
                    user.setEnabled(true);
                    user.setPasswordChanged(false); // Devra changer le mot de passe
                    user.addRole(defaultRole);

                    user = userRepository.save(user);

                    // Envoyer un email de bienvenue
                    emailService.sendWelcomeEmail(email, username, password);
                }

                imported++;
            } catch (Exception e) {
                failed++;
                errors.add("Erreur pour l'utilisateur: " + e.getMessage());
            }
        }

        response.setImportedCount(imported);
        response.setFailedCount(failed);
        response.setSuccess(failed == 0);
        response.setErrors(errors);
        response.setMessage(String.format("Import terminé: %d importés, %d échoués", imported, failed));

        return response;
    }

    /**
     * Exporte les utilisateurs en CSV
     */
    public Resource exportUsersToCSV() throws IOException {
        List<User> users = userRepository.findAll();
        
        StringWriter writer = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(writer);
        
        // Headers
        String[] headers = {"id", "username", "email", "email_verified", "enabled", "roles", "created_at"};
        csvWriter.writeNext(headers);
        
        // Data
        for (User user : users) {
            String roles = user.getRoles().stream()
                    .map(r -> r.getName().name())
                    .collect(Collectors.joining(","));
            
            String[] row = {
                user.getId().toString(),
                user.getUsername(),
                user.getEmail(),
                user.getEmailVerified().toString(),
                user.getEnabled().toString(),
                roles,
                user.getCreatedAt() != null ? user.getCreatedAt().toString() : ""
            };
            csvWriter.writeNext(row);
        }
        
        csvWriter.close();
        
        byte[] bytes = writer.toString().getBytes();
        return new ByteArrayResource(bytes != null ? bytes : new byte[0]);
    }

    /**
     * Exporte les utilisateurs en Excel
     */
    public Resource exportUsersToExcel() throws IOException {
        List<User> users = userRepository.findAll();
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Users");
        
        // Style pour les headers
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        
        // Headers
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Username", "Email", "Email Verified", "Enabled", "Roles", "Created At"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data
        int rowNum = 1;
        for (User user : users) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(user.getId());
            row.createCell(1).setCellValue(user.getUsername());
            row.createCell(2).setCellValue(user.getEmail());
            row.createCell(3).setCellValue(user.getEmailVerified());
            row.createCell(4).setCellValue(user.getEnabled());
            
            String roles = user.getRoles().stream()
                    .map(r -> r.getName().name())
                    .collect(Collectors.joining(", "));
            row.createCell(5).setCellValue(roles);
            
            if (user.getCreatedAt() != null) {
                row.createCell(6).setCellValue(user.getCreatedAt().toString());
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

    private String generateRandomPassword() {
        // Générer un mot de passe aléatoire de 12 caractères
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }
}

