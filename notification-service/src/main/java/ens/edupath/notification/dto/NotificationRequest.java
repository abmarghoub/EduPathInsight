package ens.edupath.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class NotificationRequest {
    @NotBlank(message = "L'ID du destinataire est requis")
    private String recipientId;

    @NotBlank(message = "L'email du destinataire est requis")
    private String recipientEmail;

    @NotBlank(message = "Le type de notification est requis")
    private String type;

    @NotBlank(message = "Le titre est requis")
    private String title;

    @NotBlank(message = "Le message est requis")
    private String message;

    @NotNull(message = "Le canal est requis")
    private String channel; // EMAIL, PUSH, DASHBOARD, ALL

    private Map<String, Object> metadata;
}


