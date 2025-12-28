package ens.edupath.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AlertRequest {
    @NotBlank(message = "L'ID du destinataire est requis")
    private String recipientId;

    @NotBlank(message = "Le type d'alerte est requis")
    private String alertType;

    @NotBlank(message = "Le titre est requis")
    private String title;

    @NotBlank(message = "Le message est requis")
    private String message;

    @NotNull(message = "La sévérité est requise")
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL

    private String alertData;
}


