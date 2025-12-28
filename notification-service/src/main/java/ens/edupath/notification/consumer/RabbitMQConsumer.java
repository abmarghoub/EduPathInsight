package ens.edupath.notification.consumer;

import ens.edupath.notification.entity.Alert;
import ens.edupath.notification.service.AlertService;
import ens.edupath.notification.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RabbitMQConsumer {

    private final AlertService alertService;
    private final NotificationService notificationService;

    public RabbitMQConsumer(AlertService alertService, NotificationService notificationService) {
        this.alertService = alertService;
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "notification.queue")
    public void receiveMessage(Map<String, Object> message) {
        try {
            String type = (String) message.get("type");
            
            switch (type) {
                case "HIGH_RISK_STUDENT":
                    handleHighRiskStudent(message);
                    break;
                case "RISK_MODULE":
                    handleRiskModule(message);
                    break;
                case "ANOMALY_DETECTED":
                    handleAnomalyDetected(message);
                    break;
                case "ENROLLMENT_CREATED":
                case "ENROLLMENT_APPROVED":
                case "ENROLLMENT_REJECTED":
                case "ENROLLMENT_CANCELLED":
                    handleEnrollmentNotification(message);
                    break;
                case "PRESENCE_RECORDED":
                case "ACTIVITY_RECORDED":
                    handleActivityNotification(message);
                    break;
                case "ALERT_CREATED":
                    handleAlertCreated(message);
                    break;
                default:
                    System.out.println("Type de message non géré: " + type);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du traitement du message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleHighRiskStudent(Map<String, Object> message) {
        Map<String, Object> data = (Map<String, Object>) message.get("data");
        String studentId = (String) data.get("studentId");
        String moduleId = String.valueOf(data.get("moduleId"));
        String riskLevel = (String) data.get("riskLevel");

        String title = "Alerte: Étudiant à risque élevé";
        String alertMessage = String.format(
                "L'étudiant %s présente un risque %s dans le module %s",
                studentId, riskLevel, moduleId
        );

        Alert.AlertSeverity severity = riskLevel.equals("CRITICAL") ? Alert.AlertSeverity.CRITICAL :
                                      riskLevel.equals("HIGH") ? Alert.AlertSeverity.HIGH :
                                      Alert.AlertSeverity.MEDIUM;

        alertService.createAlert(
                studentId,
                "HIGH_RISK_STUDENT",
                title,
                alertMessage,
                severity,
                data.toString()
        );
    }

    private void handleRiskModule(Map<String, Object> message) {
        Map<String, Object> data = (Map<String, Object>) message.get("data");
        String moduleId = String.valueOf(data.get("moduleId"));
        Double riskScore = ((Number) data.get("riskScore")).doubleValue();
        Integer atRiskCount = ((Number) data.get("at_risk_students_count")).intValue();

        String title = "Alerte: Module à risque";
        String alertMessage = String.format(
                "Le module %s présente un score de risque de %.2f avec %d étudiants à risque",
                moduleId, riskScore, atRiskCount
        );

        Alert.AlertSeverity severity = riskScore > 0.7 ? Alert.AlertSeverity.HIGH :
                                      riskScore > 0.5 ? Alert.AlertSeverity.MEDIUM :
                                      Alert.AlertSeverity.LOW;

        // Pour les modules, envoyer aux administrateurs
        alertService.createAlert(
                "admin",
                "RISK_MODULE",
                title,
                alertMessage,
                severity,
                data.toString()
        );
    }

    private void handleAnomalyDetected(Map<String, Object> message) {
        Map<String, Object> data = (Map<String, Object>) message.get("data");
        String studentId = (String) data.get("studentId");
        String anomalyType = (String) data.get("alertType");
        String title = (String) data.get("title");
        String description = (String) data.get("message");

        String alertMessage = String.format(
                "Anomalie détectée pour l'étudiant %s: %s - %s",
                studentId, title, description
        );

        alertService.createAlert(
                studentId,
                "ANOMALY_DETECTED",
                title,
                alertMessage,
                Alert.AlertSeverity.MEDIUM,
                data.toString()
        );
    }

    private void handleEnrollmentNotification(Map<String, Object> message) {
        Map<String, Object> data = (Map<String, Object>) message.get("data");
        String studentId = (String) data.get("studentId");
        String studentEmail = (String) data.get("studentEmail");
        String type = (String) message.get("type");

        String title = "";
        String messageText = "";

        switch (type) {
            case "ENROLLMENT_APPROVED":
                title = "Inscription approuvée";
                messageText = "Votre inscription a été approuvée.";
                break;
            case "ENROLLMENT_REJECTED":
                title = "Inscription rejetée";
                messageText = "Votre inscription a été rejetée.";
                break;
            case "ENROLLMENT_CANCELLED":
                title = "Inscription annulée";
                messageText = "Votre inscription a été annulée.";
                break;
            default:
                title = "Notification d'inscription";
                messageText = "Vous avez une nouvelle notification concernant votre inscription.";
        }

        notificationService.createNotification(
                studentId,
                studentEmail,
                type,
                title,
                messageText,
                ens.edupath.notification.entity.Notification.NotificationChannel.ALL,
                data
        );
    }

    private void handleActivityNotification(Map<String, Object> message) {
        // Pour les activités, on peut choisir de ne pas envoyer de notification
        // ou d'envoyer seulement pour certaines activités importantes
        System.out.println("Notification d'activité reçue: " + message.get("type"));
    }

    private void handleAlertCreated(Map<String, Object> message) {
        // Les alertes sont déjà créées par le service d'alerte
        System.out.println("Alerte créée: " + message.get("type"));
    }
}


