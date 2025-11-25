package cl.levelup.authservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "auth_sessions")
@Schema(description = "Sesión de autenticación para gestión de refresh tokens")
public class AuthSession {

    @Id
    @Column(columnDefinition = "uuid")
    @Schema(
            description = "ID único de la sesión (UUID)",
            example = "123e4567-e89b-12d3-a456-426614174000"
    )
    private String id;

    @Column(name = "user_id", nullable = false)
    @Schema(
            description = "ID del usuario asociado a la sesión",
            example = "user-123"
    )
    private String userId;

    @Column(name = "refresh_token", nullable = false)
    @Schema(
            description = "Refresh token JWT de la sesión",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String refreshToken;

    @Column(name = "created_at", nullable = false)
    @Schema(
            description = "Fecha y hora de creación de la sesión",
            example = "2024-01-15T10:30:00Z"
    )
    private OffsetDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    @Schema(
            description = "Fecha y hora de expiración de la sesión",
            example = "2024-01-22T10:30:00Z"
    )
    private OffsetDateTime expiresAt;

    @Column(nullable = false)
    @Schema(
            description = "Indica si la sesión ha sido revocada",
            example = "false"
    )
    private boolean revoked = false;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    // Getters and Setters...
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; }
    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }
}