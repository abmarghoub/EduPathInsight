package ens.edupath.note.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String studentId; // ID de l'étudiant depuis auth-service

    @Column(nullable = false, length = 100)
    private String studentUsername;

    @Column(nullable = false)
    private Long moduleId; // ID du module depuis module-service

    @Column(nullable = false, length = 50)
    private String moduleCode;

    @Column(nullable = false, length = 200)
    private String moduleName;

    @Column(nullable = false, length = 50)
    private String evaluationType; // Exam, Assignment, Project, Quiz, etc.

    @Column(nullable = false, length = 200)
    private String evaluationTitle;

    @Column(nullable = false)
    private Double score; // Note obtenue

    @Column(nullable = false)
    private Double maxScore; // Note maximale possible

    @Column(length = 500)
    private String comments;

    @Column(nullable = false)
    private LocalDateTime evaluationDate;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Méthode utilitaire pour calculer le pourcentage
    public Double getPercentage() {
        if (maxScore == null || maxScore == 0) {
            return 0.0;
        }
        return (score / maxScore) * 100;
    }

    // Méthode utilitaire pour vérifier si la note est échouée (note < 10/20)
    public Boolean isPassing() {
        Double percentage = getPercentage();
        return percentage >= 50.0; // 50% = 10/20
    }
}


