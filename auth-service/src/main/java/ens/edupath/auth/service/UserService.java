package ens.edupath.auth.service;

import ens.edupath.auth.entity.*;
import ens.edupath.auth.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${email.verification.code.expiration}")
    private Long verificationCodeExpiration;

    @Value("${email.password.reset.expiration}")
    private Long passwordResetExpiration;

    @Value("${email.verification.code.length}")
    private int verificationCodeLength;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                      VerificationCodeRepository verificationCodeRepository,
                      PasswordResetTokenRepository passwordResetTokenRepository,
                      PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.verificationCodeRepository = verificationCodeRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public User registerUser(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Le nom d'utilisateur existe déjà");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("L'email existe déjà");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmailVerified(false);
        user.setEnabled(true);
        user.setPasswordChanged(true);

        Role studentRole = roleRepository.findByName(Role.RoleName.ROLE_STUDENT)
                .orElseThrow(() -> new RuntimeException("Rôle STUDENT non trouvé"));
        user.addRole(studentRole);

        user = userRepository.save(user);
        sendVerificationCode(email);
        return user;
    }

    public void sendVerificationCode(String email) {
        // Supprimer les anciens codes
        verificationCodeRepository.deleteByEmail(email);

        String code = generateVerificationCode();
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setCode(code);
        verificationCode.setEmail(email);
        verificationCode.setExpiresAt(LocalDateTime.now().plusSeconds(verificationCodeExpiration / 1000));
        verificationCode.setUsed(false);
        verificationCodeRepository.save(verificationCode);

        emailService.sendVerificationCode(email, code);
    }

    public User verifyEmail(String email, String code) {
        VerificationCode verificationCode = verificationCodeRepository
                .findByEmailAndCodeAndUsedFalse(email, code)
                .orElseThrow(() -> new RuntimeException("Code de vérification invalide ou expiré"));

        if (verificationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Code de vérification expiré");
        }

        verificationCode.setUsed(true);
        verificationCodeRepository.save(verificationCode);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        user.setEmailVerified(true);
        return userRepository.save(user);
    }

    public User login(String usernameOrEmail, String password) {
        User user = userRepository.findByUsername(usernameOrEmail)
                .orElse(userRepository.findByEmail(usernameOrEmail)
                        .orElseThrow(() -> new RuntimeException("Nom d'utilisateur ou email incorrect")));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Mot de passe incorrect");
        }

        if (!user.getEnabled()) {
            throw new RuntimeException("Compte désactivé");
        }

        // Si l'email n'est pas vérifié ou si c'est la première connexion (mot de passe non changé)
        if (!user.getEmailVerified() || !user.getPasswordChanged()) {
            sendVerificationCode(user.getEmail());
            throw new RuntimeException("Code de vérification requis");
        }

        return user;
    }

    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email non trouvé"));

        // Supprimer les anciens tokens
        passwordResetTokenRepository.deleteByEmail(email);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setEmail(email);
        resetToken.setExpiresAt(LocalDateTime.now().plusSeconds(passwordResetExpiration / 1000));
        resetToken.setUsed(false);
        passwordResetTokenRepository.save(resetToken);

        String resetLink = "http://localhost:3002/reset-password?token=" + token;
        emailService.sendPasswordResetLink(email, resetLink);
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenAndUsedFalse(token)
                .orElseThrow(() -> new RuntimeException("Token invalide ou expiré"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expiré");
        }

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        User user = userRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChanged(true);
        userRepository.save(user);
    }

    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Ancien mot de passe incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChanged(true);
        userRepository.save(user);
    }

    public User createUserByAdmin(String username, String email, String password, Set<String> roleNames) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Le nom d'utilisateur existe déjà");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("L'email existe déjà");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmailVerified(false);
        user.setEnabled(true);
        user.setPasswordChanged(false); // L'utilisateur devra changer le mot de passe

        Set<Role> roles = roleNames.stream()
                .map(roleName -> {
                    try {
                        Role.RoleName name = Role.RoleName.valueOf(roleName);
                        return roleRepository.findByName(name)
                                .orElseThrow(() -> new RuntimeException("Rôle non trouvé: " + roleName));
                    } catch (IllegalArgumentException e) {
                        throw new RuntimeException("Rôle invalide: " + roleName);
                    }
                })
                .collect(Collectors.toSet());

        user.setRoles(roles);
        user = userRepository.save(user);

        // Envoyer un email de bienvenue avec le mot de passe temporaire
        emailService.sendWelcomeEmail(email, username, password);
        sendVerificationCode(email);

        return user;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < verificationCodeLength; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    public void initializeDefaultAdmin() {
        // Vérifier si l'admin existe par username ou email
        Optional<User> existingAdmin = userRepository.findByUsername("admin")
                .or(() -> userRepository.findByEmail("edupathinsight@gmail.com"));
        
        if (existingAdmin.isEmpty()) {
            // Créer un nouvel admin
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("edupathinsight@gmail.com");
            admin.setPassword(passwordEncoder.encode("edupath"));
            admin.setEmailVerified(true);
            admin.setEnabled(true);
            admin.setPasswordChanged(true);

            Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN)
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName(Role.RoleName.ROLE_ADMIN);
                        return roleRepository.save(role);
                    });

            Role studentRole = roleRepository.findByName(Role.RoleName.ROLE_STUDENT)
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName(Role.RoleName.ROLE_STUDENT);
                        return roleRepository.save(role);
                    });

            admin.addRole(adminRole);
            admin.addRole(studentRole);
            userRepository.save(admin);
        } else {
            // S'assurer que l'admin existant a le rôle ADMIN
            User admin = existingAdmin.get();
            boolean hasAdminRole = admin.getRoles().stream()
                    .anyMatch(role -> role.getName() == Role.RoleName.ROLE_ADMIN);
            
            if (!hasAdminRole) {
                Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN)
                        .orElseGet(() -> {
                            Role role = new Role();
                            role.setName(Role.RoleName.ROLE_ADMIN);
                            return roleRepository.save(role);
                        });
                admin.addRole(adminRole);
                userRepository.save(admin);
            }
        }
    }
}


