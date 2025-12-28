package ens.edupath.notification.service;

import ens.edupath.notification.entity.Alert;
import ens.edupath.notification.entity.Notification;
import ens.edupath.notification.repository.AlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AlertService {

    private final AlertRepository alertRepository;
    private final NotificationService notificationService;

    public AlertService(AlertRepository alertRepository, NotificationService notificationService) {
        this.alertRepository = alertRepository;
        this.notificationService = notificationService;
    }

    public Alert createAlert(String recipientId, String alertType, String title, String message,
                            Alert.AlertSeverity severity, String alertData) {
        Alert alert = new Alert();
        alert.setRecipientId(recipientId);
        alert.setAlertType(alertType);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setSeverity(severity);
        alert.setAlertData(alertData);
        alert.setTriggeredAt(LocalDateTime.now());

        alert = alertRepository.save(alert);

        // Envoyer une notification pour l'alerte
        // Récupérer l'email du destinataire (en production, depuis auth-service)
        String recipientEmail = recipientId + "@example.com"; // TODO: Récupérer depuis auth-service
        
        notificationService.createNotification(
                recipientId,
                recipientEmail,
                alertType,
                title,
                message,
                Notification.NotificationChannel.ALL,
                null
        );

        return alert;
    }

    public List<Alert> getAlertsByRecipient(String recipientId) {
        return alertRepository.findByRecipientId(recipientId);
    }

    public List<Alert> getUnacknowledgedAlerts(String recipientId) {
        return alertRepository.findByRecipientIdAndAcknowledgedAtIsNull(recipientId);
    }

    public Alert acknowledgeAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alerte non trouvée avec l'ID: " + alertId));
        
        alert.setAcknowledgedAt(LocalDateTime.now());
        return alertRepository.save(alert);
    }

    public Alert resolveAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alerte non trouvée avec l'ID: " + alertId));
        
        alert.setResolvedAt(LocalDateTime.now());
        return alertRepository.save(alert);
    }
}


