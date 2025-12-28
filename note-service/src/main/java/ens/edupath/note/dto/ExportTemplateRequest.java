package ens.edupath.note.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ExportTemplateRequest {
    @NotNull(message = "L'ID du module est requis")
    private Long moduleId;

    @NotBlank(message = "Le code du module est requis")
    private String moduleCode;

    @NotBlank(message = "Le nom du module est requis")
    private String moduleName;

    @NotBlank(message = "Le type d'évaluation est requis")
    private String evaluationType; // Exam, Assignment, Project, Quiz, etc.

    @NotBlank(message = "Le titre de l'évaluation est requis")
    private String evaluationTitle;

    @NotNull(message = "La note maximale est requise")
    @Positive(message = "La note maximale doit être positive")
    private Double maxScore;
}


