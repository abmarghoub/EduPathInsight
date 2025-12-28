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
@Table(name = "kpis", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "module_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class KPI {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String studentId;

    @Column(nullable = false)
    private Long moduleId;

    @Column(nullable = false)
    private Double averageScore; // Moyenne des notes

    @Column(nullable = false)
    private Double totalScore; // Somme des notes

    @Column(nullable = false)
    private Double totalMaxScore; // Somme des notes maximales

    @Column(nullable = false)
    private Integer numberOfEvaluations; // Nombre d'évaluations

    @Column(nullable = false)
    private Integer passingCount; // Nombre de notes ≥ 10/20

    @Column(nullable = false)
    private Integer failingCount; // Nombre de notes < 10/20

    @Column(nullable = false)
    private Double highestScore; // Note la plus élevée

    @Column(nullable = false)
    private Double lowestScore; // Note la plus basse

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime lastCalculatedAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}


