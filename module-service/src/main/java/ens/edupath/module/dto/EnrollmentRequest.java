package ens.edupath.module.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EnrollmentRequest {
    @NotNull(message = "L'ID du module est requis")
    private Long moduleId;
}


