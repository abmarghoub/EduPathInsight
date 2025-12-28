package ens.edupath.ingestion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FileUploadRequest {
    @NotNull(message = "Le fichier est requis")
    private MultipartFile file;

    @NotBlank(message = "Le type d'entité est requis")
    private String entityType; // User, Module, Note, Presence, Activity

    private Boolean async = false; // Traitement asynchrone ou synchrone
}


