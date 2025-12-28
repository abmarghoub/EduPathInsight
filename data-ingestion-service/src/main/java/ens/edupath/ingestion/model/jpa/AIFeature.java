package ens.edupath.ingestion.model.jpa;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "ai_features")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AIFeature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String entityType; // Student, Module, Evaluation, etc.

    @Column(nullable = false)
    private String entityId;

    @Column(columnDefinition = "TEXT")
    private String rawData; // JSON string of raw data

    @ElementCollection
    @CollectionTable(name = "ai_feature_values", joinColumns = @JoinColumn(name = "feature_id"))
    @MapKeyColumn(name = "feature_key")
    @Column(name = "feature_value")
    private Map<String, String> features; // Processed features for AI

    @Column(columnDefinition = "TEXT")
    private String metadata; // Additional metadata as JSON

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}


