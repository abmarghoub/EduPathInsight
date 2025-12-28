package ens.edupath.note.service;

import ens.edupath.note.entity.Alert;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AlertNotificationService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.template.exchange}")
    private String exchange;

    @Value("${spring.rabbitmq.template.routing-key}")
    private String routingKey;

    @Autowired
    public AlertNotificationService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendAlertNotification(Alert alert) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "ALERT_CREATED");
            message.put("alertId", alert.getId());
            message.put("alertType", alert.getType().name());
            message.put("studentId", alert.getStudentId());
            message.put("moduleId", alert.getModuleId());
            message.put("title", alert.getTitle());
            message.put("message", alert.getMessage());
            message.put("thresholdValue", alert.getThresholdValue());
            message.put("actualValue", alert.getActualValue());
            message.put("timestamp", alert.getCreatedAt());

            rabbitTemplate.convertAndSend(exchange, routingKey, message);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de la notification d'alerte: " + e.getMessage());
        }
    }
}


