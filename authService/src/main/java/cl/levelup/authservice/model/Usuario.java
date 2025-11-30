package cl.levelup.authservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "usuarios")
@Schema(description = "Entidad que representa un usuario del sistema")
public class Usuario {

    @Id
    @Column(name = "id", nullable = false)
    @Schema(
            description = "ID único del usuario (Firebase UID)",
            example = "abc123def456",
            required = true
    )
    private String id;

    @Column(name = "email", nullable = false, unique = true)
    @Schema(
            description = "Email único del usuario",
            example = "usuario@levelup.cl",
            required = true
    )
    private String email;

    @Column(name = "nombre", nullable = false)
    @Schema(
            description = "Nombre completo del usuario",
            example = "Juan Pérez",
            required = true
    )
    private String nombre;

    @Column(name = "fecha_nacimiento")
    @Schema(
            description = "Fecha de nacimiento del usuario",
            example = "1990-05-15"
    )
    private LocalDate fechaNacimiento;

    @Column(name = "avatar_url")
    @Schema(
            description = "URL de la imagen de avatar del usuario",
            example = "https://example.com/avatar.jpg"
    )
    private String avatarUrl;

    @Column(name = "activo", nullable = false)
    @Schema(
            description = "Indica si el usuario está activo en el sistema",
            example = "true",
            defaultValue = "true"
    )
    private boolean activo = true;

    @Column(name = "creado_en", nullable = false, updatable = false)
    @Schema(
            description = "Fecha y hora de creación del usuario",
            example = "2024-01-15T10:30:00Z"
    )
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    @Schema(
            description = "Fecha y hora de última actualización del usuario",
            example = "2024-01-15T10:30:00Z"
    )
    private OffsetDateTime actualizadoEn;

    @Column(name = "rol", nullable = false)
    @Schema(
            description = "Rol del usuario en el sistema",
            example = "USER",
            allowableValues = {"USER", "ADMIN", "MODERATOR"},
            defaultValue = "USER"
    )
    private String rol = "USER";

    @PrePersist
    protected void onCreate() {
        creadoEn = OffsetDateTime.now();
        actualizadoEn = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        actualizadoEn = OffsetDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public OffsetDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(OffsetDateTime creadoEn) { this.creadoEn = creadoEn; }
    public OffsetDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(OffsetDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}