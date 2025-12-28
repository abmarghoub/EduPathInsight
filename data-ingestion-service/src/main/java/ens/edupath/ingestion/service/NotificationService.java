package ens.edupath.ingestion.service;

import ens.edupath.ingestion.model.jpa.IngestionLog;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.template.exchange}")
    private String exchange;

    @Value("${spring.rabbitmq.template.routing-key}")
    private String routingKey;

    @Autowired
    public NotificationService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendIngestionNotification(IngestionLog log) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("logId", log.getId());
            message.put("fileName", log.getFileName());
            message.put("entityType", log.getEntityType());
            message.put("status", log.getStatus().name());
            message.put("totalRecords", log.getTotalRecords());
            message.put("successfulRecords", log.getSuccessfulRecords());
            message.put("failedRecords", log.getFailedRecords());
            message.put("timestamp", log.getCreatedAt());

            rabbitTemplate.convertAndSend(exchange, routingKey, message);
        } catch (Exception e) {
            // Logger l'erreur mais ne pas faire échouer le traitement
            System.err.println("Erreur lors de l'envoi de la notification: " + e.getMessage());
        }
    }
}


