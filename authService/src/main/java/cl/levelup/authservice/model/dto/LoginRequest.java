package cl.levelup.authservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Solicitud de login usando Firebase Authentication")
public class LoginRequest {

    @Schema(
            description = "Token ID de Firebase Authentication obtenido del cliente",
            example = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjFmNj...",
            required = true
    )
    @NotBlank(message = "firebaseIdToken es requerido")
    private String firebaseIdToken;

    public String getFirebaseIdToken() {
        return firebaseIdToken;
    }

    public void setFirebaseIdToken(String firebaseIdToken) {
        this.firebaseIdToken = firebaseIdToken;
    }
}