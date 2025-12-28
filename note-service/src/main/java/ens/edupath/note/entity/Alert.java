package ens.edupath.note.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String studentId;

    @Column(nullable = false)
    private Long moduleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AlertType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AlertStatus status = AlertStatus.ACTIVE;

    @Column(nullable = false)
    private Double thresholdValue; // Valeur du seuil qui a déclenché l'alerte

    @Column(nullable = false)
    private Double actualValue; // Valeur actuelle

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime acknowledgedAt; // Date de prise en compte

    public enum AlertType {
        LOW_GRADE,              // Note basse
        LOW_AVERAGE,            // Moyenne faible
        FAILING_GRADE,          // Note échouée
        DROPPING_PERFORMANCE,   // Performance en baisse
        MULTIPLE_FAILURES       // Plusieurs échecs
    }

    public enum AlertStatus {
        ACTIVE,
        ACKNOWLEDGED,
        RESOLVED,
        DISMISSED
    }
}


