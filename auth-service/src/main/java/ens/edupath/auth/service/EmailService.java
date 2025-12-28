package ens.edupath.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Code de vérification - EduPath Insight");
        message.setText(String.format(
                "Bonjour,\n\n" +
                "Votre code de vérification est : %s\n\n" +
                "Ce code est valide pendant 10 minutes.\n\n" +
                "Si vous n'avez pas demandé ce code, veuillez ignorer cet email.\n\n" +
                "Cordialement,\n" +
                "L'équipe EduPath Insight",
                code
        ));
        mailSender.send(message);
    }

    public void sendPasswordResetLink(String to, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Réinitialisation de mot de passe - EduPath Insight");
        message.setText(String.format(
                "Bonjour,\n\n" +
                "Vous avez demandé à réinitialiser votre mot de passe.\n\n" +
                "Cliquez sur le lien suivant pour réinitialiser votre mot de passe :\n" +
                "%s\n\n" +
                "Ce lien est valide pendant 1 heure.\n\n" +
                "Si vous n'avez pas demandé cette réinitialisation, veuillez ignorer cet email.\n\n" +
                "Cordialement,\n" +
                "L'équipe EduPath Insight",
                resetLink
        ));
        mailSender.send(message);
    }

    public void sendWelcomeEmail(String to, String username, String temporaryPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Bienvenue sur EduPath Insight");
        message.setText(String.format(
                "Bonjour %s,\n\n" +
                "Votre compte a été créé avec succès.\n\n" +
                "Vos identifiants de connexion sont :\n" +
                "Nom d'utilisateur : %s\n" +
                "Mot de passe temporaire : %s\n\n" +
                "Vous devrez changer ce mot de passe lors de votre première connexion.\n\n" +
                "Cordialement,\n" +
                "L'équipe EduPath Insight",
                username, username, temporaryPassword
        ));
        mailSender.send(message);
    }
}


