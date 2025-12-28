package ens.edupath.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Le nom d'utilisateur ou l'email est requis")
    private String usernameOrEmail;

    @NotBlank(message = "Le mot de passe est requis")
    private String password;
}


