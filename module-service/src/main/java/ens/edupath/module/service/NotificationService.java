package ens.edupath.module.service;

import ens.edupath.module.entity.Enrollment;
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

    public void sendEnrollmentNotification(Enrollment enrollment, boolean byAdmin) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "ENROLLMENT_CREATED");
            message.put("enrollmentId", enrollment.getId());
            message.put("moduleId", enrollment.getModule().getId());
            message.put("moduleCode", enrollment.getModule().getCode());
            message.put("studentId", enrollment.getStudentId());
            message.put("studentEmail", enrollment.getStudentEmail());
            message.put("status", enrollment.getStatus().name());
            message.put("byAdmin", byAdmin);
            message.put("timestamp", enrollment.getEnrolledAt());

            rabbitTemplate.convertAndSend(exchange, routingKey, message);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de la notification d'inscription: " + e.getMessage());
        }
    }

    public void sendEnrollmentApprovalNotification(Enrollment enrollment) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "ENROLLMENT_APPROVED");
            message.put("enrollmentId", enrollment.getId());
            message.put("moduleId", enrollment.getModule().getId());
            message.put("moduleCode", enrollment.getModule().getCode());
            message.put("studentId", enrollment.getStudentId());
            message.put("studentEmail", enrollment.getStudentEmail());
            message.put("timestamp", enrollment.getApprovedAt());

            rabbitTemplate.convertAndSend(exchange, routingKey, message);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de la notification d'approbation: " + e.getMessage());
        }
    }

    public void sendEnrollmentRejectionNotification(Enrollment enrollment) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "ENROLLMENT_REJECTED");
            message.put("enrollmentId", enrollment.getId());
            message.put("moduleId", enrollment.getModule().getId());
            message.put("moduleCode", enrollment.getModule().getCode());
            message.put("studentId", enrollment.getStudentId());
            message.put("studentEmail", enrollment.getStudentEmail());
            message.put("timestamp", java.time.LocalDateTime.now());

            rabbitTemplate.convertAndSend(exchange, routingKey, message);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de la notification de rejet: " + e.getMessage());
        }
    }

    public void sendEnrollmentCancellationNotification(Enrollment enrollment) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "ENROLLMENT_CANCELLED");
            message.put("enrollmentId", enrollment.getId());
            message.put("moduleId", enrollment.getModule().getId());
            message.put("moduleCode", enrollment.getModule().getCode());
            message.put("studentId", enrollment.getStudentId());
            message.put("studentEmail", enrollment.getStudentEmail());
            message.put("timestamp", java.time.LocalDateTime.now());

            rabbitTemplate.convertAndSend(exchange, routingKey, message);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de la notification d'annulation: " + e.getMessage());
        }
    }
}


