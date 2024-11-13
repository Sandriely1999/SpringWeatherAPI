package model.requests;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class AuthRequest {
    @NotBlank(message = "Username não pode estar em branco")
    private String username;

    @NotBlank(message = "Password não pode estar em branco")
    private String password;
}