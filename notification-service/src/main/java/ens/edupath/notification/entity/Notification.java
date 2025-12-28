package ens.edupath.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String recipientId; // ID du destinataire (étudiant, enseignant, etc.)

    @Column(nullable = false)
    private String recipientEmail;

    @Column(nullable = false, length = 100)
    private String type; // HIGH_RISK_STUDENT, ENROLLMENT_APPROVED, etc.

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel; // EMAIL, PUSH, DASHBOARD, ALL

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.PENDING; // PENDING, SENT, FAILED

    @Column(columnDefinition = "JSON")
    private String metadata; // Données supplémentaires

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    private LocalDateTime sentAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum NotificationChannel {
        EMAIL,
        PUSH,
        DASHBOARD,
        ALL
    }

    public enum NotificationStatus {
        PENDING,
        SENT,
        FAILED
    }
}


