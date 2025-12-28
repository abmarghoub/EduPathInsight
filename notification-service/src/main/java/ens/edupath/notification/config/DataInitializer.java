package ens.edupath.notification.config;

import ens.edupath.notification.entity.Notification;
import ens.edupath.notification.entity.NotificationTemplate;
import ens.edupath.notification.repository.NotificationTemplateRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer {

    private final NotificationTemplateRepository templateRepository;

    public DataInitializer(NotificationTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @PostConstruct
    public void init() {
        // Initialiser les templates de notification par défaut si ils n'existent pas
        initializeTemplates();
    }

    private void initializeTemplates() {
        List<NotificationTemplate> templates = Arrays.asList(
            // Template pour HIGH_RISK_STUDENT - EMAIL
            createTemplate("HIGH_RISK_STUDENT_EMAIL", "HIGH_RISK_STUDENT", Notification.NotificationChannel.EMAIL,
                    "Alerte: Étudiant à risque élevé",
                    "Bonjour,\n\nL'étudiant {{student_id}} présente un risque {{risk_level}} dans le module {{module_id}}.\n\nAction recommandée immédiate.\n\nCordialement,\nEduPath Insight"),

            // Template pour ENROLLMENT_APPROVED - EMAIL
            createTemplate("ENROLLMENT_APPROVED_EMAIL", "ENROLLMENT_APPROVED", Notification.NotificationChannel.EMAIL,
                    "Inscription approuvée",
                    "Bonjour,\n\nVotre inscription au module {{module_code}} a été approuvée.\n\nVous pouvez maintenant accéder au module.\n\nCordialement,\nEduPath Insight"),

            // Template pour ENROLLMENT_REJECTED - EMAIL
            createTemplate("ENROLLMENT_REJECTED_EMAIL", "ENROLLMENT_REJECTED", Notification.NotificationChannel.EMAIL,
                    "Inscription rejetée",
                    "Bonjour,\n\nVotre inscription au module {{module_code}} a été rejetée.\n\nPour plus d'informations, veuillez contacter l'administration.\n\nCordialement,\nEduPath Insight"),

            // Template pour RISK_MODULE - EMAIL
            createTemplate("RISK_MODULE_EMAIL", "RISK_MODULE", Notification.NotificationChannel.EMAIL,
                    "Alerte: Module à risque",
                    "Bonjour,\n\nLe module {{module_code}} présente un score de risque de {{risk_score}} avec {{at_risk_count}} étudiants à risque.\n\nAction recommandée.\n\nCordialement,\nEduPath Insight"),

            // Template pour HIGH_RISK_STUDENT - PUSH
            createTemplate("HIGH_RISK_STUDENT_PUSH", "HIGH_RISK_STUDENT", Notification.NotificationChannel.PUSH,
                    "Alerte: Risque élevé",
                    "Vous présentez un risque {{risk_level}} dans le module {{module_code}}"),

            // Template pour ENROLLMENT_APPROVED - PUSH
            createTemplate("ENROLLMENT_APPROVED_PUSH", "ENROLLMENT_APPROVED", Notification.NotificationChannel.PUSH,
                    "Inscription approuvée",
                    "Votre inscription au module {{module_code}} a été approuvée")
        );

        for (NotificationTemplate template : templates) {
            if (!templateRepository.findByTemplateKey(template.getTemplateKey()).isPresent()) {
                templateRepository.save(template);
            }
        }
    }

    private NotificationTemplate createTemplate(String templateKey, String notificationType,
                                               Notification.NotificationChannel channel,
                                               String subject, String body) {
        NotificationTemplate template = new NotificationTemplate();
        template.setTemplateKey(templateKey);
        template.setNotificationType(notificationType);
        template.setChannel(channel);
        template.setSubject(subject);
        template.setBody(body);
        template.setActive(true);
        return template;
    }
}


