package ens.edupath.notification.service;

import ens.edupath.notification.entity.Notification;
import ens.edupath.notification.entity.NotificationTemplate;
import ens.edupath.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final PushNotificationService pushNotificationService;
    private final NotificationTemplateService templateService;

    public NotificationService(NotificationRepository notificationRepository,
                              EmailService emailService,
                              PushNotificationService pushNotificationService,
                              NotificationTemplateService templateService) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
        this.pushNotificationService = pushNotificationService;
        this.templateService = templateService;
    }

    public Notification createNotification(String recipientId, String recipientEmail, String type,
                                          String title, String message, Notification.NotificationChannel channel,
                                          Map<String, Object> metadata) {
        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setRecipientEmail(recipientEmail);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setChannel(channel);
        notification.setStatus(Notification.NotificationStatus.PENDING);
        notification.setScheduledAt(LocalDateTime.now());
        
        if (metadata != null) {
            notification.setMetadata(metadata.toString()); // En production, utiliser JSON
        }

        notification = notificationRepository.save(notification);

        // Envoyer la notification
        sendNotification(notification);

        return notification;
    }

    public void sendNotification(Notification notification) {
        try {
            // Récupérer le template si disponible
            Optional<NotificationTemplate> templateOpt = templateService.getTemplate(
                    notification.getType(),
                    notification.getChannel()
            );

            String title = notification.getTitle();
            String message = notification.getMessage();

            if (templateOpt.isPresent()) {
                NotificationTemplate template = templateOpt.get();
                Map<String, String> variables = extractVariablesFromMetadata(notification.getMetadata());
                title = templateService.renderTemplate(template.getSubject(), variables);
                message = templateService.renderTemplate(template.getBody(), variables);
            }

            // Envoyer selon le canal
            boolean sent = false;
            if (notification.getChannel() == Notification.NotificationChannel.EMAIL ||
                notification.getChannel() == Notification.NotificationChannel.ALL) {
                emailService.sendSimpleEmail(notification.getRecipientEmail(), title, message);
                sent = true;
            }

            if (notification.getChannel() == Notification.NotificationChannel.PUSH ||
                notification.getChannel() == Notification.NotificationChannel.ALL) {
                Map<String, Object> data = parseMetadata(notification.getMetadata());
                pushNotificationService.sendPushNotification(
                        notification.getRecipientId(),
                        title,
                        message,
                        data
                );
                sent = true;
            }

            if (notification.getChannel() == Notification.NotificationChannel.DASHBOARD ||
                notification.getChannel() == Notification.NotificationChannel.ALL) {
                // Pour le dashboard, la notification est déjà sauvegardée en base
                sent = true;
            }

            if (sent) {
                notification.setStatus(Notification.NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
            } else {
                notification.setStatus(Notification.NotificationStatus.FAILED);
            }

            notificationRepository.save(notification);
        } catch (Exception e) {
            notification.setStatus(Notification.NotificationStatus.FAILED);
            notificationRepository.save(notification);
            throw new RuntimeException("Erreur lors de l'envoi de la notification: " + e.getMessage(), e);
        }
    }

    public List<Notification> getNotificationsByRecipient(String recipientId) {
        return notificationRepository.findByRecipientId(recipientId);
    }

    public List<Notification> getUnreadNotifications(String recipientId) {
        return notificationRepository.findByRecipientIdAndStatus(recipientId, Notification.NotificationStatus.SENT);
    }

    private Map<String, String> extractVariablesFromMetadata(String metadata) {
        // En production, parser le JSON metadata
        Map<String, String> variables = new HashMap<>();
        // Pour l'instant, retourner un map vide
        return variables;
    }

    private Map<String, Object> parseMetadata(String metadata) {
        // En production, parser le JSON metadata
        Map<String, Object> data = new HashMap<>();
        // Pour l'instant, retourner un map vide
        return data;
    }
}


