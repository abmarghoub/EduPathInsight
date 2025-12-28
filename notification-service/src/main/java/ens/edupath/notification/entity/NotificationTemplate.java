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
@Table(name = "notification_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class NotificationTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String templateKey; // HIGH_RISK_STUDENT_EMAIL, ENROLLMENT_APPROVED_PUSH, etc.

    @Column(nullable = false, length = 50)
    private String notificationType; // HIGH_RISK_STUDENT, ENROLLMENT_APPROVED, etc.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Notification.NotificationChannel channel;

    @Column(nullable = false, length = 200)
    private String subject; // Pour email, titre pour push/dashboard

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body; // Corps du message avec variables {{variable}}

    @Column(nullable = false)
    private Boolean active = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}


