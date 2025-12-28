package ens.edupath.note.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoteRequest {
    @NotNull(message = "L'ID de l'étudiant est requis")
    private String studentId;

    @NotNull(message = "L'ID du module est requis")
    private Long moduleId;

    @NotNull(message = "Le type d'évaluation est requis")
    private String evaluationType; // Exam, Assignment, Project, Quiz, etc.

    @NotNull(message = "Le titre de l'évaluation est requis")
    private String evaluationTitle;

    @NotNull(message = "La note est requise")
    @Positive(message = "La note doit être positive")
    private Double score;

    @NotNull(message = "La note maximale est requise")
    @Positive(message = "La note maximale doit être positive")
    private Double maxScore;

    private String comments;

    @NotNull(message = "La date d'évaluation est requise")
    private LocalDateTime evaluationDate;
}


