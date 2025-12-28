package ens.edupath.auth.controller;

import ens.edupath.auth.dto.*;
import ens.edupath.auth.entity.User;
import ens.edupath.auth.service.JwtService;
import ens.edupath.auth.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.registerUser(request.getUsername(), request.getEmail(), request.getPassword());
            return ResponseEntity.ok(new AuthResponse(
                    null,
                    user.getUsername(),
                    user.getEmail(),
                    user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet()),
                    "Inscription réussie. Un code de vérification a été envoyé à votre email."
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            User user = userService.login(request.getUsernameOrEmail(), request.getPassword());
            String token = jwtService.generateToken(
                    user.getUsername(),
                    user.getEmail(),
                    user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.joining(","))
            );
            return ResponseEntity.ok(new AuthResponse(
                    token,
                    user.getUsername(),
                    user.getEmail(),
                    user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet()),
                    "Connexion réussie"
            ));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Code de vérification requis")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse(
                        null,
                        null,
                        null,
                        null,
                        e.getMessage()
                ));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@Valid @RequestBody VerifyRequest request) {
        try {
            User user = userService.verifyEmail(request.getEmail(), request.getCode());
            String token = jwtService.generateToken(
                    user.getUsername(),
                    user.getEmail(),
                    user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.joining(","))
            );
            return ResponseEntity.ok(new AuthResponse(
                    token,
                    user.getUsername(),
                    user.getEmail(),
                    user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet()),
                    "Email vérifié avec succès"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody PasswordResetRequest request) {
        try {
            userService.requestPasswordReset(request.getEmail());
            return ResponseEntity.ok(new ErrorResponse("Un email de réinitialisation a été envoyé"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            userService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(new ErrorResponse("Mot de passe réinitialisé avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            // Obtenir le username depuis le contexte de sécurité
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            if (username == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Impossible de déterminer l'utilisateur"));
            }

            userService.changePassword(username, request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok(new ErrorResponse("Mot de passe changé avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validate() {
        // L'endpoint /validate est public et ne nécessite pas d'authentification
        // Il est utilisé pour vérifier que le service est actif
        // Pour valider un token, on utilise le filtre JWT qui extrait les infos du token
        Map<String, Object> response = new HashMap<>();
        response.put("status", "active");
        response.put("message", "Service d'authentification actif");
        return ResponseEntity.ok(response);
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

