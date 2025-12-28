package ens.edupath.module.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ModuleRequest {
    @NotBlank(message = "Le code du module est requis")
    @Size(max = 50, message = "Le code ne doit pas dépasser 50 caractères")
    private String code;

    @NotBlank(message = "Le nom du module est requis")
    @Size(max = 200, message = "Le nom ne doit pas dépasser 200 caractères")
    private String name;

    private String description;

    @NotNull(message = "Le nombre de crédits est requis")
    private Integer credits;

    private Boolean active = true;
}


