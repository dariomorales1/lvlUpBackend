package cl.levelup.authservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Respuesta de autenticación exitosa con tokens JWT")
public class AuthResponse {

    @Schema(
            description = "JWT Access token para autorización en requests subsiguientes",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
    )
    private String accessToken;

    @Schema(
            description = "Refresh token para obtener nuevos access tokens cuando expiren",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
    )
    private String refreshToken;

    @Schema(
            description = "Tipo de token (siempre 'Bearer')",
            example = "Bearer",
            defaultValue = "Bearer"
    )
    private String tokenType = "Bearer";

    @Schema(
            description = "ID único del usuario autenticado",
            example = "abc123def456"
    )
    private String userId;

    @Schema(
            description = "Email del usuario autenticado",
            example = "usuario@levelup.cl"
    )
    private String email;

    @Schema(
            description = "Rol del usuario autenticado",
            example = "USER",
            allowableValues = {"USER", "ADMIN", "MODERATOR"}
    )
    private String rol;

    public AuthResponse() {
    }

    public AuthResponse(String accessToken, String refreshToken, String tokenType,
                        String userId, String email, String rol) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.userId = userId;
        this.email = email;
        this.rol = rol;
    }

    // Getters and Setters...
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}