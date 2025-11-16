package cl.levelup.userservice.model.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public class UsuarioResponse {
    private String id;
    private String email;
    private String nombre;
    private LocalDate fechaNacimiento;
    private String avatarUrl;
    private boolean activo;
    private String rol;
    private OffsetDateTime creadoEn;
    private OffsetDateTime actualizadoEn;

    // ------- CONSTRUCTORES -------
    public UsuarioResponse() {}

    public UsuarioResponse(String id, String email, String nombre, LocalDate fechaNacimiento,
                           String avatarUrl, boolean activo, String rol,
                           OffsetDateTime creadoEn, OffsetDateTime actualizadoEn) {
        this.id = id;
        this.email = email;
        this.nombre = nombre;
        this.fechaNacimiento = fechaNacimiento;
        this.avatarUrl = avatarUrl;
        this.activo = activo;
        this.rol = rol;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
    }

    // ------- GETTERS & SETTERS -------
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

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public OffsetDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(OffsetDateTime creadoEn) { this.creadoEn = creadoEn; }

    public OffsetDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(OffsetDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}